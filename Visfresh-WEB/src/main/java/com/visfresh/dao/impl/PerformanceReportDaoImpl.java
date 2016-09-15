/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.PerformanceReportDao;
import com.visfresh.entities.Company;
import com.visfresh.reports.performance.PerformanceReportBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class PerformanceReportDaoImpl implements PerformanceReportDao {
    /**
     * JDBC template.
     */
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private AlertProfileDao alertProfileDao;

    /**
     * Default constructor.
     */
    public PerformanceReportDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.PerformanceReportDao#createReport(com.visfresh.entities.Company, java.util.Date, java.util.Date)
     */
    @Override
    public PerformanceReportBean createReport(final Company c, final Date month) {
        final PerformanceReportBean bean = new PerformanceReportBean();
        return bean;
    }
}
