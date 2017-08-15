/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.TemperatureRule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureRuleBean extends AlertRuleBean {
    private double temperature;
    private int timeOutMinutes;
    private boolean cumulativeFlag;
    //max rate = one time per maxRateMinutes
    private Integer maxRateMinutes;
    private CorrectiveActionListBean correctiveActions;

    /**
     * Default constructor.
     */
    public TemperatureRuleBean() {
        super();
    }
    /**
     * @param r temperature rule.
     */
    public TemperatureRuleBean(final TemperatureRule r) {
        super(r);
        setTemperature(r.getTemperature());
        setTimeOutMinutes(r.getTimeOutMinutes());
        setCumulativeFlag(r.isCumulativeFlag());
        setMaxRateMinutes(r.getMaxRateMinutes());

        if (r.getCorrectiveActions() != null) {
            setCorrectiveActions(new CorrectiveActionListBean(r.getCorrectiveActions()));
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
    public boolean hasCumulativeFlag() {
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
    /**
     * @return the correctiveActions
     */
    public CorrectiveActionListBean getCorrectiveActions() {
        return correctiveActions;
    }
    /**
     * @param correctiveActions the correctiveActions to set
     */
    public void setCorrectiveActions(final CorrectiveActionListBean correctiveActions) {
        this.correctiveActions = correctiveActions;
    }
}
