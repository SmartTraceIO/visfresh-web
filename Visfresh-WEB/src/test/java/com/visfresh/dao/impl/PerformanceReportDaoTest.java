/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;

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

    /**
     * Tests only the method not throws any exception.
     * Should be removed after strong tests implemented.
     */
    @Test
    public void testJustSelect() {
        dao.createReport(sharedCompany, new Date(System.currentTimeMillis() - 1000000000l), new Date());
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
