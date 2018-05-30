/**
 *
 */
package com.visfresh.impl.services;

import java.util.Collection;
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

import com.visfresh.dao.Page;
import com.visfresh.dao.PartionedDataIterator;
import com.visfresh.dao.PartionedDataIterator.DataProvider;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.impl.siblingdetect.CalculationDirection;
import com.visfresh.impl.siblingdetect.SiblingDetector;
import com.visfresh.impl.siblingdetect.StatefullSiblingDetector.State;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.services.GroupLockService;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SiblingDetectionService;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.EntityUtils;
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
    private static final int DATA_FETCH_LIMIT = 1000;

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private GroupLockService locker;

    /**
     * Processor ID.
     */
    protected String dispatcherAlias;
    private final CalculationDirection direction = CalculationDirection.RightToLeft;
    private SiblingDetector detector = new SiblingDetector(direction);

    /**
     * @param env spring environment.
     */
    @Autowired
    public SiblingDetectDispatcher(final Environment env) {
        super(SystemMessageType.Siblings);
        dispatcherAlias = env.getProperty("siblings.dispatcher.id", "siblings");
        setBatchLimit(Integer.parseInt(env.getProperty("siblings.dispatcher.batchLimit", "10")));
        setRetryLimit(Integer.parseInt(env.getProperty("siblings.dispatcher.retryLimit", "1")));
        setNumThreads(Integer.parseInt(env.getProperty("siblings.dispatcher.numThreads", "2")));
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
    protected String getBaseProcessorId() {
        return getInstanceId() + "." + dispatcherAlias;
    }
    @Scheduled(fixedDelay = 15000l)
    public void processScheduledSiblingDetections() {
        if (isIgnoreSchedulings()) {
            return;
        }

        int processed;
        while ((processed = processMessages(getBaseProcessorId())) > 0) {
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
            sm.setMessageInfo(s.getCompanyId().toString());
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

        //create shipment siblings map.
        final Map<Long, Set<Long>> siblingMap = new HashMap<>();
        //initialize sibling map
        for (final ShipmentSiblingInfo s : shipments) {
            siblingMap.put(s.getId(), new HashSet<Long>(s.getSiblings()));
        }

        ShipmentSiblingInfo master = null;
        //remove self
        final Iterator<ShipmentSiblingInfo> iter = shipments.iterator();
        while (iter.hasNext()) {
            final ShipmentSiblingInfo next = iter.next();
            if (masterId.equals(next.getId())) {
                master = next;
                iter.remove();
                break;
            }
        }

        if (master == null) {
            log.warn("Master shipment " + masterId
                    + " not found or inactive at given moment");
            return false;
        }

        final Set<Long> oldSiblings = new HashSet<>(master.getSiblings());

        boolean updated = false;
        if (!shipments.isEmpty()) {
            updated = searchNewSiblings(master, shipments, siblingMap);
        }
        if (!oldSiblings.isEmpty()) {
            final boolean u = unsiblify(master, oldSiblings, shipments, siblingMap);
            updated = updated || u;
        }

        if (!updated) {
            log.debug("Siblng list not changed for shipment " + masterId);
        }
        return updated;
    }
    /**
     * @param master
     * @param oldSiblings
     * @param shipments
     * @param siblingMap
     * @return
     */
    private boolean unsiblify(final ShipmentSiblingInfo master, final Set<Long> oldSiblings, final List<ShipmentSiblingInfo> shipments,
            final Map<Long, Set<Long>> siblingMap) {
        if (!oldSiblings.isEmpty()) {
            log.debug("Search stopped to be siblings in ("
                    + StringUtils.combine(master.getSiblings(), ",")
                    + ") for shipment " + master.getId());

            int numUnsiblified = 0;
            for (final Long id : master.getSiblings()) {
                //get shipment indo
                ShipmentSiblingInfo sibling = EntityUtils.getEntity(shipments, id);
                if (sibling == null) {
                    sibling = findShipment(id);
                }

                if (!siblingMap.containsKey(sibling.getId())) {
                    siblingMap.put(id, sibling.getSiblings());
                }

                //check is sibling
                final Iterator<TrackerEventDto> masterEvents = getTrackeEvents(master.getId(), direction);
                final Iterator<TrackerEventDto> events = getTrackeEvents(sibling.getId(), direction);
                if (detector.detectSiblingsState(masterEvents, events) == State.NotSiblings) {
                    siblingMap.get(master.getId()).remove(sibling.getId());
                    siblingMap.get(sibling.getId()).remove(master.getId());
                    updateSiblingInfo(sibling, siblingMap.get(sibling.getId()));

                    numUnsiblified++;
                }
            }

            if (numUnsiblified > 0) {
                updateSiblingInfo(master, siblingMap.get(master.getId()));
                log.debug("Unsiblified " + numUnsiblified + " siblings from shipment "
                        + master.getId());
                return true;
            }
        }
        return false;
    }
    /**
     * @param master
     * @param shipments
     * @param siblingMap
     * @return
     */
    private boolean searchNewSiblings(final ShipmentSiblingInfo master,
            final List<ShipmentSiblingInfo> shipments,
            final Map<Long, Set<Long>> siblingMap) {
        //get shipments for group
        findSiblings(master, shipments, siblingMap);

        final Set<Long> newSiblings = getNewSiblings(
                master.getSiblings(), siblingMap.get(master.getId()));
        if (!newSiblings.isEmpty()) {
            log.debug("Search new siblings for ("
                + StringUtils.combine(siblingMap.get(master.getId()), ",")
                + ") of shipment " + master.getId());
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
            log.debug("New siblings not found for shipment " + master.getId());
        }

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
    private void findSiblings(final ShipmentSiblingInfo master, final List<ShipmentSiblingInfo> originShipments,
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
            for (final ShipmentSiblingInfo s : shipments) {
                final Iterator<TrackerEventDto> masterEvents = getTrackeEvents(master.getId(), direction);
                final Iterator<TrackerEventDto> events = getTrackeEvents(s.getId(), direction);

                if (detector.detectSiblingsState(masterEvents, events) == State.Siblings) {
                    siblingMap.get(master.getId()).add(s.getId());
                    siblingMap.get(s.getId()).add(master.getId());
                }
            }
        }
    }
    /**
     * @param shipment
     * @param set
     */
    protected void updateSiblingInfo(final ShipmentSiblingInfo shipment, final Set<Long> set) {
        shipment.getSiblings().clear();
        shipment.getSiblings().addAll(set);
        shipmentDao.updateSiblingInfo(shipment.getId(), set);
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
     * @param direction calculation direction
     * @return array of tracker events.
     */
    protected Iterator<TrackerEventDto> getTrackeEvents(final Long shipment, final CalculationDirection direction) {
        final DataProvider<TrackerEventDto> provider = new DataProvider<TrackerEventDto>() {
            /* (non-Javadoc)
             * @see com.visfresh.dao.PartionedDataIterator.DataProvider#getNextPart(int, int)
             */
            @Override
            public Collection<TrackerEventDto> getNextPart(final int page, final int limit) {
                final Page p = new Page(page, limit);
                return trackerEventDao.getOrderedByTimeNotNullLocations(
                        shipment, p, direction == CalculationDirection.LeftToRight);
            }
        };
        return new PartionedDataIterator<>(provider, DATA_FETCH_LIMIT);
    }
}
