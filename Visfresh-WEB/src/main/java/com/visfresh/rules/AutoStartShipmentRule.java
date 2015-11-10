/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.controllers.TrackerEventConstants;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AutoStartShipmentRule implements TrackerEventRule {
    /**
     * Rule name.
     */
    public static final String NAME = "AutoStartShipment";
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private AbstractRuleEngine engine;
    /**
     * Inactive time out.
     */
    private long inactiveTimeOut = 30 * 60 * 1000L;
    private int autostartDistance = 200; //200 meters

    /**
     * Default constructor.
     */
    public AutoStartShipmentRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        if(e.getShipment() == null && !context.isProcessed(this)) {
            final List<TrackerEvent> events = trackerEventDao.findAll(
                    null,
                    new Sorting(false, TrackerEventConstants.PROPERTY_ID),
                    new Page(1, 2));

            //first event
            if (events.size() == 1) {
                return true;
            }

            //if activity after long inactivity return true.
            final TrackerEvent e1 = events.get(0);
            final TrackerEvent e2= events.get(1);

            if (Math.abs(e1.getTime().getTime() - e2.getTime().getTime()) > getInactiveTimeOut()) {
                return true;
            }

            //check device motion
            final int radius = (int) Math.round(LocationUtils.distFrom(
                    e2.getLatitude(), e2.getLongitude(),
                    e1.getLatitude(), e1.getLongitude()));
            if (radius > getAutostartDistance()) {
                return true;
            }

            //TODO check may be device moving too slow
            //find all event without shipment from now to past
            //and check location is changed
        }

        return false;
    }
    /**
     * @return autostart distance.
     */
    public int getAutostartDistance() {
        return autostartDistance;
    }
    /**
     * @param autostartDistance the autostartDistance to set
     */
    public void setAutostartDistance(final int autostartDistance) {
        this.autostartDistance = autostartDistance;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final TrackerEvent event = context.getEvent();
        final Device device = event.getDevice();

        final Shipment s = new Shipment();
        s.setCompany(device.getCompany());
        s.setDevice(device);
        s.setShipmentDescription("Autocreated by autostart shipment rule");
        s.setShipmentDate(new Date());

        shipmentDao.save(s);
        event.setShipment(s);
        trackerEventDao.save(event);

        return true;
    }
    /**
     * @return the inactiveTimeOut
     */
    public long getInactiveTimeOut() {
        return inactiveTimeOut;
    }
    /**
     * @param inactiveTimeOut the inactiveTimeOut to set
     */
    public void setInactiveTimeOut(final long inactiveTimeOut) {
        this.inactiveTimeOut = inactiveTimeOut;
    }
}
