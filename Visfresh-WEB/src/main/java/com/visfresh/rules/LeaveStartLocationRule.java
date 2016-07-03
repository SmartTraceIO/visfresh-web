/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LeaveStartLocationRule implements TrackerEventRule {
    private static final String IS_STARTED_WATCH_LIVING_START_LOCATION = "isStartedWatchLivingStartLocation";
    private static final String IS_SET_LEAVING_START_LOCATION = "isSetLeavingStartLocation";

    protected static final long CHECK_SHUTDOWN_TIMEOUT = 60 * 60 * 1000L;
    public static final String NAME = "LeaveStartLocation";
    private static final Logger log = LoggerFactory.getLogger(LeaveStartLocationRule.class);
    protected static final int CONTROL_DISTANCE = 5000; //meters

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * Default constructor.
     */
    public LeaveStartLocationRule() {
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
        if (e.getShipment() == null) {
            return false;
        }

        final ShipmentSession session = context.getSessionManager().getSession(
                e.getShipment());
        //already leave start location.
        if (isSetLeaving(session)) {
            return false;
        }
        //this is for old shipments
        if (!isStartWatch(session)) {
            return true;
        }

        return isOutsideStartLocation(e);
    }
    /**
     * @param session
     * @return
     */
    private static boolean isStartWatch(final ShipmentSession session) {
        return "true".equals(
                session.getShipmentProperty(IS_STARTED_WATCH_LIVING_START_LOCATION));
    }
    /**
     * @param session
     * @return
     */
    public static boolean isSetLeaving(final ShipmentSession session) {
        return "true".equals(
                session.getShipmentProperty(IS_SET_LEAVING_START_LOCATION));
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final Shipment s = context.getEvent().getShipment();
        final ShipmentSession session = context.getSessionManager().getSession(
                s);
        if (!isStartWatch(session)) {
            session.setShipmentProperty(IS_STARTED_WATCH_LIVING_START_LOCATION,
                    "true");
        }

        //check living
        if(isOutsideStartLocation(context.getEvent())) {
            session.setShipmentProperty(IS_SET_LEAVING_START_LOCATION, "true");
            log.debug("The shipment " + s.getId() + " has leaving the start location");
            return true;
        }

        return false;
    }

    /**
     * @param s shipment.
     * @return
     */
    protected boolean isOutsideStartLocation(final TrackerEvent e) {
        final LocationProfile start = e.getShipment().getShippedFrom();
        if (start == null || e.getLatitude() == null || e.getLongitude() == 0) {
            return false;
        }

        final int distance = (int) Math.round(LocationUtils.getDistanceMeters(
                e.getLatitude(),
                e.getLongitude(),
                start.getLocation().getLatitude(),
                start.getLocation().getLongitude()));

        //if inside of location radius.
        if (distance > start.getRadius() + CONTROL_DISTANCE) {
            return true;
        }

        return false;
    }
}
