/**
 *
 */
package com.visfresh.rules;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Location;
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
public class AutoDetectEndLocationRule implements TrackerEventRule {
    private static final Logger log = LoggerFactory.getLogger(AutoDetectEndLocationRule.class);

    public static final String NAME = "AutoDetectEndLocation";

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AbstractRuleEngine engine;

    protected static class AutodetectData {
        private int numReadings = 0;
        private Long locationId;
        private final List<LocationProfile> locations = new LinkedList<>();

        public AutodetectData() {
        }

        /**
         * @return the locations
         */
        public List<LocationProfile> getLocations() {
            return locations;
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
         * @return the locationId
         */
        public Long getLocationId() {
            return locationId;
        }
        /**
         * @param locationId the locationId to set
         */
        public void setLocationId(final Long locationId) {
            this.locationId = locationId;
        }
    }

    /**
     * Default constructor.
     */
    public AutoDetectEndLocationRule() {
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
        final Shipment shipment = e.getShipment();
        if (shipment == null || shipment.getShippedTo() != null
                || e.getLatitude() == null || e.getLongitude() == null) {
            return false;
        }

        return getAutoDetectData(context.getSessionManager().getSession(shipment)) != null;
    }
    /**
     * @param context
     * @return
     */
    private LocationProfile getMatchesLocation(final AutodetectData data, final double latitude, final double longitude) {
        final List<LocationProfile> locs = data.getLocations();

        for (final LocationProfile loc : locs) {
            final Location end = loc.getLocation();
            double distance = LocationUtils.getDistanceMeters(
                    latitude, longitude, end.getLatitude(), end.getLongitude());
            distance = Math.max(0., distance - loc.getRadius());
            if (distance == 0) {
                return loc;
            }
        }

        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Shipment shipment = e.getShipment();
        final ShipmentSession session = context.getSessionManager().getSession(shipment);

        final AutodetectData data = getAutoDetectData(session);
        final LocationProfile loc = getMatchesLocation(data, e.getLatitude(), e.getLongitude());
        boolean isDetected = false;

        if (loc == null) {
            data.setNumReadings(0);
            session.setShipmentProperty(getLocationsKey(), toJSon(data).toString());
        } else if (!loc.getId().equals(data.getLocationId()) || data.getNumReadings() == 0) {
            data.setNumReadings(1);
            data.setLocationId(loc.getId());
            session.setShipmentProperty(getLocationsKey(), toJSon(data).toString());
            log.debug("Found location candidate '" + loc.getName()
                    + "' for shipment " + shipment.getId() + ". Waiting of next reading");
        } else {
            isDetected = true;
        }

        if (loc != null && (e.getType() == TrackerEventType.BRT || e.getType() == TrackerEventType.STP)) {
            isDetected = true;
        }

        if (isDetected) {
            log.debug("Location '" + loc.getName() + "' has detected and set to shipment " + shipment.getId());
            shipment.setShippedTo(loc);
            saveShipment(shipment);

            //stop check end location
            session.setShipmentProperty(getLocationsKey(), null);
            return true;
        }

        return false;
    }

    /**
     * @param shipment shipment to save.
     * @return
     */
    protected void saveShipment(final Shipment shipment) {
        shipmentDao.save(shipment);
    }
    /**
     * @param autoStart autostart issue.
     * @param state device state.
     */
    public static void needAutodetect(final AutoStartShipment autoStart,
            final ShipmentSession state) {
        final AutodetectData data = new AutodetectData();
        data.getLocations().addAll(autoStart.getShippedTo());

        state.setShipmentProperty(getLocationsKey(), toJSon(data).toString());
    }
    /**
     * @param locs list of locations.
     * @return JSON object.
     */
    protected static JsonObject toJSon(final AutodetectData data) {
        final JsonObject obj = new JsonObject();

        //add num readings
        obj.addProperty("numReadings", data.getNumReadings());
        obj.addProperty("location", data.getLocationId());

        //add locations
        final JsonArray array = new JsonArray();
        for (final LocationProfile l : data.getLocations()) {
            array.add(toJson(l));
        }

        obj.add("locations", array);
        return obj;
    }
    /**
     * @param l
     * @return
     */
    protected static JsonObject toJson(final LocationProfile l) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", l.getId());
        json.addProperty("lat", l.getLocation().getLatitude());
        json.addProperty("lon", l.getLocation().getLongitude());
        json.addProperty("radius", l.getRadius());
        return json;
    }

    protected static AutodetectData parseAutodetectData(final JsonElement obj) {
        final AutodetectData data = new AutodetectData();

        //get locations
        JsonArray array;
        if (obj.isJsonArray()) {
            //this is support of old version where only locations was serialized
            array = obj.getAsJsonArray();
        } else {
            final JsonObject json = obj.getAsJsonObject();
            //this is new approach
            array = json.get("locations").getAsJsonArray();
            //get number of readings
            data.setNumReadings(json.get("numReadings").getAsInt());
            if (json.has("location")) {
                final JsonElement e = json.get("location");
                if (!e.isJsonNull()) {
                    data.setLocationId(e.getAsLong());
                }
            }
        }

        for (final JsonElement jsonElement : array) {
            final JsonObject json = jsonElement.getAsJsonObject();

            final LocationProfile loc = new LocationProfile();
            loc.setId(json.get("id").getAsLong());
            loc.getLocation().setLatitude(json.get("lat").getAsDouble());
            loc.getLocation().setLongitude(json.get("lon").getAsDouble());
            loc.setRadius(json.get("radius").getAsInt());

            data.getLocations().add(loc);
        }

        return data;
    }
    /**
     * @param context
     * @return
     */
    protected AutodetectData getAutoDetectData(final ShipmentSession state) {
        final String str = state.getShipmentProperty(getLocationsKey());
        if (str == null) {
            return null;
        }
        final AutodetectData data = parseAutodetectData(SerializerUtils.parseJson(str).getAsJsonObject());
        return data;
    }

    /**
     * @return
     */
    protected static String getLocationsKey() {
        return NAME + ".locations";
    }
}
