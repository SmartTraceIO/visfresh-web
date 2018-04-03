/**
 *
 */
package au.smarttrace;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceMessage {
    /**
     * The message ID.
     */
    private long id = -1;
    /**
     * Device IMEI code.
     */
    private String imei;
    /**
     * Time of creation.
     */
    private Date time;
    /**
     * Battery charge.
     */
    private int battery;
    /**
     * Temperature
     */
    private double temperature;
    /**
     * The number of retry.
     */
    private int numberOfRetry;
    /**
     * The ready on date.
     */
    private Date retryOn = new Date();
    private String message;

    /**
     * Default constructor.
     */
    public DeviceMessage() {
        super();
    }

    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
    }

    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final Date time) {
        this.time = time;
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
     * @return the temperature
     */
    public double getTemperature() {
        return temperature;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }
    /**
     * @return the numberOfRetry
     */
    public int getNumberOfRetry() {
        return numberOfRetry;
    }
    /**
     * @param numberOfRetry the numberOfRetry to set
     */
    public void setNumberOfRetry(final int numberOfRetry) {
        this.numberOfRetry = numberOfRetry;
    }
    /**
     * @return the readyOn
     */
    public Date getRetryOn() {
        return retryOn;
    }
    /**
     * @param retryOn the readyOn to set
     */
    public void setRetryOn(final Date retryOn) {
        this.retryOn = retryOn;
    }
    /**
     * @return the id
     */
    public long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final long id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        //358688000000158|AUT|2013/10/18 13:28:29|<LF> <IMEI>|<DATA_TYPE>|<TIME>|
        //4023|-10.24|<LF> <BATTERY>|<TEMPERATURE>|
        final StringBuilder sb = new StringBuilder();
        sb.append(getImei()).append('|');
        sb.append("bt04").append('|');
        sb.append(getTime()).append('|');

        sb.append('\n');

        sb.append(getBattery()).append('|');
        sb.append(getTemperature()).append('|');
        sb.append('\n');

        return sb.toString();
    }

    /**
     * @param msg
     */
    public void setMessage(final String msg) {
        this.message = msg;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
