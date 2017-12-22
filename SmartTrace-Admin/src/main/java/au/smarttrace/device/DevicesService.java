/**
 *
 */
package au.smarttrace.device;

import java.util.List;

import au.smarttrace.ApplicationException;
import au.smarttrace.Color;
import au.smarttrace.Device;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DevicesService {
    /**
     * @return array of available system colors.
     */
    List<Color> getAvailableColors();
    /**
     * @param imei device IMEI.
     * @return device.
     */
    Device getDevice(String imei);
    /**
     * @param d device to save.
     */
    void updateDevice(Device d);
    /**
     * Device to delete.
     * @param imei device IMEI.
     */
    void deleteDevice(String imei);
    /**
     * @param device device IMEI.
     * @param company company.
     * @return
     * @throws ApplicationException
     */
    Device moveDevice(String device, Long company) throws ApplicationException;
    /**
     * @param d device to create.
     */
    void createDevice(Device d);
    /**
     * @param req request for list devices.
     * @return list devices
     */
    ListResponse<Device> getDevices(GetDevicesRequest req);
}
