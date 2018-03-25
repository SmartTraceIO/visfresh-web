/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Location;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.LeaveLocationState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ClearAutoStartOnLeaveStartLocationStateRule implements TrackerEventRule {
    public static final String NAME = "ClearAutoStartOnLeaveStartLocationState";
    private static final Logger log = LoggerFactory.getLogger(ClearAutoStartOnLeaveStartLocationStateRule.class);

    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Default constructor.
     */
    public ClearAutoStartOnLeaveStartLocationStateRule() {
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
        final LeaveLocationState s = AutoStartOnLeaveStartLocationRule.getLeaveLocationState(
                context.getDeviceState(), e.getBeaconId());
        if (s == null) {
            return false;
        }

        if (e.getShipment() != null && !e.getShipment().hasFinalStatus()) {
            return true;
        }

        //check possible device returned to start location
        return s.getLeaveOn() != null && !AutoStartOnLeaveStartLocationRule.isOutsideLocation(
                e, new Location(s.getLatitude(), s.getLongitude()), s.getRadius());
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        log.debug("Clearing leave location state for device " + context.getEvent().getDevice().getImei());
        AutoStartOnLeaveStartLocationRule.clearLeaveLocationState(context.getDeviceState(),
                context.getEvent().getBeaconId());
        return false;
    }
}
