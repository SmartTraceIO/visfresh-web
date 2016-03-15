/**
 *
 */
package com.visfresh.rules;

import java.util.LinkedList;
import java.util.List;

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

    /**
     * Default constructor.
     */
    public AutoDetectEndLocationRule() {
        super();
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

        return getMatchesLocation(context) != null;
    }
    /**
     * @param context
     * @return
     */
    private LocationProfile getMatchesLocation(final RuleContext context) {
        final String str = context.getState().getShipmentProperty(getLocationsKey());
        if (str == null) {
            return null;
        }
        final double latitude = context.getEvent().getLatitude();
        final double longitude = context.getEvent().getLongitude();

        final List<LocationProfile> locs = parseLocations(SerializerUtils.parseJson(str).getAsJsonArray());
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
        final LocationProfile loc = getMatchesLocation(context);

        final Shipment shipment = context.getEvent().getShipment();
        shipment.setShippedTo(loc);
        saveShipment(shipment);

        //stop check end location
        context.getState().setShipmentProperty(getLocationsKey(), null);

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
        final JsonArray json = toJSon(autoStart.getShippedTo());
        state.setShipmentProperty(getLocationsKey(), json.toString());
    }
    /**
     * @param locs list of locations.
     * @return JSON object.
     */
    protected static JsonArray toJSon(final List<LocationProfile> locs) {
        final JsonArray array = new JsonArray();
        for (final LocationProfile l : locs) {
            final JsonObject json = new JsonObject();
            json.addProperty("id", l.getId());
            json.addProperty("lat", l.getLocation().getLatitude());
            json.addProperty("lon", l.getLocation().getLongitude());
            json.addProperty("radius", l.getRadius());

            array.add(json);
        }
        return array;
    }

    protected static List<LocationProfile> parseLocations(final JsonArray array) {
        final List<LocationProfile> locs = new LinkedList<>();
        for (final JsonElement jsonElement : array) {
            final JsonObject json = jsonElement.getAsJsonObject();

            final LocationProfile loc = new LocationProfile();
            loc.setId(json.get("id").getAsLong());
            loc.getLocation().setLatitude(json.get("lat").getAsDouble());
            loc.getLocation().setLongitude(json.get("lon").getAsDouble());
            loc.setRadius(json.get("radius").getAsInt());

            locs.add(loc);
        }

        return locs;
    }

    /**
     * @return
     */
    protected static String getLocationsKey() {
        return NAME + ".locations";
    }
}
