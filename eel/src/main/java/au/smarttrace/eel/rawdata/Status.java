/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Status {
    private final int status;

    /**
     * @param status status value.
     */
    public Status(final int status) {
        super();
        this.status = status;
    }

    public boolean isGpsFixed() {
        return (status & 1) > 0;
    }
    public boolean isDeviceDesignedForCar() {
        return ((status >> 1) & 1) > 0;
    }
    public boolean isCarEngineFired() {
        return isDeviceDesignedForCar() && ((status >> 2) & 1) > 0;
    }
    public boolean isAccelerometerSupported() {
        return ((status >> 3) & 1) > 0;
    }
    public boolean isMotionWarningActivated() {
        return isAccelerometerSupported() && ((status >> 4) & 1) > 0;
    }
    public boolean isRelayControlSupported() {
        return ((status >> 5) & 1) > 0;
    }
    public boolean isRelayControlTriggered() {
        return isRelayControlSupported() && ((status >> 6) & 1) > 0;
    }
    public boolean isExternalChargingSupported() {
        return ((status >> 7) & 1) > 0;
    }
    public boolean isDeviceCharging() {
        return isExternalChargingSupported() && ((status >> 8) & 1) > 0;
    }
    public boolean isDeviceActive() {
        return isAccelerometerSupported() && ((status >> 9) & 1) > 0;
    }
    public boolean isGpsModuleRunning() {
        return ((status >> 10) & 1) > 0;
    }
    public boolean isObdModuleRunning() {
        return ((status >> 11) & 1) > 0;
    }
    public boolean isDin0HighLevel() {
        return ((status >> 12) & 1) > 0;
    }
    public boolean isDin1HighLevel() {
        return ((status >> 13) & 1) > 0;
    }
    public boolean isDin2HighLevel() {
        return ((status >> 14) & 1) > 0;
    }
    public boolean isDin3HighLevel() {
        return ((status >> 15) & 1) > 0;
    }
    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }
}
