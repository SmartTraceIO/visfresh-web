/**
 *
 */
package com.visfresh.controllers.lite;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteKeyLocation {

    private double temperature;
    private Date time;

    /**
     * Default constructor.
     */
    public LiteKeyLocation() {
        super();
    }
    /**
     * @param temperature
     * @param time
     */
    public LiteKeyLocation(final double temperature, final Date time) {
        super();
        this.temperature = temperature;
        this.time = time;
    }

    /**
     * @return temperature.
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
     * @return time.
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
}
