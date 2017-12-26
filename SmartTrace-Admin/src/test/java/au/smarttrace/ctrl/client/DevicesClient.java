/**
 *
 */
package au.smarttrace.ctrl.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.smarttrace.Device;
import au.smarttrace.ctrl.client.resp.AnyResponse;
import au.smarttrace.ctrl.client.resp.ColorListResponse;
import au.smarttrace.ctrl.client.resp.DeviceListResponse;
import au.smarttrace.ctrl.client.resp.DeviceResponse;
import au.smarttrace.ctrl.res.ColorDto;
import au.smarttrace.ctrl.res.ListResponse;
import au.smarttrace.device.GetDevicesRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DevicesClient extends BaseClient {
    /**
     * Default constructor.
     */
    public DevicesClient() {
        super();
    }

    /**
     * @param imei device IMEI.
     * @throws ServiceException
     * @throws IOException
     */
    public void deleteDevice(final String imei) throws IOException, ServiceException {
        final Map<String, String> params = new HashMap<>();
        params.put("device", imei);
        sendGetRequest(getPathWithToken("deleteDevice"), params, AnyResponse.class);
    }

    /**
     * @param device device to create.
     * @throws ServiceException
     * @throws IOException
     */
    public void createDevice(final Device device) throws IOException, ServiceException {
        sendPostRequest(getPathWithToken("createDevice"), device, AnyResponse.class);
    }
    /**
     * @param imei device IMEI code.
     * @return device.
     * @throws ServiceException
     * @throws IOException
     */
    public Device getDevice(final String imei) throws IOException, ServiceException {
        final Map<String, String> params = new HashMap<>();
        params.put("imei", imei);
        return sendGetRequest(getPathWithToken("getDevice"), params, DeviceResponse.class);
    }
    /**
     * @param req get devices request.
     * @return list of devices.
     * @throws IOException
     * @throws ServiceException
     */
    public ListResponse<Device> getDevices(final GetDevicesRequest req) throws IOException, ServiceException {
        return sendPostRequest(getPathWithToken("getDevices"), req, DeviceListResponse.class);
    }
    /**
     * @param device
     * @throws ServiceException
     * @throws IOException
     */
    public void updateDevice(final Device device) throws IOException, ServiceException {
        sendPostRequest(getPathWithToken("updateDevice"), device, AnyResponse.class);
    }

    /**
     * @param d device.
     * @param company company.
     * @throws ServiceException
     * @throws IOException
     */
    public Device moveDevice(final Device d, final Long company) throws IOException, ServiceException {
        final Map<String, String> params = new HashMap<>();
        params.put("device", d.getImei());
        params.put("company", company.toString());
        return sendGetRequest(getPathWithToken("moveDevice"), params, DeviceResponse.class);
    }

    /**
     * @return list of available system colors.
     * @throws ServiceException
     * @throws IOException
     */
    public List<ColorDto> getDeviceColors() throws IOException, ServiceException {
        return sendGetRequest(getPathWithToken("getDeviceColors"), new HashMap<>(), ColorListResponse.class);
    }
}
