/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NoInitMessageArrivedShipmentRule extends AbstractNoInitMessageRule {
    protected static final long CHECK_SHUTDOWN_TIMEOUT = 60 * 60 * 1000L;
    public static final String NAME = "NoInitMessageArrivedShipment";
    private static final Logger log = LoggerFactory.getLogger(NoInitMessageArrivedShipmentRule.class);

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * Default constructor.
     */
    public NoInitMessageArrivedShipmentRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Shipment s = e.getShipment();

        return e.getType() != TrackerEventType.INIT
                && s != null
                && s.hasFinalStatus()
                && s.getDeviceShutdownTime() != null
                && e.getTime().getTime() > s.getDeviceShutdownTime().getTime() + CHECK_SHUTDOWN_TIMEOUT;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Device device = e.getDevice();
        final List<TrackerEvent> events = getEventsAfterShutdownTimeOut(e);

        final Shipment s = autoStartNewShipment(
                device, e.getLatitude(), e.getLongitude(), e.getTime());
        e.setShipment(s);
        saveEvent(e);

        final String msg = "Not INIT message found for device "
                + device.getImei()
                + ". But current shipment " + e.getShipment().getId()
                + " was shutdown. New shipment " + s.getId() + " has autostarted";
        log.debug(msg);
        sendMessageToSupport("Unusual start", msg);

        //set null shipment to events after shutdown time out.
        for (final TrackerEvent evt : events) {
            if (evt.getShipment() != null) {
                evt.setShipment(null);
                saveEvent(evt);
                log.debug("Set null shipment to event " + evt.getId() + " because after shutdown time out");
            }
        }

        return true;
    }

    /**
     * @param e
     * @return
     */
    private List<TrackerEvent> getEventsAfterShutdownTimeOut(final TrackerEvent e) {
        final Shipment s = e.getShipment();
        final List<TrackerEvent> events = getEventsAfterDate(s,
                new Date(s.getDeviceShutdownTime().getTime() + CHECK_SHUTDOWN_TIMEOUT));

        //delete given event
        final Iterator<TrackerEvent> iter = events.iterator();
        while (iter.hasNext()) {
            if (iter.next().getId().equals(e.getId())) {
                iter.remove();
                break;
            }
        }

        return events;
    }

    /**
     * @param s
     * @param date
     * @return
     */
    protected List<TrackerEvent> getEventsAfterDate(final Shipment s,
            final Date date) {
        return trackerEventDao.getEventsAfterDate(s, date);
    }

    /**
     * @param evt
     */
    protected void saveEvent(final TrackerEvent evt) {
        trackerEventDao.save(evt);
    }
}
