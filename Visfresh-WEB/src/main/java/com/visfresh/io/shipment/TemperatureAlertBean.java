/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.TemperatureAlert;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureAlertBean extends AlertBean {
    /**
     * Temperature.
     */
    private double temperature = 0.;
    /**
     * The time interval for given temperature
     */
    private int minutes = 0;
    /**
     * Is cumulative.
     */
    private boolean cumulative;
    /**
     * ID of rule which creates given alert..
     */
    private Long ruleId;

    /**
     * Default constructor.
     */
    public TemperatureAlertBean() {
        super();
    }
    /**
     * @param a temperature alert.
     */
    public TemperatureAlertBean(final TemperatureAlert a) {
        super(a);
        setTemperature(a.getTemperature());
        setMinutes(a.getMinutes());
        setCumulative(a.isCumulative());
        setRuleId(a.getRuleId());
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
     * @return the minutes
     */
    public int getMinutes() {
        return minutes;
    }
    /**
     * @param minutes the minutes to set
     */
    public void setMinutes(final int minutes) {
        this.minutes = minutes;
    }
    /**
     * @return the cumulative
     */
    public boolean isCumulative() {
        return cumulative;
    }
    /**
     * @param cumulative the cumulative to set
     */
    public void setCumulative(final boolean cumulative) {
        this.cumulative = cumulative;
    }
    /**
     * @return the ruleId
     */
    public Long getRuleId() {
        return ruleId;
    }
    /**
     * @param ruleId the ruleId to set
     */
    public void setRuleId(final Long ruleId) {
        this.ruleId = ruleId;
    }
}
