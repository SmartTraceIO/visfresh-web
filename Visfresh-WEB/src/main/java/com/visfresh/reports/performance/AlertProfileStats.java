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
}
