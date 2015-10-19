/**
 *
 */
package com.visfresh.entities;



/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommand implements EntityWithId<Long> {
    /**
     * Command ID.
     */
    private Long id;
    /**
     * Device.
     */
    private Device device;
    /**
     * Command to device
     */
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
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return id;
    }
}
