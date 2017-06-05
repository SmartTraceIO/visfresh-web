/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class InterimStopRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(InterimStopRule.class);

    public static final String NAME = "InterimStop";

    private static final long MINUTE = 60 * 1000l;

    @Autowired
    protected ArrivalDao arrivalDao;
    @Autowired
    protected InterimStopDao interimStopDao;
    @Autowired
    private AbstractRuleEngine engine;

    protected static class InterimStopInfo {
        private long startTime;
        private int numReadings = 0;
        private Long id; //id of reference to InterimStop in DB
        private double latitude;
        private double longitude;

        /**
         * @return the sartTime
         */
        public long getStartTime() {
            return startTime;
        }
        /**
         * @param sartTime the sartTime to set
         */
        public void setStartTime(final long sartTime) {
            this.startTime = sartTime;
        }
        /**
         * @return the numReadings
         */
        public int getNumReadings() {
            return numReadings;
        }
        /**
         * @param numReadings the numReadings to set
         */
        public void setNumReadings(final int numReadings) {
            this.numReadings = numReadings;
        }
        /**
         * @return the id
         */
        public Long getId() {
            return id;
        }
        /**
         * @param id the id to set
         */
        public void setId(final Long id) {
            this.id = id;
        }
        /**
         * @return the latitude
         */
        public double getLatitude() {
            return latitude;
        }
        /**
         * @param latitude the latitude to set
         */
        public void setLatitude(final double latitude) {
            this.latitude = latitude;
        }
        /**
         * @return the longitude
         */
        public double getLongitude() {
            return longitude;
        }
        /**
         * @param longitude the longitude to set
         */
        public void setLongitude(final double longitude) {
            this.longitude = longitude;
        }
    }

    /**
     * Default constructor.
     */
    public InterimStopRule() {
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
        final TrackerEvent event = req.getEvent();
        final Shipment shipment = event.getShipment();
        final ShipmentSession session = shipment == null ? null : req.getSessionManager().getSession(shipment);

        final boolean accept = shipment != null
                && !req.isProcessed(this)
                && !shipment.hasFinalStatus()
                && LeaveStartLocationRule.isLeavingStartLocation(shipment, session)
                && getInterimLocations(shipment) != null;

        if (accept) {
            if (isInInterimStop(session)) {
                return true;
            }

            if(isNearInterimStop(shipment, session, event.getLatitude(), event.getLongitude())) {
                //only one interim stop can be used in given version of API.
                return !hasInterimStops(shipment);
            } else {
                return false;
            }
        }

        return accept;
    }
    /**
     * @param shipment
     * @return
     */
    protected boolean hasInterimStops(final Shipment shipment) {
        return (interimStopDao.getByShipment(shipment).size() > 0);
    }
    private boolean isNearInterimStop(final Shipment shipment, final ShipmentSession state, final Double latitude,
            final Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

        final List<LocationProfile> locs = getInterimLocations(shipment);
        if (locs != null) {
            final LocationProfile p = getBestLocation(locs, latitude, longitude);
            return p != null;
        }
        return false;
    }

    /**
     * @param locs location list.
     * @param latitude latitude.
     * @param longitude longitude.
     * @return mathes location.
     */
    private static LocationProfile getBestLocation(final List<LocationProfile> locs,
            final double latitude, final double longitude) {
        int maxDistance = Integer.MAX_VALUE;
        LocationProfile best = null;

        for (final LocationProfile l : locs) {
            final int distance = (int) Math.round(LocationUtils.getDistanceMeters(
                    latitude, longitude, l.getLocation().getLatitude(), l.getLocation().getLongitude()));

            //if inside of location radius.
            if (Math.max(0, distance - l.getRadius()) == 0 && distance < maxDistance) {
                maxDistance = distance;
                best = l;
            }
        }
        return best;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final TrackerEvent event = context.getEvent();
        final ShipmentSession state = context.getSessionManager().getSession(event.getShipment());
        if (isNearInterimStop(event.getShipment(), state, event.getLatitude(), event.getLongitude())) {
            InterimStopInfo stop = getInterimStop(state);
            final List<LocationProfile> locs = getInterimLocations(event.getShipment());
            boolean shouldCreateStop = false;

            if (stop == null) {
                stop = new InterimStopInfo();
                stop.setLatitude(event.getLatitude());
                stop.setLongitude(event.getLongitude());
                stop.setStartTime(event.getTime().getTime());

                //if STP message type, then not need to wait next reading inside of location
                //just need save interim stop
                if (event.getType() == TrackerEventType.STP) {
                    shouldCreateStop = true;
                }
            } else if (stop.getId() == null){
                shouldCreateStop = true;
            } else {
                //update stop time in DB
                final int minutes = (int) ((event.getTime().getTime() - stop.getStartTime()) / MINUTE);
                updateStopTime(stop, minutes);
                log.debug("Stop time for shipment " + event.getShipment().getId()
                        + " has update to " + minutes + " min");
            }

            if (shouldCreateStop) {
                //update stop time
                final InterimStop s = new InterimStop();
                s.setLocation(getBestLocation(locs, event.getLatitude(), event.getLongitude()));
                s.setDate(new Date(stop.getStartTime()));
                s.setTime((int) ((event.getTime().getTime() - stop.getStartTime()) / MINUTE));

                log.debug("Interim stop detected near location " + s.getLocation().getId()
                        + " (" + s.getLocation().getName() + ")");
                final Long id = save(event.getShipment(), s);
                stop.setId(id);
            }

            setInterimStopState(state, stop);
        } else if (isInInterimStop(state)) {
            //remove interim stop
            log.debug("Interim stop of shipment " + event.getShipment().getId() + " has finished");
            state.setShipmentProperty(createInterimStopKey(), null);
        }

        return false;
    }

    /**
     * @param shipment
     * @return
     */
    protected List<LocationProfile> getInterimLocations(final Shipment shipment) {
        return engine.getInterimLocations(shipment);
    }

    /**
     * @param state interim stop state.
     * @param stop the interim stop info.
     */
    protected void setInterimStopState(final ShipmentSession state, final InterimStopInfo stop) {
        state.setShipmentProperty(createInterimStopKey(), toJson(stop).toString());
    }
    /**
     * @param shipment
     * @param stop
     * @return
     */
    protected Long save(final Shipment shipment, final InterimStop stop) {
        interimStopDao.save(shipment, stop);
        return stop.getId();
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

    /**
     * @param state
     * @return
     */
    private static boolean isInInterimStop(final ShipmentSession state) {
        return getInterimStop(state) != null;
    }
    /**
     * @param state
     * @return
     */
    protected static InterimStopInfo getInterimStop(final ShipmentSession state) {
        final String key = createInterimStopKey();
        final String info = state.getShipmentProperty(key);

        if (info != null) {
            return parseInterimStopInfo(SerializerUtils.parseJson(info).getAsJsonObject());
        }
        return null;
    }
    /**
     * @param json JSON object.
     * @return interim stop info.
     */
    protected static InterimStopInfo parseInterimStopInfo(final JsonObject json) {
        final InterimStopInfo info = new InterimStopInfo();
        final JsonElement id = json.get("id");
        if (id != null && !id.isJsonNull()) {
            info.setId(id.getAsLong());
        }
        info.setLatitude(json.get("latitude").getAsDouble());
        info.setLongitude(json.get("longitude").getAsDouble());
        info.setNumReadings(json.get("numReadings").getAsInt());
        info.setStartTime(json.get("startTime").getAsLong());
        return info;
    }
    protected static JsonObject toJson(final InterimStopInfo info) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", info.getId());
        json.addProperty("latitude", info.getLatitude());
        json.addProperty("longitude", info.getLongitude());
        json.addProperty("numReadings", info.getNumReadings());
        json.addProperty("startTime", info.getStartTime());
        return json;
    }
    /**
     * @return
     */
    private static String createInterimStopKey() {
        return NAME + "-stop";
    }
}
