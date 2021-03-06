/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.Language;
import com.visfresh.entities.ListDeviceItem;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.json.DeviceSerializer;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceRestClient extends RestClient {
    private DeviceSerializer serializer;
    /**
     *
     */
    public DeviceRestClient(final User user) {
        this(user.getTimeZone(), user.getLanguage(), user.getTemperatureUnits());
    }
    /**
     * @param tz
     * @param lang
     * @param tu
     */
    public DeviceRestClient(final TimeZone tz, final Language lang, final TemperatureUnits tu) {
        serializer = new DeviceSerializer(tz, lang, tu);
    }

    /**
     * @param tr Device.
     */
    public void saveDevice(final Device tr) throws RestServiceException, IOException {
        sendPostRequest(getPathWithToken("saveDevice"),
                serializer.toJson(tr));
    }
    /**
     * @param sortColumn sort column.
     * @param sortOrder sort order.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return
     */
    public List<ListDeviceItem> getDevices(final String sortColumn, final boolean sortOrder,
            final Integer pageIndex, final Integer pageSize) throws RestServiceException, IOException {
        final JsonArray response = getDevicesJson(sortColumn, sortOrder, pageIndex, pageSize);

        final List<ListDeviceItem> devices = new ArrayList<>(response.size());
        for (int i = 0; i < response.size(); i++) {
            devices.add(serializer.parseListDeviceItem(response.get(i).getAsJsonObject()));
        }
        return devices;
    }
    /**
     * @param sortColumn
     * @param sortOrder
     * @param pageIndex
     * @param pageSize
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    public JsonArray getDevicesJson(final String sortColumn, final boolean sortOrder, final Integer pageIndex,
            final Integer pageSize) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        if (sortColumn != null) {
            params.put("sc", sortColumn);
            params.put("so", sortOrder ? "asc" : "desc");
        }

        final JsonArray response = sendGetRequest(getPathWithToken("getDevices"),
                params).getAsJsonArray();
        return response;
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
    /**
     * @param imei device IMEI.
     * @param shipment shipment, can be null
     * @throws RestServiceException
     * @throws IOException
     */
    public void shutdownDevice(final Shipment shipment) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentId", shipment.getId().toString());
        sendGetRequest(getPathWithToken("shutdownDevice"), params);
    }
    /**
     * @param device
     * @param company
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public String moveDevice(final Device device, final Company company) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("device", device.getImei());
        params.put("company", company.getId().toString());

        final JsonElement response = sendGetRequest(getPathWithToken("moveDevice"), params);
        return parseStringId(response.getAsJsonObject());
    }
    /**
     * @param company.
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public void initDeviceColors(final Long company) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (company != null) {
            params.put("company", company.toString());
        }

        sendGetRequest(getPathWithToken("initDeviceColors"), params);
    }

    /**
     * @param device
     * @param startDate
     * @param endDate
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public String getReadings(final Device device, final Date startDate, final Date endDate) throws IOException, RestServiceException {
        final DateFormat fmt = DateTimeUtils.createDateFormat(
                "yyyy-MM-dd'T'HH-mm-ss", Language.English, this.serializer.getTimeZone());

        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("device", device.getImei());
        if (startDate != null) {
            params.put("startDate", fmt.format(startDate));
        }
        if (endDate != null) {
            params.put("endDate", fmt.format(endDate));
        }

        return doSendGetRequest(getPathWithToken("getReadings"), params);
    }
    /**
     * @param s shipment.
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public String getReadings(final Shipment s) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("sn", Device.getSerialNumber(s.getDevice().getModel(), s.getDevice().getImei()));
        params.put("trip", Integer.toString(s.getTripCount()));

        return doSendGetRequest(getPathWithToken("getReadings"), params);
    }
}
