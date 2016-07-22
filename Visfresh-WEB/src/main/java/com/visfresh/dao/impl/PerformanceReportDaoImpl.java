/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.PerformanceReportDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.reports.performance.AlertProfileStats;
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
    public PerformanceReportBean createReport(final Company c, final Date startDate,
            final Date endDate) {
        final PerformanceReportBean bean = new PerformanceReportBean();
        bean.setStartDate(startDate);
        bean.setEndDate(endDate);

        //tracker statistics
        setTrackerStats(c, bean);

        //alert statistics
        setAlertsStats(c, bean);

        return bean;
    }

    /**
     * @param c company.
     * @param bean performance report bean.
     */
    private void setAlertsStats(final Company c, final PerformanceReportBean bean) {
        final Map<Long, Set<Long>> alertProfilesToShipmentMap = getAlertProfilesByShipments(
                c, bean.getStartDate(), bean.getEndDate());

        final List<AlertProfile> aps = alertProfileDao.findAll(alertProfilesToShipmentMap.keySet());
        for (final AlertProfile a : aps) {
            bean.getAlertProfiles().add(createAlertProfileStats(a, bean.getStartDate(), bean.getEndDate()));
        }
    }

    /**
     * @param a
     * @param startDate
     * @param endDate
     * @return
     */
    private AlertProfileStats createAlertProfileStats(final AlertProfile a,
            final Date startDate, final Date endDate) {
        return new AlertProfileStats();
    }

    /**
     * @param c
     * @param startDate
     * @param endDate
     * @return
     */
    private Map<Long, Set<Long>> getAlertProfilesByShipments(final Company c,
            final Date startDate, final Date endDate) {
        final Map<Long, Set<Long>> result = new HashMap<>();
        // TODO Auto-generated method stub
        return result;
    }

    /**
     * @param c company.
     * @param bean performance report bean.
     */
    private void setTrackerStats(final Company c, final PerformanceReportBean bean) {
        final String sql = "select d.imei as device, count(s.id) as numShipments"
                + " from devices d"
                + " left outer join shipments s on s.device = d.imei"
                + " group by d.imei, d.company"
                + " having d.imei regexp '[0-9]+' and d.company = :company";
        final Map<String, Object> params = new HashMap<>();
        params.put("company", c.getId());

        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        //number of trackers.
        bean.setNumberOfTrackers(rows.size());
        if (bean.getNumberOfTrackers() == 0) {
            return;
        }

        //number of shipments
        double tpsSumm = 0;
        int numShipments = 0;
        for (final Map<String, Object> map : rows) {
            final int num = ((Number) map.get("numShipments")).intValue();
            numShipments += num;
            tpsSumm = 1.0 / num;
        }
        bean.setNumberOfShipments(numShipments);

        //avg
        bean.setAvgShipmentsPerTracker(
                (double) numShipments / bean.getNumberOfTrackers());
        bean.setAvgTrackersPerShipment(tpsSumm / bean.getNumberOfTrackers());
    }
}
