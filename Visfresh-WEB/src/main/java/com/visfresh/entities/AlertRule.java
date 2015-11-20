/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertRule {
    private AlertType type;
    private double temperature;
    private int timeOutMinutes;
    private boolean cumulativeFlag;
    private Long id;

    /**
     * Default constructor.
     */
    public AlertRule(final AlertType type) {
        super();
        setType(type);
    }
    /**
     * Default constructor.
     */
    public AlertRule() {
        super();
    }

    /**
     * @return the type
     */
    public AlertType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final AlertType type) {
        switch (type) {
            case Cold:
            case CriticalCold:
            case Hot:
            case CriticalHot:
            break;

            default:
                throw new RuntimeException("Unexpected temperature alert type: " + type);
        }
        this.type = type;
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
     * @return the timeOutMinutes
     */
    public int getTimeOutMinutes() {
        return timeOutMinutes;
    }
    /**
     * @param timeOutMinutes the timeOutMinutes to set
     */
    public void setTimeOutMinutes(final int timeOutMinutes) {
        this.timeOutMinutes = timeOutMinutes;
    }
    /**
     * @return
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the cumulativeFlag
     */
    public boolean isCumulativeFlag() {
        return cumulativeFlag;
    }
    /**
     * @param cumulativeFlag the cumulativeFlag to set
     */
    public void setCumulativeFlag(final boolean cumulativeFlag) {
        this.cumulativeFlag = cumulativeFlag;
    }
}