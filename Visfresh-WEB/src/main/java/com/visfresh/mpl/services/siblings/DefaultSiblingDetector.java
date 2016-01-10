/**
 *
 */
package com.visfresh.mpl.services.siblings;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    protected static final double MAX_DISTANCE = 3000; //meters
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
    public int getSiblingCount(final Shipment s) {
        return getSiblings(s).size();
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
        if (!shipments.isEmpty()) {
            //get shipments for group
            CollectionUtils.sortById(shipments);
            while (!shipments.isEmpty()) {
                final Shipment master = shipments.remove(0);

                int count = 1;
                Long groupId = master.getSiblingGroup();
                if (groupId == null) {
                    //if the group ID is null, assign it to master shipment ID.
                    groupId = master.getId();
                    master.setSiblingGroup(groupId);
                    saveShipment(master);
                }

                final TrackerEvent[] masterEvents = getTrackeEvents(master);
                if (masterEvents.length > 0) {
                    //find siblings for given shipment
                    final Iterator<Shipment> iter = shipments.iterator();
                    while (iter.hasNext()) {
                        final Shipment s = iter.next();
                        if (isSiblings(s, masterEvents)) {
                            count++;
                            iter.remove();

                            //set sibling group ID to sibling
                            final Long oldId = s.getSiblingGroup();
                            if (oldId == null || !oldId.equals(groupId)) {
                               s.setSiblingGroup(groupId);
                               saveShipment(s);
                            }
                        }
                    }
                }

                log.debug("Found " + count + " siblings for group " + groupId);
            }
        }

        log.debug("End of search shipment siblings for company " + company.getName());
    }

    /**
     * save shipments has moved to separate method only for test purposes.
     * @param shipment shipment.
     */
    protected void saveShipment(final Shipment shipment) {
        shipmentDao.save(shipment);
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
     * @param shipment shipment.
     * @return array of tracker events.
     */
    protected TrackerEvent[] getTrackeEvents(final Shipment shipment) {
        final List<TrackerEvent> list = trackerEventDao.getEvents(shipment);
        return list.toArray(new TrackerEvent[list.size()]);
    }
    /**
     * TODO implement more accurate
     * @param s other sibling.
     * @param master master sibling events.
     * @return true if shipments are siblings.
     */
    //set as protected only for testing purposes.
    protected boolean isSiblings(final Shipment s, final TrackerEvent[] masterEvents) {
        int near = 0;
        int far = 0;

        for (final TrackerEvent e: getTrackeEvents(s)) {
            final Double distance = getDistanceMeters(e, masterEvents);
            if (distance != null) {
                if (distance < MAX_DISTANCE) {
                    near++;
                } else {
                    far++;
                }
            }
        }

        return ((double) far / (double) near < 0.01);
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
            if (masterEvents.length > 1) {
                final long eventTimeOut = masterEvents[0].getTime().getTime()
                        - lastEvent.getTime().getTime();
                if (e.getTime().getTime() - lastEvent.getTime().getTime() <= eventTimeOut) {
                    return getDistance(e, lastEvent);
                }
            }
            return null;
        }

        //do binary search
        int low = 0;
        int high = masterEvents.length - 1;
        final long key = e.getTime().getTime();

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final long midVal = masterEvents[mid].getTime().getTime();

            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                low = mid;
                break;
            }
        }

        if (low + 1 < masterEvents.length) {
            return getDistance(e, masterEvents[low], masterEvents[low + 1]);
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

        final long lat = Math.round(me1.getLatitude() + delta * (me2.getLatitude() - me1.getLatitude()));
        final long lon = Math.round(me1.getLongitude() + delta * (me2.getLongitude() - me1.getLongitude()));

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
}
