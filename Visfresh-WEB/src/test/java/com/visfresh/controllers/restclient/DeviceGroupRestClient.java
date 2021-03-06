/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
    private final User user;

    /**
     * @param user user.
     */
    public DeviceGroupRestClient(final User user) {
        super();
        this.user = user;
        this.serializer = new DeviceGroupSerializer(user);
    }

    /**
     * @param group
     * @throws RestServiceException
     * @throws IOException
     */
    public Long saveDeviceGroup(final DeviceGroup group) throws IOException, RestServiceException {
        final JsonElement result = sendPostRequest(getPathWithToken("saveDeviceGroup"), serializer.toJson(group));
        return parseId(result.getAsJsonObject());
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
        return getDeviceGroups(pageIndex, pageSize, null, null);
    }
    /**
     * @param pageIndex page idex.
     * @param pageSize page size.
     * @param sc sort column.
     * @param so sort order.
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<DeviceGroup> getDeviceGroups(final Integer pageIndex, final Integer pageSize,
            final String sc, final String so)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        if (sc != null) {
            params.put("sc", sc);
        }
        if (so != null) {
            params.put("so", so);
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
        return getDeviceGroup(params);
    }
    /**
     * @param groupName
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public DeviceGroup getDeviceGroup(final Long groupId) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", groupId.toString());
        return getDeviceGroup(params);
    }
    /**
     * @param params
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    protected DeviceGroup getDeviceGroup(final HashMap<String, String> params)
            throws IOException, RestServiceException {
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
        deleteDeviceGroup(params);
    }
    /**
     * @param name device group name.
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteDeviceGroup(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());
        deleteDeviceGroup(params);
    }

    /**
     * @param params
     * @throws IOException
     * @throws RestServiceException
     */
    protected void deleteDeviceGroup(final HashMap<String, String> params)
            throws IOException, RestServiceException {
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
        addDeviceToGroup(device, params);
    }
    /**
     * @param device device IMEI.
     * @param groupName device group name.
     * @throws RestServiceException
     * @throws IOException
     */
    public void addDeviceToGroup(final String device, final Long groupId)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("groupId", groupId.toString());
        addDeviceToGroup(device, params);
    }
    /**
     * @param device
     * @param params
     * @throws IOException
     * @throws RestServiceException
     */
    protected void addDeviceToGroup(final String device,
            final HashMap<String, String> params) throws IOException,
            RestServiceException {
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
        removeDeviceFromGroup(device, params);
    }
    /**
     * @param device device IMEI.
     * @param groupId device group ID.
     * @throws RestServiceException
     * @throws IOException
     */
    public void removeDeviceFromGroup(final String device, final Long groupId)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("groupId", groupId.toString());
        removeDeviceFromGroup(device, params);
    }
    /**
     * @param device
     * @param params
     * @throws IOException
     * @throws RestServiceException
     */
    protected void removeDeviceFromGroup(final String device,
            final HashMap<String, String> params) throws IOException,
            RestServiceException {
        params.put("device", device);
        sendGetRequest(getPathWithToken("removeDeviceFromGroup"), params);
    }

    /**
     * @param groupName group name.
     * @return list of devices for given group.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<Device> getDevicesOfGroup(final String groupName) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("groupName", groupName);
        return getDevicesOfGroup(params);
    }
    /**
     * @param groupName group name.
     * @return list of devices for given group.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<Device> getDevicesOfGroup(final Long groupId) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("groupId", groupId.toString());
        return getDevicesOfGroup(params);
    }
    /**
     * @param params
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    protected List<Device> getDevicesOfGroup(
            final HashMap<String, String> params) throws IOException,
            RestServiceException {
        final JsonArray array = sendGetRequest(getPathWithToken("getDevicesOfGroup"), params).getAsJsonArray();

        final DeviceSerializer devSer = new DeviceSerializer(user);
        final List<Device> list = new LinkedList<Device>();
        for (final JsonElement e : array) {
            list.add(devSer.parseDevice(e.getAsJsonObject()));
        }
        return list;
    }
    /**
     * @param device device IMEI.
     * @return list of groups for given device.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<DeviceGroup> getGroupsOfDevice(final String device) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("device", device);

        final JsonArray array = sendGetRequest(getPathWithToken("getGroupsOfDevice"), params).getAsJsonArray();

        final List<DeviceGroup> list = new LinkedList<DeviceGroup>();
        for (final JsonElement e : array) {
            list.add(serializer.parseDeviceGroup(e.getAsJsonObject()));
        }
        return list;
    }
}
