/**
 *
 */
package com.visfresh.mpl.services.siblings;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.StringUtils;

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
        if (shipment.getSiblings().isEmpty()) {
            return new LinkedList<>();
        }

        final List<Shipment> group = shipmentDao.findAll(shipment.getSiblings());
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

        final List<Shipment> shipments = new LinkedList<>(findActiveShipments(company));

        //detect siblings.
        if (!shipments.isEmpty()) {
            final Map<Long, Set<Long>> siblingMap = new HashMap<>();
            //initialize sibling map
            for (final Shipment s : shipments) {
                siblingMap.put(s.getId(), new HashSet<Long>());
            }

            //get shipments for group
            while (!shipments.isEmpty()) {
                final Shipment master = shipments.remove(0);
                findSiblings(master, shipments, siblingMap);

                log.debug("Found siblings (" + StringUtils.combine(siblingMap.get(master.getId()), ",")
                        + ") for shipment " + master.getId());
                if (!isEquals(master.getSiblings(), siblingMap.get(master.getId()))) {
                    updateSiblingInfo(master, siblingMap.get(master.getId()));
                }
            }
        }

        log.debug("End of search shipment siblings for company " + company.getName());
    }
    /**
     * @param master
     * @param originShipments
     * @param siblingMap
     */
    private void findSiblings(final Shipment master, final List<Shipment> originShipments,
            final Map<Long, Set<Long>> siblingMap) {
        final List<Shipment> shipments = new LinkedList<>(originShipments);

        //create shipment map
        final Map<Long, Shipment> shipmentMap = new HashMap<>();
        for (final Shipment s : shipments) {
            shipmentMap.put(s.getId(), s);
        }

        //add inactive siblings
        final Set<Long> ids = new HashSet<>();
        for (final Long id : master.getSiblings()) {
            if (!shipmentMap.containsKey(id)) {
                ids.add(id);
            }
        }

        //get shipments from DB
        final List<Shipment> otherSiblings = getShipments(ids);
        for (final Shipment s : otherSiblings) {
            if (s.hasFinalStatus() && s.getCompany().getId().equals(master.getCompany().getId())) {
                shipments.add(s);
            }
        }

        final List<TrackerEvent> masterEvents = getTrackeEvents(master);
        for (final Shipment s : shipments) {
            final List<TrackerEvent> events = getTrackeEvents(s);
            if (isSiblings(masterEvents, events)) {
                siblingMap.get(master.getId()).add(s.getId());
            }
            if (!s.hasFinalStatus() && isSiblings(events, masterEvents)) {
                siblingMap.get(s.getId()).add(master.getId());
            }
        }
    }
    /**
     * @param ids set of shipment ID.
     * @return shipment list.
     */
    protected List<Shipment> getShipments(final Set<Long> ids) {
        return shipmentDao.findAll(ids);
    }

    /**
     * @param master
     * @param set
     */
    protected void updateSiblingInfo(final Shipment master, final Set<Long> set) {
        master.getSiblings().clear();
        master.getSiblings().addAll(set);
        master.setSiblingCount(master.getSiblings().size());
        shipmentDao.updateSiblingInfo(master);
    }

    /**
     * @param set1
     * @param set2
     * @return
     */
    private boolean isEquals(final Set<Long> set1, final Set<Long> set2) {
        if (set1.size() != set2.size()) {
            return false;
        }

        for (final Long id : set1) {
            if (!set2.contains(id)) {
                return false;
            }
        }
        return true;
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
    protected List<TrackerEvent> getTrackeEvents(final Shipment shipment) {
        final List<TrackerEvent> list = getEventsFromDb(shipment);

        //filter events without locations.
        final Iterator<TrackerEvent> iter = list.iterator();
        while (iter.hasNext()) {
            final TrackerEvent e = iter.next();
            if (e.getLatitude() == null || e.getLongitude() == null) {
                iter.remove();
            }
        }

        return list;
    }

    /**
     * @param shipment
     * @return
     */
    protected List<TrackerEvent> getEventsFromDb(final Shipment shipment) {
        return trackerEventDao.getEvents(shipment);
    }
    /**
     * @param originE1
     * @param originE2
     * @return true if siblings.
     */
    protected boolean isSiblings(final List<TrackerEvent> originE1, final List<TrackerEvent> originE2) {
        if (originE1.isEmpty() || originE2.isEmpty()) {
            return false;
        }

        final List<TrackerEvent> e1 = new LinkedList<>(originE1);
        final List<TrackerEvent> e2 = new LinkedList<>(originE2);

        final List<Double> distances = new LinkedList<>();

        TrackerEvent before = e2.remove(0);
        final Iterator<TrackerEvent> iter1 = e1.iterator();
        while (iter1.hasNext()) {
            final TrackerEvent e = iter1.next();
            Double distance = null;

            if (!e.getTime().before(before.getTime())) {
                final Iterator<TrackerEvent> iter2 = e2.iterator();
                while (iter2.hasNext()) {
                    final TrackerEvent after = iter2.next();
                    if (!before.getTime().after(e.getTime()) && !after.getTime().before(e.getTime())) {
                        distance = getDistance(e, before, after);
                        break;
                    }

                    before = after;
                    iter2.remove();
                }

                if (distance == null) {
                    distance = getDistance(e, before);
                }

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

        return getDistanceMeters(e.getLatitude(), e.getLongitude(), lat, lon);
    }
    /**
     * @param e1 first tracker event.
     * @param e2 second tracker event.
     * @return distance between two event in meters.
     */
    private double getDistance(final TrackerEvent e1, final TrackerEvent e2) {
        return getDistanceMeters(e1.getLatitude(), e1.getLongitude(),
                e2.getLatitude(), e2.getLongitude());
    }

    /**
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    protected double getDistanceMeters(final double lat1, final double lon1,
            final double lat2, final double lon2) {
        return LocationUtils.getDistanceMeters(lat1, lon1, lat2, lon2);
    }
}
