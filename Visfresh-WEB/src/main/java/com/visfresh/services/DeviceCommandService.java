/**
 *
 */
package com.visfresh.services;

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
     */
    void sendCommand(DeviceCommand cmd);
    /**
     * Sends shutdown device command.
     * @param device device to shutdown.
     */
    void shutdownDevice(Device device);
}
