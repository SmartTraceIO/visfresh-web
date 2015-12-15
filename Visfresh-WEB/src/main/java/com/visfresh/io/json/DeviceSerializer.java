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
        obj.addProperty(DeviceConstants.PROPERTY_SN, d.getSn());
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
