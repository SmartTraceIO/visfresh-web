/**
 *
 */
package com.visfresh.dao.impl;

import org.junit.Test;

import com.visfresh.dao.BaseDaoTest;
import com.visfresh.dao.PerformanceReportDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PerformanceReportDaoTest extends BaseDaoTest<PerformanceReportDao> {
    /**
     * Default constructor.
     */
    public PerformanceReportDaoTest() {
        super(PerformanceReportDao.class);
    }

    @Test
    public void testTwoAlertProfilesOneMonth() {

    }
    @Test
    public void testAlertProfileTwoMonths() {

    }
    @Test
    public void testAlertProfileTwoMonthsCuttedByDateRanges() {

    }
    @Test
    public void testAlertProfileShipmentStats() {
        //two shipments with alerts, one without alerts
    }
    @Test
    public void testThreeIncedents() {

    }

}
