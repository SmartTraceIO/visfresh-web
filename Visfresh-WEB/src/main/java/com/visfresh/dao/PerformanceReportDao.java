/**
 *
 */
package com.visfresh.dao;

import java.util.Date;

import com.visfresh.dao.impl.TimeAtom;
import com.visfresh.entities.Company;
import com.visfresh.entities.LocationProfile;
import com.visfresh.reports.performance.PerformanceReportBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface PerformanceReportDao {
    /**
     * @param c company.
     * @param startDate start date.
     * @param endDate end date.
     * @param timeAtom time period.
     * @param location end location.
     * @return
     */
    PerformanceReportBean createReport(Company c, Date startDate, Date endDate, TimeAtom timeAtom,
           LocationProfile location);
}
