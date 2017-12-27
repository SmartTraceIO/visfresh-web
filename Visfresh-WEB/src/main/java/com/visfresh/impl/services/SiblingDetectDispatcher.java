/**
 *
 */
package com.visfresh.impl.services;

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
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.services.AbstractAssyncSystemMessageDispatcher;
import com.visfresh.services.GroupLockService;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SiblingDetectionService;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.EntityUtils;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SiblingDetectDispatcher extends AbstractAssyncSystemMessageDispatcher
    implements SiblingDetectionService, SystemMessageHandler {
    protected static String GROUP_PREFIX = "shp-";
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SiblingDetectDispatcher.class);

    /**
     * Minimal path for siblings.
     */
    protected static final int MIN_PATH = 25000; // meters
    protected static final double MAX_DISTANCE_AVERAGE = 3000; //meters

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private GroupLockService locker;

    /**
     * Processor ID.
     */
    protected String processorId;
    private static int numInstances;

    /**
     * @param env spring environment.
     */
    @Autowired
    public SiblingDetectDispatcher(final Environment env) {
        super(SystemMessageType.Siblings);
        processorId = buildInstanceId(env, "siblings.dispatcher.id", "siblings") + "-" + (numInstances++);
        setBatchLimit(Integer.parseInt(env.getProperty("siblings.dispatcher.batchLimit", "10")));
        setRetryLimit(Integer.parseInt(env.getProperty("siblings.dispatcher.retryLimit", "1")));
        setNumThreads(Integer.parseInt(env.getProperty("main.dispatcher.numThreads", "2")));
        setInactiveTimeOut(Long.parseLong(env.getProperty("siblings.dispatcher.retryTimeOut", "3000")));
    }
    /**
     * For unit tests.
     */
    protected SiblingDetectDispatcher() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageDispatcher#getProcessorId()
     */
    @Override
    protected String getProcessorId() {
        return processorId;
    }

    @Scheduled(fixedDelay = 15000l)
    public void processScheduledSiblingDetections() {
        if (isIgnoreSchedulings()) {
            return;
        }

        int processed;
        while ((processed = processMessages(getProcessorId())) > 0) {
            log.debug("Processed " + processed + " sibling detection schedules");
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#stop()
     */
    @Override
    @PreDestroy
    public void stop() {
        super.setSystemMessageHandler(SystemMessageType.Siblings, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#start()
     */
    @Override
    @PostConstruct
    public void start() {
        //disable default thread
        super.setSystemMessageHandler(SystemMessageType.Siblings, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final String group = msg.getGroup();
        //allow to send new sibling detection requests for given device.
        unlockGroup(group);

        String shipment;
        if (group.startsWith(GROUP_PREFIX)) {
            //new schema with 'shp-' prefix.
            shipment = group.substring(GROUP_PREFIX.length());
        } else {
            //old schema should be supported to
            shipment = group;
        }
        updateSiblings(Long.valueOf(shipment), Long.valueOf(msg.getMessageInfo()));
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SiblingDetectionService#scheduleSiblingDetection(com.visfresh.entities.Shipment)
     */
    @Override
    public void scheduleSiblingDetection(final Shipment s, final Date retryOn) {
        final String group = createGroupId(s);
        if (lockGroup(group, retryOn)) {
            log.debug("New sibling detection has scheduled for shipment " + s.getId());
            final SystemMessage sm = new SystemMessage();
            sm.setType(SystemMessageType.Siblings);
            sm.setMessageInfo(s.getCompany().getId().toString());
            sm.setTime(new Date());
            sm.setRetryOn(retryOn);
            sm.setGroup(group);

            saveMessage(sm);
        } else {
            log.debug("Sibling detection is already scheduled for shipment " + s.getId() + ", now ignored");
        }
    }
    /**
     * @param group
     * @return
     */
    protected boolean lockGroup(final String group, final Date retryOn) {
        if (locker.lockGroup(group, "any")) {
            locker.setUnlockOn(group, "any", new Date(retryOn.getTime() + 30000l));
            return true;
        }
        return false;
    }
    /**
     * @param group group to unlock.
     */
    protected void unlockGroup(final String group) {
        locker.unlock(group, "any");
    }
    /**
     * @param s shipment;
     * @return group ID for given shipment.
     */
    private String createGroupId(final Shipment s) {
        return GROUP_PREFIX + s.getId().toString();
    }
    /**
     * Given method is for overriding in junit service implementation.
     * @return whether or not should ignore schedules.
     */
    protected boolean isIgnoreSchedulings() {
        return false;
    }

    //sibling detection
    /**
     * @param master
     * @return
     */
    public boolean updateSiblings(final Long masterId, final Long companyId) {
        log.debug("Start search of siblings for shipment " + masterId);

        final List<ShipmentSiblingInfo> shipments = new LinkedList<>(findActiveShipments(companyId));

        //detect siblings.
        final Map<Long, Set<Long>> siblingMap = new HashMap<>();
        //initialize sibling map
        for (final ShipmentSiblingInfo s : shipments) {
            siblingMap.put(s.getId(), new HashSet<Long>(s.getSiblings()));
        }

        //remove self
        final Iterator<ShipmentSiblingInfo> iter = shipments.iterator();
        while (iter.hasNext()) {
            final ShipmentSiblingInfo next = iter.next();
            if (masterId.equals(next.getId())) {
                iter.remove();
                break;
            }
        }

        if (!shipments.isEmpty()) {
            //fetch master
            final ShipmentSiblingInfo master = findShipment(masterId);
            siblingMap.put(master.getId(), new HashSet<Long>(master.getSiblings()));

            //get shipments for group
            findSiblings(master, shipments, siblingMap);

            final Set<Long> newSiblings = getNewSiblings(
                    master.getSiblings(), siblingMap.get(master.getId()));
            if (!newSiblings.isEmpty()) {
                log.debug("Uplate siblings (" + StringUtils.combine(siblingMap.get(master.getId()), ",")
                + ") for shipment " + master.getId());
                updateSiblingInfo(master, siblingMap.get(master.getId()));

                //update sibling info also for new found siblings
                for (final Long id : newSiblings) {
                    final ShipmentSiblingInfo sibling = EntityUtils.getEntity(shipments, id);
                    final Set<Long> set = new HashSet<>(siblingMap.get(id));
                    set.add(master.getId());

                    log.debug("Sibling info has updated also for new found sibling " + id);
                    updateSiblingInfo(sibling, set);
                }
                return true;
            } else {
                log.debug("Sibling list is not changed for shipment " + master.getId());
            }
        }

        log.debug("End search of siblings for shipment " + masterId);
        return false;
    }
    /**
     * @param masterId
     * @return
     */
    protected ShipmentSiblingInfo findShipment(final Long masterId) {
        return shipmentDao.getShipmentSiblingInfo(masterId);
    }

    /**
     * @param master
     * @param originShipments
     * @param siblingMap
     */
    protected void findSiblings(final ShipmentSiblingInfo master, final List<ShipmentSiblingInfo> originShipments,
            final Map<Long, Set<Long>> siblingMap) {
        final List<ShipmentSiblingInfo> shipments = new LinkedList<>(originShipments);

        //ignore old siblings
        final Set<Long> siblings = master.getSiblings();
        final Iterator<ShipmentSiblingInfo> iter = shipments.iterator();
        while (iter.hasNext()) {
            if (siblings.contains(iter.next().getId())) {
                iter.remove();
            }
        }

        //continue only if shipment list is not empty
        if (!shipments.isEmpty()) {
            log.debug("Fetch readings for master shipment " + master.getId());
            final List<TrackerEventDto> masterEvents = getTrackeEvents(master.getId());
            for (final ShipmentSiblingInfo s : shipments) {
                final List<TrackerEventDto> events = getTrackeEvents(s.getId());
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
    protected void updateSiblingInfo(final ShipmentSiblingInfo master, final Set<Long> set) {
        master.getSiblings().clear();
        master.getSiblings().addAll(set);
        shipmentDao.updateSiblingInfo(master.getId(), set);
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
    protected List<ShipmentSiblingInfo> findActiveShipments(final Long company) {
        return shipmentDao.findActiveShipments(company);
    }
    /**
     * @param shipment shipment.
     * @return array of tracker events.
     */
    protected List<TrackerEventDto> getTrackeEvents(final Long shipment) {
        final List<TrackerEventDto> list = getLocationsFromDb(shipment);

        //filter events without locations.
        final Iterator<TrackerEventDto> iter = list.iterator();
        while (iter.hasNext()) {
            final TrackerEventDto e = iter.next();
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
    protected List<TrackerEventDto> getLocationsFromDb(final Long shipment) {
        final List<Long> ids = new LinkedList<>();
        ids.add(shipment);
        final Map<Long, List<TrackerEventDto>> eventsForShipmentIds = trackerEventDao.getEventsForShipmentIds(ids);

        final List<TrackerEventDto> result = eventsForShipmentIds.get(shipment);
        return result == null ? new LinkedList<>() : result;
    }
    /**
     * @param originE1
     * @param originE2
     * @return true if siblings.
     */
    protected boolean isSiblings(final List<TrackerEventDto> originE1, final List<TrackerEventDto> originE2) {
        if (originE1.isEmpty()) {
            return false;
        }

        final List<TrackerEventDto> e1 = new LinkedList<>(originE1);
        final List<TrackerEventDto> e2 = new LinkedList<>(originE2);

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
    protected boolean isPathNotLessThen(final List<TrackerEventDto> events, final int minPath) {
        if (events.size() == 0) {
            return false;
        }

        final LinkedList<TrackerEventDto> list = new LinkedList<>(events);
        TrackerEventDto e;
        while (list.size() > 0) {
            e = list.remove(0);

            final Iterator<TrackerEventDto> iter = list.descendingIterator();
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
            final List<TrackerEventDto> e1, final List<TrackerEventDto> e2, final double maxAvg) {
        //calculate the distance in intersected time
        int count = 0;
        final double maxSumm = maxAvg * e1.size(); // max acceptable summ
        double summ = 0;

        final Iterator<TrackerEventDto> iter1 = e1.iterator();
        final Iterator<TrackerEventDto> iter2 = e2.iterator();

        TrackerEventDto before = iter2.next();
        while (iter1.hasNext()) {
            final TrackerEventDto e = iter1.next();

            if (!e.getTime().before(before.getTime())) {
                while (iter2.hasNext()) {
                    final TrackerEventDto after = iter2.next();
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
    private void removeEventsBeforeDate(final List<TrackerEventDto> e,
            final Date startDate) {
        final Iterator<TrackerEventDto> iter = e.iterator();
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
    private List<TrackerEventDto> cutEventsAfterDate(final List<TrackerEventDto> events,
            final Date time) {
        final List<TrackerEventDto> list = new LinkedList<>();
        final Iterator<TrackerEventDto> iter = events.iterator();
        while (iter.hasNext()) {
            final TrackerEventDto e = iter.next();
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
    private double getDistance(final TrackerEventDto e, final TrackerEventDto me1,
            final TrackerEventDto me2) {
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
    private double getDistance(final TrackerEventDto e1, final TrackerEventDto e2) {
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
