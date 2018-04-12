/**
 *
 */
package au.smarttrace.bt04;

import java.util.Date;
import java.util.Objects;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BeaconSignal {
    // SN|Name|Temperature|Humidity|RSSI|Distance|battery|LastScannedTime|HardwareModel|<LF>
    // SN|Name|Temperature|Humidity|RSSI|Distance|battery|LastScannedTime|HardwareModel|<LF>
    private String sn;
    private String name;
    private Double temperature;
    private Double humidity;
    private Double distance;
    private double battery;
    private Date lastScannedTime;
    private String hardwareModel;

    /**
     * Default constructor.
     */
    public BeaconSignal() {
        super();
    }

    /**
     * @return the sn
     */
    public String getSn() {
        return sn;
    }
    /**
     * @param sn the sn to set
     */
    public void setSn(final String sn) {
        this.sn = sn;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the temperature
     */
    public Double getTemperature() {
        return temperature;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final Double temperature) {
        this.temperature = temperature;
    }
    /**
     * @return the humidity
     */
    public Double getHumidity() {
        return humidity;
    }
    /**
     * @param humidity the humidity to set
     */
    public void setHumidity(final Double humidity) {
        this.humidity = humidity;
    }
    /**
     * @return the distance
     */
    public Double getDistance() {
        return distance;
    }
    /**
     * @param distance the distance to set
     */
    public void setDistance(final Double distance) {
        this.distance = distance;
    }
    /**
     * @return the battery
     */
    public double getBattery() {
        return battery;
    }
    /**
     * @param battery the battery to set
     */
    public void setBattery(final double battery) {
        this.battery = battery;
    }
    /**
     * @return the lastScannedTime
     */
    public Date getLastScannedTime() {
        return lastScannedTime;
    }
    /**
     * @param lastScannedTime the lastScannedTime to set
     */
    public void setLastScannedTime(final Date lastScannedTime) {
        this.lastScannedTime = lastScannedTime;
    }
    /**
     * @return the hardwareModel
     */
    public String getHardwareModel() {
        return hardwareModel;
    }
    /**
     * @param hardwareModel the hardwareModel to set
     */
    public void setHardwareModel(final String hardwareModel) {
        this.hardwareModel = hardwareModel;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof BeaconSignal)) {
            return false;
        }

        final BeaconSignal other = (BeaconSignal) obj;
        return Objects.equals(this.sn, other.sn) &&
            Objects.equals(this.name, other.name) &&
            Objects.equals(this.temperature, other.temperature) &&
            Objects.equals(this.humidity, other.humidity) &&
            Objects.equals(this.distance, other.distance) &&
            Objects.equals(this.battery, other.battery) &&
            Objects.equals(this.lastScannedTime.getTime(), other.lastScannedTime.getTime()) &&
            Objects.equals(this.hardwareModel, other.hardwareModel);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(sn, name, temperature, humidity,
                distance, battery, lastScannedTime.getTime(), hardwareModel);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("sn: " + sn + ", ");
        sb.append("name: " + name + ", ");
        sb.append("temperature: " + temperature + ", ");
        sb.append("humidity: " + humidity + ", ");
        sb.append("distance: " + distance + ", ");
        sb.append("battery: " + battery + ", ");
        sb.append("lastScannedTime.getTime(): " + lastScannedTime.getTime() + ", ");
        sb.append("hardwareModel: " + hardwareModel);
        return sb.toString();
    }
}
