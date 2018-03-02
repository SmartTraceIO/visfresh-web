/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Color;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.ListDeviceItem;
import com.visfresh.rules.state.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceDao extends EntityWithCompanyDaoBase<Device, Device, String> {
    /**
     * Is equivalent of find by ID.
     * @param imei device IMEI.
     * @return list of all registered devices by given IMEI.
     */
    Device findByImei(String imei);
    /**
     * @param imei device IMEI.
     * @return device state.
     */
    DeviceState getState(String imei);
    /**
     * @param imei device IMEI.
     * @param state device state.
     */
    void saveState(String imei, DeviceState state);
    /**
     * Get devices from given group.
     * @param group device group.
     * @return devices.
     */
    List<Device> findByGroup(DeviceGroup group);
    /**
     * @param company company.
     * @param sorting sorting.
     * @param page page.
     * @return list of device items.
     */
    List<ListDeviceItem> getDevices(Long company, Sorting sorting,
            Page page);
    /**
     * @param device device.
     * @param c company.
     */
    void moveToNewCompany(Device device, Long c);
    /**
     * @param d device.
     * @param color color.
     */
    void updateColor(Device d, Color color);
}
