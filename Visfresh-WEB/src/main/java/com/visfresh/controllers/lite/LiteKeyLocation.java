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
    private long id;
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
}
