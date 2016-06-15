/**
 *
 */
package com.visfresh.reports.performance;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.TemperatureRule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureRuleStats {
    private TemperatureRule rule;
    private long totalTime;
    private List<BiggestTemperatureException> biggestExceptions = new LinkedList<>();

    /**
     * Default constructor.
     */
    public TemperatureRuleStats() {
        super();
    }

    /**
     * @return the rule
     */
    public TemperatureRule getRule() {
        return rule;
    }
    /**
     * @param rule the rule to set
     */
    public void setRule(final TemperatureRule rule) {
        this.rule = rule;
    }
    /**
     * @return the totalTime
     */
    public long getTotalTime() {
        return totalTime;
    }
    /**
     * @param totalTime the totalTime to set
     */
    public void setTotalTime(final long totalTime) {
        this.totalTime = totalTime;
    }
    /**
     * @return the biggestExceptions
     */
    public List<BiggestTemperatureException> getBiggestExceptions() {
        return biggestExceptions;
    }
    /**
     * @param biggestExceptions the biggestExceptions to set
     */
    public void setBiggestExceptions(
            final List<BiggestTemperatureException> biggestExceptions) {
        this.biggestExceptions = biggestExceptions;
    }
}
