/**
 *
 */
package com.visfresh.services;

import java.util.Date;

import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceCommandService {
    /**
     * Sends command to device.
     * @param cmd command to send.
     * @param date command time out
     */
    void sendCommand(DeviceCommand cmd, Date date);
    /**
     * Sends shutdown device command.
     * @param device device to shutdown.
     * @param date shutdown date.
     */
    void shutdownDevice(Device device, Date date);
}
