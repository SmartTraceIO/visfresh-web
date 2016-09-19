/**
 *
 */
package com.visfresh.dao;

import java.util.Date;

import com.visfresh.entities.Company;
import com.visfresh.reports.performance.PerformanceReportBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface PerformanceReportDao {
    PerformanceReportBean createReport(Company c, Date startDate, Date endDate);
}
