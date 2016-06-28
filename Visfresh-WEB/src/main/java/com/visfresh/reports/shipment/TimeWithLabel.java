/**
 *
 */
package com.visfresh.reports.shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TimeWithLabel {
    /**
     * Total time above high temp (5°C): 2hrs 12min
     * Total time above critical high temp (8°C): 1hrs 12min
     * Total time below low temp (0°C): 22min
     * Total time below critical low temp (-2C): nil
     */
    private String label;
    private long totalTime;

    /**
     * Default constructor.
     */
    public TimeWithLabel() {
        super();
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    /**
     * @param label the label to set
     */
    public void setLabel(final String label) {
        this.label = label;
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
}
