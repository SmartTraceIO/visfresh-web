/**
 *
 */
package com.visfresh.io;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class StartSimulatorRequest {
    private String user;
    private String startDate;
    private String endDate;
    private int velosity;

    /**
     * default constructor.
     */
    public StartSimulatorRequest() {
        super();
    }

    /**
     * @return user Email
     */
    public String getUser() {
        return user;
    }
    /**
     * @param user the user to set
     */
    public void setUser(final String user) {
        this.user = user;
    }
    /**
     * @return the startDate
     */
    public String getStartDate() {
        return startDate;
    }
    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(final String startDate) {
        this.startDate = startDate;
    }
    /**
     * @return the endDate
     */
    public String getEndDate() {
        return endDate;
    }
    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(final String endDate) {
        this.endDate = endDate;
    }
    /**
     * @return the velosity
     */
    public int getVelosity() {
        return velosity;
    }
    /**
     * @param velosity the velosity to set
     */
    public void setVelosity(final int velosity) {
        this.velosity = velosity;
    }
}
