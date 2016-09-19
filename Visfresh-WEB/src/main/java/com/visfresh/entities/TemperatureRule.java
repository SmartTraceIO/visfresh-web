/**
 *
 */
package com.visfresh.entities;

import org.apache.commons.lang3.builder.HashCodeBuilder;

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
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof TemperatureRule)) {
            return false;
        }

        final TemperatureRule other = (TemperatureRule) obj;

        return
                getType() == other.getType() &&
                getTemperature() == other.getTemperature() &&
                getTimeOutMinutes() == other.getTimeOutMinutes() &&
                isCumulativeFlag() == other.isCumulativeFlag();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hb = new HashCodeBuilder();
        hb.append(getType());
        hb.append(getTemperature());
        hb.append(getTimeOutMinutes());
        hb.append(isCumulativeFlag());
        return hb.toHashCode();
    }
}
