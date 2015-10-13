/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommand {
    private Device device;
    private String command;

    /**
     * Default constructor.
     */
    public DeviceCommand() {
        super();
    }

    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }
    /**
     * @param device the device to set
     */
    public void setDevice(final Device device) {
        this.device = device;
    }
    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }
    /**
     * @param command the command to set
     */
    public void setCommand(final String command) {
        this.command = command;
    }
}
