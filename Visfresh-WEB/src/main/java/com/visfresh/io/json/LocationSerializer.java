/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.visfresh.constants.LocationConstants;
import com.visfresh.entities.LocationProfile;
import com.visfresh.io.shipment.LocationProfileBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationSerializer extends AbstractJsonSerializer {
    /**
     * @param tz time zone.
     */
    public LocationSerializer(final TimeZone tz) {
        super(tz);
    }

    /**
     * @param location
     * @return
     */
    public JsonObject toJson(final LocationProfileBean location) {
        if (location == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();

        obj.addProperty(LocationConstants.PROPERTY_LOCATION_ID, location.getId());
        obj.addProperty(LocationConstants.PROPERTY_LOCATION_NAME, location.getName());
        obj.addProperty(LocationConstants.PROPERTY_COMPANY_NAME, location.getCompanyName());
        obj.addProperty(LocationConstants.PROPERTY_NOTES, location.getNotes());
        obj.addProperty(LocationConstants.PROPERTY_ADDRESS, location.getAddress());

        final JsonObject loc = new JsonObject();
        obj.add(LocationConstants.PROPERTY_LOCATION, loc);
        loc.addProperty(LocationConstants.PROPERTY_LAT, location.getLocation().getLatitude());
        loc.addProperty(LocationConstants.PROPERTY_LON, location.getLocation().getLongitude());

        obj.addProperty(LocationConstants.PROPERTY_RADIUS_METERS, location.getRadius());

        obj.addProperty(LocationConstants.PROPERTY_START_FLAG, location.isStart() ? "Y" : "N");
        obj.addProperty(LocationConstants.PROPERTY_INTERIM_FLAG, location.isInterim() ? "Y" : "N");
        obj.addProperty(LocationConstants.PROPERTY_END_FLAG, location.isStop() ? "Y" : "N");

        return obj;
    }
    /**
     * @param obj encoded location profile.
     * @return location profile.
     */
    public LocationProfile parseLocationProfile(final JsonObject obj) {
        final LocationProfile location = new LocationProfile();
        parseLocationProfile(obj, location);
        return location;
    }
    /**
     * @param obj encoded location profile.
     * @return location profile.
     */
    public LocationProfileBean parseLocationProfileDto(final JsonObject obj) {
        final LocationProfileBean location = new LocationProfileBean();
        parseLocationProfile(obj, location);
        return location;
    }
    /**
     * @param obj
     * @param location
     */
    protected void parseLocationProfile(final JsonObject obj, final LocationProfileBean location) {
        location.setId(asLong(obj.get(LocationConstants.PROPERTY_LOCATION_ID)));
        location.setCompanyName(asString(obj.get(LocationConstants.PROPERTY_COMPANY_NAME)));
        location.setName(asString(obj.get(LocationConstants.PROPERTY_LOCATION_NAME)));
        location.setNotes(asString(obj.get(LocationConstants.PROPERTY_NOTES)));
        location.setAddress(asString(obj.get(LocationConstants.PROPERTY_ADDRESS)));

        location.setStart("Y".equalsIgnoreCase(asString(obj.get(LocationConstants.PROPERTY_START_FLAG))));
        location.setInterim("Y".equalsIgnoreCase(asString(obj.get(LocationConstants.PROPERTY_INTERIM_FLAG))));
        location.setStop("Y".equalsIgnoreCase(asString(obj.get(LocationConstants.PROPERTY_END_FLAG))));

        final JsonObject loc = obj.get(LocationConstants.PROPERTY_LOCATION).getAsJsonObject();
        location.getLocation().setLatitude(loc.get(LocationConstants.PROPERTY_LAT).getAsDouble());
        location.getLocation().setLongitude(loc.get(LocationConstants.PROPERTY_LON).getAsDouble());

        location.setRadius(asInt(obj.get(LocationConstants.PROPERTY_RADIUS_METERS)));
    }
}
