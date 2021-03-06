/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureRule extends AlertRule {
    private double temperature;
    private int timeOutMinutes;
    private boolean cumulativeFlag;
    //max rate = one time per maxRateMinutes
    private Integer maxRateMinutes;
    private CorrectiveActionList correctiveActions;

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
    /**
     * @return the maxRateMinutes
     */
    public Integer getMaxRateMinutes() {
        return maxRateMinutes;
    }
    /**
     * @param maxRateMinutes the maxRateMinutes to set
     */
    public void setMaxRateMinutes(final Integer maxRateMinutes) {
        this.maxRateMinutes = maxRateMinutes;
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
                isCumulativeFlag() == other.isCumulativeFlag() &&
                Objects.equals(getMaxRateMinutes(), other.getMaxRateMinutes()) &&
                Objects.equals(getCorrectiveActions(), other.getCorrectiveActions());
    }
    /**
     * @return the correctiveActions
     */
    public CorrectiveActionList getCorrectiveActions() {
        return correctiveActions;
    }
    /**
     * @param correctiveActions the correctiveActions to set
     */
    public void setCorrectiveActions(final CorrectiveActionList correctiveActions) {
        this.correctiveActions = correctiveActions;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final List<Object> hb = new LinkedList<>();
        hb.add(getType());
        hb.add(getTemperature());
        hb.add(getTimeOutMinutes());
        hb.add(isCumulativeFlag());
        hb.add(getMaxRateMinutes());
        hb.add(getCorrectiveActions());
        return Objects.hash(hb.toArray(new Object[hb.size()]));
    }
}
