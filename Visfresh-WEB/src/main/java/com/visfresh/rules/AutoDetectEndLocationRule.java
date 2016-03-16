/**
 *
 */
package com.visfresh.rules;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

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
import com.visfresh.rules.state.DeviceState;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AutoDetectEndLocationRule implements TrackerEventRule {
    public static final String NAME = "AutoDetectEndLocation";

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AbstractRuleEngine engine;

    protected static class AutodetectData {
        private int numReadings = 0;
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
        final Shipment shipment = context.getEvent().getShipment();
        if (shipment == null || shipment.getShippedTo() != null) {
            return false;
        }

        return getAutoDetectData(context.getState()) != null;
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

        final AutodetectData data = getAutoDetectData(context.getState());
        final LocationProfile loc = getMatchesLocation(data, e.getLatitude(), e.getLongitude());
        if (loc == null) {
            data.setNumReadings(0);
            context.getState().setShipmentProperty(getLocationsKey(), toJSon(data).toString());
        } else if (data.getNumReadings() == 0) {
            data.setNumReadings(1);
            context.getState().setShipmentProperty(getLocationsKey(), toJSon(data).toString());
        } else {
            final Shipment shipment = context.getEvent().getShipment();
            shipment.setShippedTo(loc);
            saveShipment(shipment);

            //stop check end location
            context.getState().setShipmentProperty(getLocationsKey(), null);
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
            final DeviceState state) {
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
    protected AutodetectData getAutoDetectData(final DeviceState state) {
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
