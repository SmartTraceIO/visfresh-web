/**
 *
 */
package au.smarttrace.device;

import au.smarttrace.Company;
import au.smarttrace.Device;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DevicesDao {
    /**
     * @param imei device IMEI.
     * @return device.
     */
    Device getByImei(String imei);
    /**
     * @param imei device IMEI.
     */
    void deleteDevice(String imei);
    /**
     * @param d device to save.
     */
    void updateDevice(Device d);
    /**
     * @param device device.
     * @return trip count for given device.
     */
    int getTripCount(Device device);
    /**
     * @param d device.
     * @param tripCount trip count to set.
     */
    void setTripCount(Device d, int tripCount);
    /**
     * @param device device to move.
     * @param c target company.
     * @param backup backup device.
     */
    void moveToNewCompany(Device device, Company c, Device backup);
    /**
     * @param d device to create.
     */
    void createDevice(Device d);
    /**
     * @param req request for get devices.
     * @return list devices.
     */
    ListResponse<Device> getDevices(GetDevicesRequest req);
}
