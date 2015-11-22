/**
 *
 */
package com.visfresh.io.json;

import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceGroupConstants;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceGroupSerializer extends AbstractJsonSerializer {
    private final User user;

    /**
     * @param user the user.
     */
    public DeviceGroupSerializer(final User user) {
        super(user.getTimeZone());
        this.user = user;
    }

    /**
     * @param json JSON object.
     * @return device group.
     */
    public DeviceGroup parseDeviceGroup(final JsonObject json) {
        final DeviceGroup group = new DeviceGroup();
        group.setName(asString(json.get(DeviceGroupConstants.PROPERTY_NAME)));
        group.setDescription(asString(json.get(DeviceGroupConstants.PROPERTY_DESCRIPTION)));
        group.setCompany(user.getCompany());
        return group;
    }
    /**
     * @param group device group.
     * @return JSON object.
     */
    public JsonObject toJson(final DeviceGroup group) {
        final JsonObject json = new JsonObject();
        json.addProperty(DeviceGroupConstants.PROPERTY_NAME, group.getName());
        json.addProperty(DeviceGroupConstants.PROPERTY_DESCRIPTION, group.getDescription());
        return json;
    }
}
