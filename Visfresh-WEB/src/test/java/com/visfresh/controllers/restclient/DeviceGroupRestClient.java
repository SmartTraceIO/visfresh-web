/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.User;
import com.visfresh.io.json.DeviceGroupSerializer;
import com.visfresh.io.json.DeviceSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceGroupRestClient extends RestClient {

    /**
     * Serializer.
     */
    private DeviceGroupSerializer serializer;

    /**
     * @param user user.
     */
    public DeviceGroupRestClient(final User user) {
        super();
        this.serializer = new DeviceGroupSerializer(user);
    }

    /**
     * @param group
     * @throws RestServiceException
     * @throws IOException
     */
    public void saveDeviceGroup(final DeviceGroup group) throws IOException, RestServiceException {
        sendPostRequest(getPathWithToken("saveDeviceGroup"), serializer.toJson(group));
    }
    /**
     * @param pageIndex
     * @param pageSize
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<DeviceGroup> getDeviceGroups(final Integer pageIndex, final Integer pageSize)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }

        final JsonArray response = sendGetRequest(getPathWithToken("getDeviceGroups"),
                params).getAsJsonArray();

        final List<DeviceGroup> devices = new ArrayList<DeviceGroup>(response.size());
        for (int i = 0; i < response.size(); i++) {
            devices.add(serializer.parseDeviceGroup(response.get(i).getAsJsonObject()));
        }
        return devices;
    }
    /**
     * @param groupName
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public DeviceGroup getDeviceGroup(final String groupName) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", groupName);

        final JsonElement response = sendGetRequest(getPathWithToken("getDeviceGroup"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseDeviceGroup(
                response.getAsJsonObject());
    }
    /**
     * @param name device group name.
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteDeviceGroup(final String name) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        sendGetRequest(getPathWithToken("deleteDeviceGroup"), params);
    }

    /**
     * @param device device IMEI.
     * @param groupName device group name.
     * @throws RestServiceException
     * @throws IOException
     */
    public void addDeviceToGroup(final String device, final String groupName)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("groupName", groupName);
        params.put("device", device);
        sendGetRequest(getPathWithToken("addDeviceToGroup"), params);
    }

    /**
     * @param device device IMEI.
     * @param groupName device group name.
     * @throws RestServiceException
     * @throws IOException
     */
    public void removeDeviceFromGroup(final String device, final String groupName)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("groupName", groupName);
        params.put("device", device);
        sendGetRequest(getPathWithToken("removeDeviceFromGroup"), params);
    }

    /**
     * @param groupName group name.
     * @return list of devices for given group.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<Device> getGroupDevices(final String groupName) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("groupName", groupName);

        final JsonArray array = sendGetRequest(getPathWithToken("getGroupDevices"), params).getAsJsonArray();

        final DeviceSerializer devSer = new DeviceSerializer(TimeZone.getTimeZone("UTC"));
        final List<Device> list = new LinkedList<Device>();
        for (final JsonElement e : array) {
            list.add(devSer.parseDevice(e.getAsJsonObject()));
        }
        return list;
    }
}
