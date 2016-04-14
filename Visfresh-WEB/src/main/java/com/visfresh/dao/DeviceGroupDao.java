/**
 *
 */
package com.visfresh.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.io.shipment.DeviceGroupDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceGroupDao extends EntityWithCompanyDaoBase<DeviceGroup, Long> {
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
    /**
     * @param ids set of shipment ID.
     * @return map of shipment ID to list of device groups.
     */
    Map<Long, List<DeviceGroupDto>> getShipmentGroups(Set<Long> ids);
    /**
     * @param groupName group name.
     * @return device group by given name.
     */
    DeviceGroup findByName(String groupName);
    /**
     * @param oldDevice old device.
     * @param newDevice new device.
     */
    void moveToNewDevice(Device oldDevice, Device newDevice);
}
