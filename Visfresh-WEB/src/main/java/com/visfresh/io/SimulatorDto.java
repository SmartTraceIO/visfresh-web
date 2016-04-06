/**
 *
 */
package com.visfresh.io;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SimulatorDto {
    private String sourceDevice;
    private String user;
    private String targetDevice;
    private Long autoStart;
    private boolean isStarted;

    /**
     * default constructor.
     */
    public SimulatorDto() {
        super();
    }

    /**
     * @return source device.
     */
    public String getSourceDevice() {
        return sourceDevice;
    }
    /**
     * @param sourceDevice the sourceDevice to set
     */
    public void setSourceDevice(final String sourceDevice) {
        this.sourceDevice = sourceDevice;
    }
    /**
     * @return user Email
     */
    public String getUser() {
        return user;
    }
    /**
     * @param user the user to set
     */
    public void setUser(final String user) {
        this.user = user;
    }
    /**
     * @return the targetDevice
     */
    public String getTargetDevice() {
        return targetDevice;
    }
    /**
     * @param targetDevice the targetDevice to set
     */
    public void setTargetDevice(final String targetDevice) {
        this.targetDevice = targetDevice;
    }
    /**
     * @return the started state.
     */
    public boolean isStarted() {
        return isStarted;
    }
    /**
     * @param b the started state.
     */
    public void setStarted(final boolean b) {
        this.isStarted = b;
    }
    /**
     * @return the autostart
     */
    public Long getAutoStart() {
        return autoStart;
    }
    /**
     * @param autostart the autostart to set
     */
    public void setAutoStart(final Long autostart) {
        this.autoStart = autostart;
    }
}
