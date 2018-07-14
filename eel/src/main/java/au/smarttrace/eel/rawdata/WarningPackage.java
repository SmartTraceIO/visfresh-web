/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class WarningPackage extends AbstractPackage {
    public enum WarningType {
        Sos(0x02),//0x02 SOS
        ExternalPowerCutOff(0x01),//0x01 External power cut-off
        BatteryLow(0x03),//0x03 Battery low (only for the device which uses a battery as main power)
        GpsAntennaOpenCircuit(0x08),//0x08 GPS antenna open-circuit (only for the device with external GPS antenna)
        GpsAntennaShortCircuit(0x09),//0x09 GPS antenna short-circuit (only for the device with external GPS antenna)
        ActivityWarning(0x04),//0x04 Activity warning (only for the device with an accelerometer)
        CrashWarning(0x85),//0x85 Crash warning (only for the device with an accelerometer)
        FreeFallWarning(0x86),//0x86 Free-fall warning (only for the device with an accelerometer)
        HighSpeed(0x82),//0x82 High-speed warning
        InToFence(0x83),//0x83 In-to-fence warning
        OutOfFence(0x84),//0x84 Out-of-fence warning
        Shift(0x05),//0x05 Shift warning
        OutOfInternalTemperatureRange(0x20),//0x20 Out of internal temperature range
        OutOfHumidityRange(0x21),//0x21 Out of humidity range
        OutOfIlluminanceRange(0x22),//0x22 Out of illuminance range
        OutOfCo2ConcentrationRange(0x23),//0x23 Out of CO2 concentration range
        OutOfProbeTemperatureRange(0x24);//0x24 Out of probe temperature range

        private final int value;

        /**
         * @param value numeric value.
         */
        private WarningType(final int value) {
            this.value = value;
        }

        public static WarningType valueOf(final int value) {
            for (final WarningType pid : values()) {
                if (pid.value == value) {
                    return pid;
                }
            }
            throw new RuntimeException("Unexpected package identifier " + value);
        }
        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    private DevicePosition location;
    private WarningType warningType;
    private Status status;

    /**
     * Default constructor.
     */
    public WarningPackage() {
        super();
    }

    /**
     * @param pos device position.
     */
    public void setLocation(final DevicePosition pos) {
        this.location = pos;
    }
    /**
     * @return the location
     */
    public DevicePosition getLocation() {
        return location;
    }
    /**
     * @param t
     */
    public void setWarningType(final WarningType t) {
        this.warningType = t;
    }
    /**
     * @return the warningType
     */
    public WarningType getWarningType() {
        return warningType;
    }

    /**
     * @param status
     */
    public void setStatus(final Status status) {
        this.status = status;
    }
    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }
}
