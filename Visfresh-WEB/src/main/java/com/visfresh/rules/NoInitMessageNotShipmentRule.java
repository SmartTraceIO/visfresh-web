/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NoInitMessageNotShipmentRule extends AbstractNoInitMessageRule {
    public static final String NAME = "NoInitMessageNotShipment";
    private static final Logger log = LoggerFactory.getLogger(NoInitMessageNotShipmentRule.class);

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public NoInitMessageNotShipmentRule() {
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
        final boolean accept = e.getType() != TrackerEventType.INIT && e.getShipment() == null;
        if (accept) {
            final Shipment last = findLastShipment(e.getDevice().getImei());
            return last != null && last.hasFinalStatus() && last.getDeviceShutdownTime() != null;
        }

        return accept;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Device device = e.getDevice();

        final Shipment last = findLastShipment(device.getImei());
        final Shipment s = autoStartNewShipment(
                device, e.getLatitude(), e.getLongitude(), e.getTime());
        e.setShipment(s);
        saveEvent(e);

        final String msg = "Not INIT message found for device "
                + device.getImei()
                + ". But last shipment " + last.getId()
                + " was shutdown. New shipment " + s.getId() + " has autostarted";
        log.debug(msg);
        sendMessageToSupport("Unusual start", msg);
        return true;
    }
    /**
     * @param s
     */
    @Override
    protected void saveShipment(final Shipment s) {
        shipmentDao.save(s);
    }
    /**
     * @param imei device IMEI.
     * @return shipment.
     */
    protected Shipment findLastShipment(final String imei) {
        return shipmentDao.findLastShipment(imei);
    }
}
