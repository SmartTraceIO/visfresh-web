/**
 *
 */
package com.visfresh.reports.performance;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.dao.impl.TimeAtom;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PerformanceReportBean {
    private String companyName;
    private Date date;
    private final List<AlertProfileStats> alertProfiles = new LinkedList<>();
    private TimeAtom timeAtom;
    private String locationName;

    /**
     * Default constructor.
     */
    public PerformanceReportBean() {
        super();
    }

    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return companyName;
    }
    /**
     * @param companyName the companyName to set
     */
    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }
    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(final Date date) {
        this.date = date;
    }
    /**
     * @return the alertProfiles
     */
    public List<AlertProfileStats> getAlertProfiles() {
        return alertProfiles;
    }

    /**
     * @param atom the time atom.
     */
    public void setTimeAtom(final TimeAtom atom) {
        this.timeAtom = atom;
    }
    /**
     * @return the timeAtom
     */
    public TimeAtom getTimeAtom() {
        return timeAtom;
    }
    /**
     * @return the locationName
     */
    public String getLocationName() {
        return locationName;
    }
    /**
     * @param locationName the locationName to set
     */
    public void setLocationName(final String locationName) {
        this.locationName = locationName;
    }
}
