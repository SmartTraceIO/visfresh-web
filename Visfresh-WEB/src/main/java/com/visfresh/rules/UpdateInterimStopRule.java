/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UpdateInterimStopRule extends AbstractInterimStopRule {
    private static final Logger log = LoggerFactory.getLogger(UpdateInterimStopRule.class);

    public static final String NAME = "UpdateInterimStop";

    private static final long MINUTE = 60 * 1000l;

    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Default constructor.
     */
    public UpdateInterimStopRule() {
        super();
    }

    @PostConstruct
    public final void initalize() {
        engine.setRule(getName(), this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext req) {
        if (super.accept(req)) {
            final TrackerEvent event = req.getEvent();
            final Shipment shipment = event.getShipment();
            final ShipmentSession session = shipment == null ? null : req.getSessionManager().getSession(shipment);

            final InterimStopInfo info = getInterimStop(session);
            if (info != null && info.getId() != null) {
                final InterimStop stp = getInterimStop(shipment, info);
                return LocationUtils.isNearLocation(stp.getLocation(),
                        new Location(event.getLatitude(), event.getLongitude()));
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final TrackerEvent event = context.getEvent();
        final ShipmentSession state = context.getSessionManager().getSession(event.getShipment());

        final InterimStopInfo stop = getInterimStop(state);

        //update stop time in DB
        final int minutes = (int) ((event.getTime().getTime() - stop.getStartTime()) / MINUTE);
        updateStopTime(stop, minutes);
        log.debug("Stop time for shipment " + event.getShipment().getId()
                + " has update to " + minutes + " min");

        setInterimStopState(state, stop);

        return false;
    }

    /**
     * @param stop interim stop info.
     * @param minutes stop time in minutes.
     */
    protected void updateStopTime(final InterimStopInfo stop, final int minutes) {
        interimStopDao.updateTime(stop.getId(), minutes);
    }

    public String getName() {
        return NAME;
    }
}
