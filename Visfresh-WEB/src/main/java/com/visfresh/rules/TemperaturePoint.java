/**
 *
 */
package com.visfresh.rules;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperaturePoint {
    private double temperature;
    private Date date;

    /**
     * Default constructor.
     */
    public TemperaturePoint() {
        super();
    }

    /**
     * @param temperature
     * @param date
     */
    public TemperaturePoint(final double temperature, final Date date) {
        super();
        setTemperature(temperature);
        setDate(date);
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(final Date date) {
        this.date = date;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }
    /**
     * @return the temperature
     */
    public double getTemperature() {
        return temperature;
    }
}
