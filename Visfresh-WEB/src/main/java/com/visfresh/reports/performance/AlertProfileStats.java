/**
 *
 */
package com.visfresh.reports.performance;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileStats {
    private String name;
    private final List<BiggestTemperatureException> temperatureExceptions = new LinkedList<>();
    private final List<MonthlyTemperatureStats> monthlyData = new LinkedList<>();
    private double lowerTemperatureLimit = 0;
    private double upperTemperatureLimit = 5;

    /**
     * Default constructor.
     */
    public AlertProfileStats() {
        super();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the monthlyData
     */
    public List<MonthlyTemperatureStats> getMonthlyData() {
        return monthlyData;
    }
    /**
     * @return the temperatureExceptions
     */
    public List<BiggestTemperatureException> getTemperatureExceptions() {
        return temperatureExceptions;
    }

    /**
     * @return lower temperature limit.
     */
    public double getLowerTemperatureLimit() {
        return lowerTemperatureLimit;
    }
    /**
     * @param lowerTemperatureLimit the lowerTemperatureLimit to set
     */
    public void setLowerTemperatureLimit(final double lowerTemperatureLimit) {
        this.lowerTemperatureLimit = lowerTemperatureLimit;
    }
    /**
     * @return upper temperature limit.
     */
    public double getUpperTemperatureLimit() {
        return upperTemperatureLimit;
    }
    /**
     * @param upperTemperatureLimit the upperTemperatureLimit to set
     */
    public void setUpperTemperatureLimit(final double upperTemperatureLimit) {
        this.upperTemperatureLimit = upperTemperatureLimit;
    }
}
