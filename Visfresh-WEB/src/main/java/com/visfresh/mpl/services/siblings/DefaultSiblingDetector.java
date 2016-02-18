/**
 *
 */
package com.visfresh.mpl.services.siblings;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.CollectionUtils;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultSiblingDetector implements SiblingDetector {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultSiblingDetector.class);
    protected static final double MAX_DISTANCE_AVERAGE = 3000; //meters
    /**
     * Group name prefix.
     */
    private static final String GROUP_PREFIX = "siblingGroup_";

    /**
     * Number of sibling detection threads.
     */
    private int numberOfThreads = 1;
    @Autowired
    private CompanyDao companyDao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * @param env spring environment.
     */
    @Autowired
    public DefaultSiblingDetector(final Environment env) {
        this(Integer.parseInt(env.getProperty("sibling.detect.numThreads", "1")));
    }

    /**
     * @param threadNum number of threads.
     */
    protected DefaultSiblingDetector(final int threadNum) {
        numberOfThreads = threadNum;
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.SiblingDetector#getSiblings(com.visfresh.entities.Shipment)
     */
    @Override
    public List<Shipment> getSiblings(final Shipment shipment) {
        if (shipment.getSiblingGroup() == null) {
            return new LinkedList<>();
        }

        final List<Shipment> group = shipmentDao.getSiblingGroup(shipment.getSiblingGroup());
        removeGivenShipment(group, shipment);
        return group;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SiblingDetectorService#getSiblingCount(com.visfresh.entities.Shipment)
     */
    @Override
    public int getSiblingCount(final Shipment shipment) {
        return shipment.getSiblingCount();
    }

    public void detectSiblings() {
        log.debug("Sibling detection has started");

        final ExecutorService pool = Executors.newFixedThreadPool(numberOfThreads);
        try {
            //TODO add pagination
            final List<Company> compaines = companyDao.findAll(null, null, null);
            for (final Company company : compaines) {
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        updateShipmentSiblingsForCompany(company);
                    }
                });
            }

            pool.shutdown();
            if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
                log.warn("End of sibling searching is not reached");
            }
        } catch (final Throwable t) {
            log.error("Failed to detect siblings", t);
        } finally {
            pool.shutdown();
        }

        log.debug("Sibling detection has finished");
    }
    /**
     * @param company company to process.
     */
    public void updateShipmentSiblingsForCompany(final Company company) {
        log.debug("Start of search shipment siblings for company " + company.getName());

        final List<Shipment> shipments = findActiveShipments(company);
        final Set<Long> alreadyUsedGroups = new HashSet<>();

        if (!shipments.isEmpty()) {
            //get shipments for group
            CollectionUtils.sortById(shipments);
            while (!shipments.isEmpty()) {
                final Shipment master = shipments.remove(0);

                // get group ID for given master shipment
                final Long groupId = getGroupId(master, alreadyUsedGroups);

                //create group
                final List<Shipment> group = new LinkedList<>();
                group.add(master);

                //cut siblings for group from active shipment list.
                group.addAll(cutSiblings(master, shipments));

                //get inactive siblings
                final List<Shipment> inactive = getInactiveSiblings(groupId);
                final List<Shipment> toRemoveFromGroup = cutBySomeDevice(inactive, group);

                //save shipments if required.
                final int siblingCount = group.size() + inactive.size() - 1;
                final List<Shipment> toUpdate = new LinkedList<Shipment>();

                final List<Shipment> all = new LinkedList<>(group);
                all.addAll(inactive);

                for (final Shipment s : all) {
                    //set sibling group ID to sibling
                    final Long oldId = s.getSiblingGroup();
                    if (s.getSiblingCount() != siblingCount || !groupId.equals(oldId)) {
                        toUpdate.add(s);
                    }
                }

                updateSiblingInfo(toUpdate, groupId, siblingCount);

                if (!toRemoveFromGroup.isEmpty()) {
                    updateSiblingInfo(toRemoveFromGroup,
                            -toRemoveFromGroup.get(0).getId(), toRemoveFromGroup.size() - 1);
                }

                log.debug("Found " + group.size() + " siblings for group " + groupId);
            }
        }

        log.debug("End of search shipment siblings for company " + company.getName());
    }

    /**
     * @param inactive
     * @param group
     * @return
     */
    private List<Shipment> cutBySomeDevice(final List<Shipment> inactive,
            final List<Shipment> group) {
        //create device set.
        final Set<String> devices = new HashSet<String>();
        for (final Shipment s : group) {
            devices.add(s.getDevice().getImei());
        }

        final List<Shipment> bySomeDevice = new LinkedList<Shipment>();
        final Iterator<Shipment> iter = inactive.iterator();
        while (iter.hasNext()) {
            final Shipment s = iter.next();
            if (devices.contains(s.getDevice().getImei())) {
                iter.remove();
                bySomeDevice.add(s);
            }
        }

        return bySomeDevice;
    }

    /**
     * @param master master shipment.
     * @param shipments active shipment list.
     * @return list of siblings for given master shipment.
     */
    private List<Shipment> cutSiblings(final Shipment master,
            final List<Shipment> shipments) {
        final List<Shipment> siblings = new LinkedList<Shipment>();

        final TrackerEvent[] masterEvents = getTrackeEvents(master);
        //find siblings for given shipment
        final Iterator<Shipment> iter = shipments.iterator();
        while (iter.hasNext()) {
            final Shipment s = iter.next();
            if (isSiblings(s, master, masterEvents)) {
                iter.remove();
                siblings.add(s);
            }
        }

        return siblings;
    }

    /**
     * @param master
     * @param alreadyUsedGroups
     * @return
     */
    protected Long getGroupId(final Shipment master,
            final Set<Long> alreadyUsedGroups) {
        Long groupId = master.getSiblingGroup();

        //check already used group
        if (alreadyUsedGroups.contains(groupId)) {
            //clear group ID
            groupId = null;
        }
        if (groupId == null) {
            //if the group ID is null, assign it to master shipment ID.
            groupId = master.getId();
        }

        //save group ID as already used
        alreadyUsedGroups.add(groupId);
        return groupId;
    }

    /**
     * @param s shipment to test.
     * @param master master shipment.
     * @param masterEvents master shipment events.
     * @return
     */
    private boolean isSiblings(final Shipment s, final Shipment master,
            final TrackerEvent[] masterEvents) {
        //check test group
        final String masterTestGroup = getSiblingTestGroup(master);
        final String testGroup = getSiblingTestGroup(s);

        if (testGroup != null) {
            return testGroup.equals(masterTestGroup);
        }

        //check ordinary group
        if (masterEvents.length > 0) {
            return isSiblings(s, masterEvents);
        }
        return false;
    }

    /**
     * save shipments has moved to separate method only for test purposes.
     * @param shipments shipment.
     * @param siblingGroup sibling group.
     * @param siblingCount sibling count.
     */
    protected void updateSiblingInfo(final List<Shipment> shipments,
            final Long siblingGroup, final int siblingCount) {
        shipmentDao.updateSiblingInfo(shipments, siblingGroup, siblingCount);
    }
    /**
     * Find active shipments has moved to separate method only for test purposes.
     * @param company company.
     * @return list of active shipments for given company.
     */
    protected List<Shipment> findActiveShipments(final Company company) {
        return shipmentDao.findActiveShipments(company);
    }
    /**
     * @param groupId group ID.
     * @return list of inactive siblings in group.
     */
    protected List<Shipment> getInactiveSiblings(final Long groupId) {
        final List<Shipment> group = shipmentDao.getSiblingGroup(groupId);

        final List<Shipment> inactive = new LinkedList<Shipment>();
        for (final Shipment s : group) {
            if (s.hasFinalStatus()) {
                inactive.add(s);
            }
        }

        return inactive;
    }

    /**
     * @param shipment shipment.
     * @return array of tracker events.
     */
    protected TrackerEvent[] getTrackeEvents(final Shipment shipment) {
        final List<TrackerEvent> list = trackerEventDao.getEvents(shipment);
        return list.toArray(new TrackerEvent[list.size()]);
    }
    /**
     * @param s other sibling.
   )  * @param master master sibling events.
     * @return true if shipments are siblings.
     */
    //set as protected only for testing purposes.
    protected boolean isSiblings(final Shipment s, final TrackerEvent[] masterEvents) {
        final List<Double> distances = new LinkedList<>();

        for (final TrackerEvent e: getTrackeEvents(s)) {
            final Double distance = getDistanceMeters(e, masterEvents);
            if (distance != null) {
                distances.add(distance);
            }
        }

        if (distances.isEmpty()) {
            return false;
        }

        double summ = 0;
        final double norma = distances.size();
        for (final Double d : distances) {
            summ += d / norma;
        }

        return summ < MAX_DISTANCE_AVERAGE;
    }

    /**
     * @param e event.
     * @param masterEvents master shipment trajectory.
     * @return distance between event and master shipment trajectory.
     */
    //have protected modified only for testing purposes
    protected Double getDistanceMeters(final TrackerEvent e, final TrackerEvent[] masterEvents) {
        //check out of bounds
        if (e.getTime().before(masterEvents[0].getTime())) {
            return null;
        }

        //check out of last event.
        final TrackerEvent lastEvent = masterEvents[masterEvents.length - 1];
        if (e.getTime().after(lastEvent.getTime())) {
            return null;
        }

        //do binary search
        int low = 0;
        int high = masterEvents.length - 1;
        final long key = e.getTime().getTime();

        while (low + 1 < high) {
            final int mid = (low + high) / 2;
            final long midVal = masterEvents[mid].getTime().getTime();

            if (midVal < key) {
                low = mid;
            } else if (midVal > key) {
                high = mid;
            } else {
                low = mid;
                high = low + 1;
                break;
            }
        }

        if (low + 1 < masterEvents.length) {
            return getDistance(e, masterEvents[low], masterEvents[high]);
        } else {
            return getDistance(e, masterEvents[low]);
        }
    }

    /**
     * @param e event.
     * @param me1 first master event.
     * @param me2 second master event.
     * @return distance between given event and the sub path
     */
    private double getDistance(final TrackerEvent e, final TrackerEvent me1,
            final TrackerEvent me2) {
        //not ordinary situations
        final Date mt1 = me1.getTime();
        final Date mt2 = me2.getTime();
        if (mt1.equals(mt2)) {
            return getDistance(e, me1);
        }

        final Date t = e.getTime();
        if (!t.after(mt1)) {
            return getDistance(e, me1);
        }
        if (!t.before(mt2)) {
            return getDistance(e, me2);
        }

        //ordinary situation
        final double delta = (double) (t.getTime() - mt1.getTime()) / (mt2.getTime() - mt1.getTime());

        final double lat = me1.getLatitude() + delta * (me2.getLatitude() - me1.getLatitude());
        final double lon = me1.getLongitude() + delta * (me2.getLongitude() - me1.getLongitude());

        return LocationUtils.getDistanceMeters(e.getLatitude(), e.getLongitude(), lat, lon);
    }

    /**
     * @param e1 first tracker event.
     * @param e2 second tracker event.
     * @return distance between two event in meters.
     */
    private double getDistance(final TrackerEvent e1, final TrackerEvent e2) {
        return LocationUtils.getDistanceMeters(e1.getLatitude(), e1.getLongitude(),
                e2.getLatitude(), e2.getLongitude());
    }

    /**
     * @param list shipment group.
     * @param shipment shipment to remove.
     */
    private void removeGivenShipment(final List<Shipment> list, final Shipment shipment) {
        //remove given shipment from group
        final Iterator<Shipment> iter = list.iterator();
        while (iter.hasNext()) {
            if (iter.next().getId().equals(shipment.getId())) {
                iter.remove();
                break;
            }
        }
    }
    /**
     * @param shipment
     * @return
     */
    private String getSiblingTestGroup(final Shipment shipment) {
        final String desc = shipment.getShipmentDescription();
        if (desc != null) {
            for (final String seg: desc.split("[^\\w]+")) {
                if (seg.startsWith(GROUP_PREFIX)) {
                    return seg.substring(GROUP_PREFIX.length());
                }
            }
        }
        return null;
    }
}
