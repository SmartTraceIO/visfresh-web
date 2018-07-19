/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationPackageBody implements PackageBody {
    // Location N Device position, see Section 3.6 POSITION
    private DevicePosition location;
    // Status 2 Device status, see Section 3.5 STATUS
    private Status deviceStatus;
    // Battery 2 Battery voltage (in mV) --- Unsigned 16 bits integer
    private int battery;
    //Mileage 4 Device mileage (in m) --- Unsigned 32 bits integer
    private int mileage;
    // Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
    private int temperature;
    //Humidity 2 Sensor humidity (in (1/10)%) --- Unsigned 16 bits integer
    private int humidity;
    // Illuminance 4 Sensor illuminance (in (1/256)lx) --- Unsigned 32 bits integer
    private long illuminance;
    //CO2 4 Sensor CO2 concentration (in ppm) --- Unsigned 32 bits integer
    private long co2;
    //2 Sensor Accelerometer X axis(in g)--- Unsigned 16 bits integer
    private int xAcceleration;
    //2 Sensor Accelerometer Y axis(in g)--- Unsigned 16 bits integer
    private int yAcceleration;
    //2 Sensor Accelerometer Z axis(final in g)--- Unsigned 16 bits integer
    private int zAcceleration;
    // 1 The version of Becaon data --- Unsigned 8 bits integer
    int beaconVersion;
    //Becaon data
    private final List<BeaconData> beacons = new LinkedList<>();
    /**
     * Default constructor.
     */
    public LocationPackageBody() {
        super();
    }

    /**
     * @return the location
     */
    public DevicePosition getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(final DevicePosition location) {
        this.location = location;
    }
    /**
     * @return the deviceStatus
     */
    public Status getDeviceStatus() {
        return deviceStatus;
    }
    /**
     * @param deviceStatus the deviceStatus to set
     */
    public void setDeviceStatus(final Status deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
    /**
     * @return the battery
     */
    public int getBattery() {
        return battery;
    }
    /**
     * @param battery the battery to set
     */
    public void setBattery(final int battery) {
        this.battery = battery;
    }
    /**
     * @return the mileage
     */
    public int getMileage() {
        return mileage;
    }
    /**
     * @param mileage the mileage to set
     */
    public void setMileage(final int mileage) {
        this.mileage = mileage;
    }
    /**
     * @return the temperature
     */
    public int getTemperature() {
        return temperature;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final int temperature) {
        this.temperature = temperature;
    }
    /**
     * @return the humidity
     */
    public int getHumidity() {
        return humidity;
    }
    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(final int humidity) {
        this.humidity = humidity;
    }
    /**
     * @return the illuminance
     */
    public long getIlluminance() {
        return illuminance;
    }
    /**
     * @param illuminance the illuminance to set
     */
    public void setIlluminance(final long illuminance) {
        this.illuminance = illuminance;
    }
    /**
     * @return the co2
     */
    public long getCo2() {
        return co2;
    }
    /**
     * @param co2 the co2 to set
     */
    public void setCo2(final long co2) {
        this.co2 = co2;
    }
    /**
     * @return the xAcceleration
     */
    public int getxAcceleration() {
        return xAcceleration;
    }
    /**
     * @param xAcceleration the xAcceleration to set
     */
    public void setxAcceleration(final int xAcceleration) {
        this.xAcceleration = xAcceleration;
    }
    /**
     * @return the yAcceleration
     */
    public int getyAcceleration() {
        return yAcceleration;
    }
    /**
     * @param yAcceleration the yAcceleration to set
     */
    public void setyAcceleration(final int yAcceleration) {
        this.yAcceleration = yAcceleration;
    }
    /**
     * @return the zAcceleration
     */
    public int getzAcceleration() {
        return zAcceleration;
    }
    /**
     * @param zAcceleration the zAcceleration to set
     */
    public void setzAcceleration(final int zAcceleration) {
        this.zAcceleration = zAcceleration;
    }
    /**
     * @return the beaconVersion
     */
    public int getBeaconVersion() {
        return beaconVersion;
    }
    /**
     * @param beaconVersion the beaconVersion to set
     */
    public void setBeaconVersion(final int beaconVersion) {
        this.beaconVersion = beaconVersion;
    }
    /**
     * @return the beacons
     */
    public List<BeaconData> getBeacons() {
        return beacons;
    }
}
