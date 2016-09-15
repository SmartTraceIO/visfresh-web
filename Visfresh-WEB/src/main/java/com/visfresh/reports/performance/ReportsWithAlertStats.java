/**
 *
 */
package com.visfresh.reports.performance;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReportsWithAlertStats {
    private int notAlerts;
    private int hotAlerts;
    private int coldAlerts;
    private int hotAndColdAlerts;

    /**
     * Default constructor.
     */
    public ReportsWithAlertStats() {
        super();
    }

    /**
     * @return the notAlerts
     */
    public int getNotAlerts() {
        return notAlerts;
    }
    /**
     * @param notAlerts the notAlerts to set
     */
    public void setNotAlerts(final int notAlerts) {
        this.notAlerts = notAlerts;
    }
    /**
     * @return the hotAlerts
     */
    public int getHotAlerts() {
        return hotAlerts;
    }
    /**
     * @param hotAlerts the hotAlerts to set
     */
    public void setHotAlerts(final int hotAlerts) {
        this.hotAlerts = hotAlerts;
    }
    /**
     * @return the coldAlerts
     */
    public int getColdAlerts() {
        return coldAlerts;
    }
    /**
     * @param coldAlerts the coldAlerts to set
     */
    public void setColdAlerts(final int coldAlerts) {
        this.coldAlerts = coldAlerts;
    }
    /**
     * @return the hotAndColdAlerts
     */
    public int getHotAndColdAlerts() {
        return hotAndColdAlerts;
    }
    /**
     * @param hotAndColdAlerts the hotAndColdAlerts to set
     */
    public void setHotAndColdAlerts(final int hotAndColdAlerts) {
        this.hotAndColdAlerts = hotAndColdAlerts;
    }
}
