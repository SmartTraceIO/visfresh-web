/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.io.json.DeviceSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceRestClient extends RestClient {
    private DeviceSerializer serializer;
    /**
     *
     */
    public DeviceRestClient(final TimeZone tz) {
        serializer = new DeviceSerializer(tz);
    }

    /**
     * @param tr Device.
     */
    public void saveDevice(final Device tr) throws RestServiceException, IOException {
        sendPostRequest(getPathWithToken("saveDevice"),
                serializer.toJson(tr));
    }
    /**
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return
     */
    public List<Device> getDevices(final Integer pageIndex, final Integer pageSize) throws RestServiceException, IOException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }

        final JsonArray response = sendGetRequest(getPathWithToken("getDevices"),
                params).getAsJsonArray();

        final List<Device> devices = new ArrayList<Device>(response.size());
        for (int i = 0; i < response.size(); i++) {
            devices.add(serializer.parseDevice(response.get(i).getAsJsonObject()));
        }
        return devices;
    }
    /**
     * @param device device.
     * @param command device specific command.
     * @throws IOException
     * @throws RestServiceException
     */
    public void sendCommandToDevice(final Device device, final String command) throws IOException, RestServiceException {
        final DeviceCommand cmd = new DeviceCommand();
        cmd.setDevice(device);
        cmd.setCommand(command);

        sendPostRequest(getPathWithToken("sendCommandToDevice"),
                serializer.toJson(cmd));
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getDevice(java.lang.String)
     */
    public Device getDevice(final String id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("imei", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getDevice"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseDevice(
                response.getAsJsonObject());
    }
    /**
     * @param p device to delete.
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteDevice(final Device p) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("imei", p.getId());
        sendGetRequest(getPathWithToken("deleteDevice"), params);
    }
}