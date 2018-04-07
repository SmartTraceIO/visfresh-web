/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Location;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.correctmoving.LastLocationInfo;
import com.visfresh.rules.correctmoving.LastLocationInfoParser;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CorrectMovingControllRule implements TrackerEventRule {
    protected static final long TIME_OUT = 30 * 60 * 1000l;
    protected static final int DISTANCE = 200000;
    private static final String LAST_LOCATION_INFO = "lastLocationInfo";
    private static final Logger log = LoggerFactory.getLogger(CorrectMovingControllRule.class);

    /**
     * Rule name.
     */
    public static final String NAME = "CorrectMovingControll";
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Default constructor.
     */
    public CorrectMovingControllRule() {
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
        if (context.isProcessed(this) || e.getLatitude() == null || e.getLongitude() == null) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final DeviceState state = context.getDeviceState();
        final TrackerEvent e = context.getEvent();
        boolean shouldClearLocation = false;

        //check correct moving
        LastLocationInfo info = getLastLocationInfo(state);
        if (e.getType() != TrackerEventType.INIT && info != null) {
            final Location loc = info.getLastLocation();
            final int meters = (int) LocationUtils.getDistanceMeters(loc.getLatitude(), loc.getLongitude(),
                    e.getLatitude(), e.getLongitude());

            final long dt = e.getTime().getTime() - info.getLastReadTime().getTime();
            if (meters > DISTANCE && dt < TIME_OUT) {
                log.warn("Incorrect device moving to " + meters + " meters has detected. Event has ignored");
                shouldClearLocation = true;
            }
        }

        info = new LastLocationInfo();
        info.setLastReadTime(e.getTime());
        info.setLastLocation(new Location(e.getLatitude(), e.getLongitude()));
        setLastLocationInfo(state, info);

        if (shouldClearLocation) {
            e.setLatitude(null);
            e.setLongitude(null);
            saveTrackerEvent(e);
        }

        return shouldClearLocation;
    }
    /**
     * @param e tracker event.
     */
    protected void saveTrackerEvent(final TrackerEvent e) {
        trackerEventDao.save(e);
    }
    /**
     * @param state device state.
     * @param info  last location info.
     */
    protected void setLastLocationInfo(
            final DeviceState state, final LastLocationInfo info) {
        state.setProperty(LAST_LOCATION_INFO,
                new LastLocationInfoParser().toJSon(info).toString());
    }
    /**
     * @param state device state.
     * @return
     */
    protected LastLocationInfo getLastLocationInfo(final DeviceState state) {
        final String str = state.getProperty(LAST_LOCATION_INFO);
        if (str == null) {
            return null;
        }

        final JsonObject json = SerializerUtils.parseJson(str).getAsJsonObject();
        return new LastLocationInfoParser().parseLastLocationInfo(json);
    }
}
