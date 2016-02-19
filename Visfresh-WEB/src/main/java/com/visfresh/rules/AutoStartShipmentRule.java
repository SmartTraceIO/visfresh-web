/**
 *
 */
package com.visfresh.rules;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.services.ArrivalEstimationService;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AutoStartShipmentRule implements TrackerEventRule {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AutoStartShipmentRule.class);
    /**
     * Rule name.
     */
    public static final String NAME = "AutoStartShipment";
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AutoStartShipmentDao autoStartShipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ArrivalEstimationService estimationService;

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
        if (context.isProcessed(this)) {
            return false;
        }

        //check init message.
        final TrackerEvent e = context.getEvent();
        if(e.getShipment() == null || e.getType() == TrackerEventType.INIT) {
            return true;
        }

        //check shipment is null

        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);

        log.debug("New INIT event occurred");

        final TrackerEvent event = context.getEvent();
        final Device device = event.getDevice();
        final Shipment last = shipmentDao.findLastShipment(device.getImei());

        Shipment shipment;

        //first of all attempt to select autostart shipment template
        final List<AutoStartShipment> autoStarts = autoStartShipmentDao.findByCompany(
                device.getCompany(), null, null, null);
        if (!autoStarts.isEmpty()) {
            shipment = findByStartLocation(
                    autoStarts, event.getLatitude(), event.getLongitude(), device);
            //if not found, create new shipment from most priority template
            if (shipment == null) {
                shipment = createNewShipment(autoStarts.get(0).getTemplate(), null, device);
            }
        } else {
            log.debug("Create new shipment for device " + device.getImei());
            shipment = startNewShipment(device);
        }

        //close old shipment if need
        if (last != null && !last.hasFinalStatus()) {
            log.debug("Close old active shipment " + last.getShipmentDescription()
                    + " for device " + device.getImei());
            closeOldShipment(last);
        }

        context.getState().possibleNewShipment(shipment);
        event.setShipment(shipment);
        trackerEventDao.save(event);

        return true;
    }

    /**
     * @param autoStarts list of autostart templates.
     * @param latitude latitude.
     * @param longitude longitude.
     * @param device device.
     * @return shipment.
     */
    private Shipment findByStartLocation(final List<AutoStartShipment> autoStarts,
            final double latitude, final double longitude, final Device device) {
        Collections.sort(autoStarts);
        for (final AutoStartShipment auto : autoStarts) {
            for (final LocationProfile loc : auto.getShippedFrom()) {
                int distance = (int) LocationUtils.getDistanceMeters(
                        loc.getLocation().getLatitude(),
                        loc.getLocation().getLongitude(),
                        latitude,
                        longitude);
                distance = Math.max(0, distance - loc.getRadius());
                if (distance == 0) {
                    return createNewShipment(auto.getTemplate(), loc, device);
                }
            }
        }

        return null;
    }

    /**
     * @param tpl template
     * @param startLocation start location.
     * @param device device
     * @return
     */
    private Shipment createNewShipment(final ShipmentTemplate tpl,
            final LocationProfile startLocation, final Device device) {
        final Shipment s = shipmentDao.createNewFrom(tpl);
        s.setStatus(ShipmentStatus.InProgress);
        s.setDevice(device);
        s.setShippedFrom(startLocation);
        s.setShipmentDate(new Date());

        if (tpl.getShipmentDescription() != null) {
            s.setShipmentDescription(tpl.getShipmentDescription());
        } else if (tpl.getName() != null) {
            s.setShipmentDescription("Auto created from '" + tpl.getName() + "'");
        } else {
            s.setShipmentDescription("Created by autostart shipment rule");
        }
        return shipmentDao.save(s);
    }

    /**
     * @param shipment
     */
    private void closeOldShipment(final Shipment shipment) {
        shipment.setStatus(ShipmentStatus.Ended);
        shipmentDao.save(shipment);
    }

    /**
     * @param device
     */
    private Shipment startNewShipment(final Device device) {
        final Shipment s = new Shipment();
        s.setCompany(device.getCompany());
        s.setStatus(ShipmentStatus.Default);
        s.setDevice(device);
        s.setShipmentDescription("Created by autostart shipment rule");
        s.setShipmentDate(new Date());

        shipmentDao.save(s);
        return s;
    }
}
