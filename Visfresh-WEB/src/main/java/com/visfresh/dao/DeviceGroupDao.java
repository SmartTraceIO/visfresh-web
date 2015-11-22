/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceGroupDao extends EntityWithCompanyDaoBase<DeviceGroup, String> {
    /**
     * Adds device to group.
     * @param group group.
     * @param device device.
     */
    void addDevice(DeviceGroup group, Device device);
    /**
     * Removes device from group.
     * @param group group.
     * @param device device.
     */
    void removeDevice(DeviceGroup group, Device device);
    /**
     * List of groups of given device.
     * @param device device.
     * @return list of groups for given device.
     */
    List<DeviceGroup> findByDevice(Device device);
}
