/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureRule extends AlertRule {
    private double temperature;
    private int timeOutMinutes;
    private boolean cumulativeFlag;

    /**
     * Default constructor.
     */
    public TemperatureRule(final AlertType type) {
        super(type);
    }
    /**
     * Default constructor.
     */
    public TemperatureRule() {
        super();
    }

    /**
     * @param type the type to set
     */
    @Override
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
        super.setType(type);
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
