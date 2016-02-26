/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceConstants;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.io.DeviceResolver;
import com.visfresh.lists.ListDeviceItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceSerializer extends AbstractJsonSerializer {
    private DeviceResolver deviceResolver;

    /**
     * @param tz time zone.
     */
    public DeviceSerializer(final TimeZone tz) {
        super(tz);
    }
    /**
     * @param json JSON object.
     * @return device.
     */
    public Device parseDevice(final JsonObject json) {
        final Device tr = new Device();
        tr.setImei(asString(json.get(DeviceConstants.PROPERTY_IMEI)));
        tr.setName(asString(json.get(DeviceConstants.PROPERTY_NAME)));
        tr.setDescription(asString(json.get(DeviceConstants.PROPERTY_DESCRIPTION)));
        tr.setActive(!Boolean.FALSE.equals(asBoolean(json.get(DeviceConstants.PROPERTY_ACTIVE))));
        return tr;
    }
    /**
     * @param d device.
     * @return device serialized to JSON format.
     */
    public JsonElement toJson(final Device d) {
        if (d == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(DeviceConstants.PROPERTY_DESCRIPTION, d.getDescription());
        obj.addProperty(DeviceConstants.PROPERTY_IMEI, d.getImei());
        obj.addProperty(DeviceConstants.PROPERTY_NAME, d.getName());
        obj.addProperty(DeviceConstants.PROPERTY_ACTIVE, d.isActive());
        obj.addProperty(DeviceConstants.PROPERTY_SN, d.getSn());
        return obj;
    }
    public ListDeviceItem parseListDeviceItem(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final ListDeviceItem d = new ListDeviceItem();
        d.setImei(asString(json.get(DeviceConstants.PROPERTY_IMEI)));
        d.setName(asString(json.get(DeviceConstants.PROPERTY_NAME)));
        d.setDescription(asString(json.get(DeviceConstants.PROPERTY_DESCRIPTION)));
        d.setSn(asString(json.get(DeviceConstants.PROPERTY_SN)));
        d.setActive(!Boolean.FALSE.equals(json.get(DeviceConstants.PROPERTY_ACTIVE)));

        d.setLastShipmentId(asLong(json.get(DeviceConstants.PROPERTY_LAST_SHIPMENT)));
        d.setLastReadingTimeISO(asString(json.get(DeviceConstants.PROPERTY_LAST_READING_TIME)));
        d.setLastReadingTemperature(asDouble(json.get(DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE)));
        d.setLastReadingBattery(asInteger(json.get(DeviceConstants.PROPERTY_LAST_READING_BATTERY)));
        d.setLastReadingLat(asDouble(json.get(DeviceConstants.PROPERTY_LAST_READING_LAT)));
        d.setLastReadingLong(asDouble(json.get(DeviceConstants.PROPERTY_LAST_READING_LONG)));

        return d;
    }
    /**
     * @param d device.
     * @return device serialized to JSON format.
     */
    public JsonElement toJson(final ListDeviceItem d) {
        if (d == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(DeviceConstants.PROPERTY_DESCRIPTION, d.getDescription());
        obj.addProperty(DeviceConstants.PROPERTY_IMEI, d.getImei());
        obj.addProperty(DeviceConstants.PROPERTY_NAME, d.getName());
        obj.addProperty(DeviceConstants.PROPERTY_SN, d.getSn());
        obj.addProperty(DeviceConstants.PROPERTY_ACTIVE, d.isActive());

        obj.addProperty(DeviceConstants.PROPERTY_LAST_SHIPMENT, d.getLastShipmentId());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_TIME, d.getLastReadingTimeISO());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE, d.getLastReadingTemperature());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_BATTERY, d.getLastReadingBattery());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_LAT, d.getLastReadingLat());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_LONG, d.getLastReadingLong());
        return obj;
    }
    /**
     * @param json JSON object.
     * @return device command.
     */
    public DeviceCommand parseDeviceCommand(final JsonObject json) {
        final DeviceCommand dc = new DeviceCommand();
        dc.setDevice(deviceResolver.getDevice(asString(json.get("device"))));
        dc.setCommand(asString(json.get("command")));
        return dc;
    }
    public JsonElement toJson(final DeviceCommand cmd) {
        if (cmd == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty("device", cmd.getDevice().getId());
        obj.addProperty("command", cmd.getCommand());
        return obj;
    }
    /**
     * @param deviceResolver the deviceResolver to set
     */
    public void setDeviceResolver(final DeviceResolver deviceResolver) {
        this.deviceResolver = deviceResolver;
    }
    /**
     * @return the deviceResolver
     */
    public DeviceResolver getDeviceResolver() {
        return deviceResolver;
    }
}
