/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.EntityUtils;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SiblingDetectionRule implements TrackerEventRule {
    /**
     * Minimal path for siblings.
     */
    protected static final int MIN_PATH = 25000; // meters
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SiblingDetectionRule.class);
    protected static final double MAX_DISTANCE_AVERAGE = 3000; //meters
    private static final String NAME = "SiblingDetectionRule";

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * @param env spring environment.
     */
    public SiblingDetectionRule() {
        super();
    }

    @PostConstruct
    public final void initalize() {
        engine.setRule(NAME, this);
    }
    @PreDestroy
    public final void destroy() {
        engine.setRule(NAME, null);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent event = context.getEvent();
        final Shipment shipment = event.getShipment();

        return shipment != null
                && !context.isProcessed(this)
                && !shipment.hasFinalStatus();
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);
        return updateSiblings(context.getEvent().getShipment());
    }

    /**
     * @param master
     * @return
     */
    private boolean updateSiblings(final Shipment master) {
        log.debug("Start search of siblings for shipment " + master.getId());

        final List<Shipment> shipments = new LinkedList<>(findActiveShipments(master.getCompany()));

        //detect siblings.
        final Map<Long, Set<Long>> siblingMap = new HashMap<>();
        //initialize sibling map
        for (final Shipment s : shipments) {
            siblingMap.put(s.getId(), new HashSet<Long>(s.getSiblings()));
        }

        //remove self
        final Iterator<Shipment> iter = shipments.iterator();
        while (iter.hasNext()) {
            if (master.getId().equals(iter.next().getId())) {
                iter.remove();
                break;
            }
        }

        if (!shipments.isEmpty()) {
            //get shipments for group
            findSiblings(master, shipments, siblingMap);

            final Set<Long> newSiblings = getNewSiblings(
                    master.getSiblings(), siblingMap.get(master.getId()));
            if (!newSiblings.isEmpty()) {
                log.debug("Uplate siblings (" + StringUtils.combine(siblingMap.get(master.getId()), ",")
                + ") for shipment " + master.getId());
                updateSiblingInfo(master, siblingMap.get(master.getId()));

                //update sibling info also for new found sblings
                for (final Long id : newSiblings) {
                    final Shipment sibling = EntityUtils.getEntity(shipments, id);
                    final Set<Long> set = new HashSet<>(siblingMap.get(id));
                    set.add(master.getId());

                    log.debug("Sibling info has updated also for new found sibling " + id);
                    updateSiblingInfo(sibling, set);
                }
                return true;
            }
        }

        log.debug("End search of siblings for shipment " + master.getId());
        return false;
    }

    /**
     * @param master
     * @param originShipments
     * @param siblingMap
     */
    protected void findSiblings(final Shipment master, final List<Shipment> originShipments,
            final Map<Long, Set<Long>> siblingMap) {
        final List<Shipment> shipments = new LinkedList<>(originShipments);

        //ignore old siblings
        final Set<Long> siblings = master.getSiblings();
        final Iterator<Shipment> iter = shipments.iterator();
        while (iter.hasNext()) {
            if (siblings.contains(iter.next())) {
                iter.remove();
            }
        }

        //continue only if shipment list is not empty
        if (!shipments.isEmpty()) {
            log.debug("Fetch readings for master shipment " + master.getId());
            final List<TrackerEvent> masterEvents = getTrackeEvents(master);
            for (final Shipment s : shipments) {
                final List<TrackerEvent> events = getTrackeEvents(s);
                if (isSiblings(masterEvents, events)) {
                    siblingMap.get(master.getId()).add(s.getId());
                    siblingMap.get(s.getId()).add(master.getId());
                }
            }
        }
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
    private Set<Long> getNewSiblings(
            final Set<Long> oldSiblings, final Set<Long> newSiblings) {
        final Set<Long> result = new HashSet<>();
        for (final Long id : newSiblings) {
            if (!oldSiblings.contains(id)) {
                result.add(id);
            }
        }
        return result;
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
        if (originE1.isEmpty()) {
            return false;
        }

        final List<TrackerEvent> e1 = new LinkedList<>(originE1);
        final List<TrackerEvent> e2 = new LinkedList<>(originE2);

        //1. ignore events before first
        removeEventsBeforeDate(e2, e1.get(0).getTime());
        if (e2.isEmpty()) {
            return false;
        }

        //get events of given tracker after the intersecting time
        cutEventsAfterDate(e1, e2.get(e2.size() - 1).getTime());

        final boolean isSiblings = isSiblingsByDistance(e1, e2, MAX_DISTANCE_AVERAGE);

        //check given tracker lives the sibling area
        return isSiblings
                && isPathNotLessThen(e1, MIN_PATH)
                && isPathNotLessThen(e2, MIN_PATH);
    }

    /**
     * @param events
     * @param minPath
     * @return
     */
    protected boolean isPathNotLessThen(final List<TrackerEvent> events, final int minPath) {
        if (events.size() == 0) {
            return false;
        }

        final LinkedList<TrackerEvent> list = new LinkedList<>(events);
        TrackerEvent e;
        while (list.size() > 0) {
            e = list.remove(0);

            final Iterator<TrackerEvent> iter = list.descendingIterator();
            while (iter.hasNext()) {
                if (getDistance(e, iter.next()) >= minPath) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param e1 first list of events
     * @param e2 secondd list of events
     * @param maxAvg max acceptable average distance.
     * @return
     */
    private boolean isSiblingsByDistance(
            final List<TrackerEvent> e1, final List<TrackerEvent> e2, final double maxAvg) {
        //calculate the distance in intersected time
        int count = 0;
        final double maxSumm = maxAvg * e1.size(); // max acceptable summ
        double summ = 0;

        final Iterator<TrackerEvent> iter1 = e1.iterator();
        final Iterator<TrackerEvent> iter2 = e2.iterator();

        TrackerEvent before = iter2.next();
        while (iter1.hasNext()) {
            final TrackerEvent e = iter1.next();

            if (!e.getTime().before(before.getTime())) {
                while (iter2.hasNext()) {
                    final TrackerEvent after = iter2.next();
                    if (!after.getTime().before(e.getTime())) {
                        summ += getDistance(e, before, after);
                        count++;

                        //if max possible avg is more than avg limit, should return the max distance
                        //as for not siblings
                        if (summ >= maxSumm) {
                            return false;
                        }
                        before = after;
                        break;
                    }
                    if (!after.getTime().before(e.getTime())) {
                        before = after;
                    }
                }
            }
        }

        if (count == 0) {
            return false;
        }

        return summ / count < maxAvg;
    }

    /**
     * @param e
     * @param startDate
     */
    private void removeEventsBeforeDate(final List<TrackerEvent> e,
            final Date startDate) {
        final Iterator<TrackerEvent> iter = e.iterator();
        while (iter.hasNext()) {
            if (iter.next().getTime().before(startDate)) {
                iter.remove();
            } else {
                break;
            }
        }
    }

    /**
     * @param events
     * @param time
     * @return
     */
    private List<TrackerEvent> cutEventsAfterDate(final List<TrackerEvent> events,
            final Date time) {
        final List<TrackerEvent> list = new LinkedList<>();
        final Iterator<TrackerEvent> iter = events.iterator();
        while (iter.hasNext()) {
            final TrackerEvent e = iter.next();
            if (e.getTime().after(time)) {
                iter.remove();
                list.add(e);
            }
        }

        return list;
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
        final long dt = mt2.getTime() - mt1.getTime();

        final double lat;
        final double lon;

        if (dt == 0) {
            lat = me1.getLatitude();
            lon = me1.getLongitude();
        } else {
            final double delta = (double) (t.getTime() - mt1.getTime()) / dt;

            lat = me1.getLatitude() + delta * (me2.getLatitude() - me1.getLatitude());
            lon = me1.getLongitude() + delta * (me2.getLongitude() - me1.getLongitude());
        }

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
