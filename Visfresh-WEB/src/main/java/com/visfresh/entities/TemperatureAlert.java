/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureAlert extends Alert {
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
    public TemperatureAlert() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.Alert#setType(com.visfresh.entities.AlertType)
     */
    @Override
    public void setType(final AlertType type) {
        checkAlertType(type);
        super.setType(type);
    }
    /**
     * @param type
     */
    private void checkAlertType(final AlertType type) {
        if (!(type == AlertType.Cold || type == AlertType.Hot
                || type == AlertType.CriticalCold || type == AlertType.CriticalHot)) {
            throw new IllegalArgumentException("Illegal alert type " + type + " for temperature alert");
        }
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
     * @param cumulative the cumulative to set
     */
    public void setCumulative(final boolean cumulative) {
        this.cumulative = cumulative;
    }
    /**
     * @return the cumulative
     */
    public boolean isCumulative() {
        return cumulative;
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
