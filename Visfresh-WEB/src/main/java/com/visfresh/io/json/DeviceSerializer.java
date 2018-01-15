/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceConstants;
import com.visfresh.entities.Color;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.DeviceModel;
import com.visfresh.io.DeviceResolver;
import com.visfresh.lists.DeviceDto;

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
        if (has(json, DeviceConstants.PROPERTY_MODEL)) {
            tr.setModel(DeviceModel.valueOf(asString(json.get(DeviceConstants.PROPERTY_MODEL))));
        }
        tr.setName(asString(json.get(DeviceConstants.PROPERTY_NAME)));
        tr.setColor(parseColor(asString(json.get(DeviceConstants.PROPERTY_COLOR))));
        tr.setDescription(asString(json.get(DeviceConstants.PROPERTY_DESCRIPTION)));
        tr.setActive(!Boolean.FALSE.equals(asBoolean(json.get(DeviceConstants.PROPERTY_ACTIVE))));
        tr.setAutostartTemplateId(asLong(json.get(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID)));
        return tr;
    }
    /**
     * @param d device.
     * @return device serialized to JSON format.
     */
    public JsonObject toJson(final Device d) {
        if (d == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(DeviceConstants.PROPERTY_DESCRIPTION, d.getDescription());
        obj.addProperty(DeviceConstants.PROPERTY_IMEI, d.getImei());
        obj.addProperty(DeviceConstants.PROPERTY_MODEL, d.getModel().name());
        obj.addProperty(DeviceConstants.PROPERTY_NAME, d.getName());
        obj.addProperty(DeviceConstants.PROPERTY_ACTIVE, d.isActive());
        obj.addProperty(DeviceConstants.PROPERTY_SN, d.getSn());
        obj.addProperty(DeviceConstants.PROPERTY_COLOR, d.getColor() != null ? d.getColor().name() : null);
        obj.addProperty(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID, d.getAutostartTemplateId());
        return obj;
    }
    public DeviceDto parseListDeviceItem(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final DeviceDto d = new DeviceDto();
        d.setImei(asString(json.get(DeviceConstants.PROPERTY_IMEI)));
        if (has(json, DeviceConstants.PROPERTY_MODEL)) {
            d.setModel(DeviceModel.valueOf(asString(json.get(DeviceConstants.PROPERTY_MODEL))));
        }
        d.setName(asString(json.get(DeviceConstants.PROPERTY_NAME)));
        d.setDescription(asString(json.get(DeviceConstants.PROPERTY_DESCRIPTION)));
        d.setSn(asString(json.get(DeviceConstants.PROPERTY_SN)));
        d.setActive(!Boolean.FALSE.equals(asBoolean(json.get(DeviceConstants.PROPERTY_ACTIVE))));
        d.setAutostartTemplateId(asLong(json.get(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID)));
        d.setAutostartTemplateName(asString(json.get(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_NAME)));
        d.setColor(asString(json.get(DeviceConstants.PROPERTY_COLOR)));

        d.setLastShipmentId(asLong(json.get(DeviceConstants.PROPERTY_LAST_SHIPMENT)));
        final String status = asString(json.get(DeviceConstants.PROPERTY_SHIPMENT_STATUS));
        if (status != null) {
            d.setShipmentStatus(status);
        }
        d.setShipmentNumber(asString(json.get(DeviceConstants.PROPERTY_SHIPMENT_NUMBER)));
        d.setLastReadingTimeISO(asString(json.get(DeviceConstants.PROPERTY_LAST_READING_TIME_ISO)));
        d.setLastReadingTime(asString(json.get(DeviceConstants.PROPERTY_LAST_READING_TIME)));
        d.setLastReadingTemperature(asString(json.get(DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE)));
        d.setLastReadingBattery(asInteger(json.get(DeviceConstants.PROPERTY_LAST_READING_BATTERY)));
        d.setLastReadingLat(asDouble(json.get(DeviceConstants.PROPERTY_LAST_READING_LAT)));
        d.setLastReadingLong(asDouble(json.get(DeviceConstants.PROPERTY_LAST_READING_LONG)));

        return d;
    }
    /**
     * @param d device.
     * @return device serialized to JSON format.
     */
    public JsonElement toJson(final DeviceDto d) {
        if (d == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(DeviceConstants.PROPERTY_DESCRIPTION, d.getDescription());
        obj.addProperty(DeviceConstants.PROPERTY_IMEI, d.getImei());
        obj.addProperty(DeviceConstants.PROPERTY_MODEL, d.getModel().name());
        obj.addProperty(DeviceConstants.PROPERTY_NAME, d.getName());
        obj.addProperty(DeviceConstants.PROPERTY_SN, d.getSn());
        obj.addProperty(DeviceConstants.PROPERTY_COLOR, d.getColor());
        obj.addProperty(DeviceConstants.PROPERTY_ACTIVE, d.isActive());
        obj.addProperty(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID, d.getAutostartTemplateId());
        obj.addProperty(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_NAME, d.getAutostartTemplateName());

        obj.addProperty(DeviceConstants.PROPERTY_LAST_SHIPMENT, d.getLastShipmentId());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_TIME_ISO, d.getLastReadingTimeISO());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_TIME, d.getLastReadingTime());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE, d.getLastReadingTemperature());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_BATTERY, d.getLastReadingBattery());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_LAT, d.getLastReadingLat());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_LONG, d.getLastReadingLong());
        obj.addProperty(DeviceConstants.PROPERTY_SHIPMENT_NUMBER, d.getShipmentNumber());
        obj.addProperty(DeviceConstants.PROPERTY_SHIPMENT_STATUS, d.getShipmentStatus());
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
    /**
     * @param name color name.
     * @return
     */
    private Color parseColor(final String name) {
        if (name == null) {
            return null;
        }
        return Color.valueOf(name);
    }
}
