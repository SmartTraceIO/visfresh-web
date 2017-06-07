/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceCommandDao extends DaoBase<DeviceCommand, DeviceCommand, Long> {
    /**
     * @param command command.
     * @param imei device IMEI.
     */
    void saveCommand(String command, String imei);
    /**
     * @param d device.
     */
    void deleteCommandsFor(Device d);
}
