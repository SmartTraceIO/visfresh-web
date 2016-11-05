/**
 *
 */
package com.visfresh.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.BaseDaoTest;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.PerformanceReportDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.reports.performance.AlertProfileStats;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.performance.ReportsWithAlertStats;
import com.visfresh.reports.performance.TimeRangesTemperatureStats;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PerformanceReportDaoTest extends BaseDaoTest<PerformanceReportDao> {
    private Device device;

    /**
     * Default constructor.
     */
    public PerformanceReportDaoTest() {
        super(PerformanceReportDao.class);
    }

    @Before
    public void setUp() {
        device = createDevice(sharedCompany, "9283749873434");
    }

    /**
     * Tests only the method not throws any exception.
     * Should be removed after strong tests implemented.
     */
    @Test
    public void testJustSelect() {
        dao.createReport(sharedCompany, new Date(System.currentTimeMillis() - 1000000000l), new Date(),
                TimeAtom.Month, null);
    }
    @Test
    public void testShipmentStats() {
        final AlertProfile ap = createAlertProfile("JUnit-AlertProfile");
        final Shipment s1 = createShipment(ap);
        final Shipment s2 = createShipment(ap);
        final Shipment s3 = createShipment(ap);
        final Shipment s4 = createShipment(ap);
        createShipment(ap);

        final Date time = getMiddleOfMonth("2016.08");

        final TrackerEvent e1 = createEvent(s1, time, 1.);
        final TrackerEvent e2 = createEvent(s2, time, 1.);
        final TrackerEvent e3 = createEvent(s3, time, 1.);
        createEvent(s4, time, 1.);

        createAlert(e1, AlertType.Hot);
        createAlert(e1, AlertType.Hot);
        createAlert(e1, AlertType.CriticalHot);
        createAlert(e2, AlertType.Cold);
        createAlert(e2, AlertType.Cold);
        createAlert(e2, AlertType.CriticalCold);
        createAlert(e3, AlertType.Hot);
        createAlert(e3, AlertType.Cold);

        final PerformanceReportBean report = dao.createReport(ap.getCompany(),
                new Date(time.getTime() - 10000000l), new Date(time.getTime() + 10000000l), TimeAtom.Month, null);

        assertNotNull(report);
        assertEquals(1, report.getAlertProfiles().size());

        final List<TimeRangesTemperatureStats> monthlyStats = report.getAlertProfiles().get(0).getTimedData();
        assertEquals(1, monthlyStats.size());

        final TimeRangesTemperatureStats ms = monthlyStats.get(0);
        assertEquals(4, ms.getNumShipments());

        final ReportsWithAlertStats stats = ms.getAlertStats();
        assertEquals(1, stats.getHotAlerts());
        assertEquals(1, stats.getColdAlerts());
        assertEquals(1, stats.getHotAndColdAlerts());
        assertEquals(1, stats.getNotAlerts());
    }
    @Test
    public void testExcludeNotArrivedShipments() {
        final AlertProfile ap = createAlertProfile("JUnit-AlertProfile");
        final Shipment s1 = createShipment(ap, ShipmentStatus.Default);
        final Shipment s2 = createShipment(ap, ShipmentStatus.Ended);
        final Shipment s3 = createShipment(ap, ShipmentStatus.Ended);
        final Shipment s4 = createShipment(ap, ShipmentStatus.Default);
        createShipment(ap, ShipmentStatus.Default);

        final Date time = getMiddleOfMonth("2016.08");

        final TrackerEvent e1 = createEvent(s1, time, 1.);
        final TrackerEvent e2 = createEvent(s2, time, 1.);
        final TrackerEvent e3 = createEvent(s3, time, 1.);
        createEvent(s4, time, 1.);

        createAlert(e1, AlertType.Hot);
        createAlert(e1, AlertType.Hot);
        createAlert(e1, AlertType.CriticalHot);
        createAlert(e2, AlertType.Cold);
        createAlert(e2, AlertType.Cold);
        createAlert(e2, AlertType.CriticalCold);
        createAlert(e3, AlertType.Hot);
        createAlert(e3, AlertType.Cold);

        final PerformanceReportBean report = dao.createReport(ap.getCompany(),
                new Date(time.getTime() - 10000000l), new Date(time.getTime() + 10000000l), TimeAtom.Month, null);

        assertNotNull(report);
        assertEquals(1, report.getAlertProfiles().size());

        final List<TimeRangesTemperatureStats> monthlyStats = report.getAlertProfiles().get(0).getTimedData();
        assertEquals(1, monthlyStats.size());

        final TimeRangesTemperatureStats ms = monthlyStats.get(0);
        assertEquals(0, ms.getNumShipments());

        final ReportsWithAlertStats stats = ms.getAlertStats();
        assertEquals(0, stats.getHotAlerts());
        assertEquals(0, stats.getColdAlerts());
        assertEquals(0, stats.getHotAndColdAlerts());
        assertEquals(0, stats.getNotAlerts());
    }
    @Test
    public void testWithEndLocation() {
        final AlertProfile ap = createAlertProfile("JUnit-AlertProfile");
        final Shipment s1 = createShipment(ap, ShipmentStatus.Arrived);
        final LocationProfile l1 = createLocation("L1", 1., 1.);
        s1.setShippedTo(l1);
        context.getBean(ShipmentDao.class).save(s1);

        final Shipment s2 = createShipment(ap, ShipmentStatus.Arrived);
        final LocationProfile l2 = createLocation("L2", 1., 1.);
        s2.setShippedTo(l2);
        context.getBean(ShipmentDao.class).save(s2);

        final Date time = getMiddleOfMonth("2016.08");

        final TrackerEvent e1 = createEvent(s1, time, 1.);
        final TrackerEvent e2 = createEvent(s2, time, 1.);

        createAlert(e1, AlertType.Hot);
        createAlert(e1, AlertType.Hot);
        createAlert(e1, AlertType.CriticalHot);
        createAlert(e2, AlertType.Cold);
        createAlert(e2, AlertType.Cold);
        createAlert(e2, AlertType.CriticalCold);

        final PerformanceReportBean report = dao.createReport(ap.getCompany(),
                new Date(time.getTime() - 10000000l), new Date(time.getTime() + 10000000l), TimeAtom.Month, l1);

        assertNotNull(report);
        assertEquals(1, report.getAlertProfiles().size());

        final List<TimeRangesTemperatureStats> monthlyStats = report.getAlertProfiles().get(0).getTimedData();
        assertEquals(1, monthlyStats.size());

        final TimeRangesTemperatureStats ms = monthlyStats.get(0);
        assertEquals(1, ms.getNumShipments());

        final ReportsWithAlertStats stats = ms.getAlertStats();
        assertEquals(1, stats.getHotAlerts());
        assertEquals(0, stats.getColdAlerts());
        assertEquals(0, stats.getHotAndColdAlerts());
        assertEquals(0, stats.getNotAlerts());
    }
    @Test
    public void testShipmentStatsTwoMonths() {
        final AlertProfile ap = createAlertProfile("JUnit-AlertProfile");
        final Shipment s1 = createShipment(ap);

        final Date d1 = getMiddleOfMonth("2016.07");
        final Date d2 = getMiddleOfMonth("2016.08");

        final TrackerEvent e1 = createEvent(s1, d1, 1.);
        final TrackerEvent e2 = createEvent(s1, d2, 1.);

        createAlert(e1, AlertType.Hot);
        createAlert(e2, AlertType.Cold);

        final PerformanceReportBean report = dao.createReport(ap.getCompany(),
                new Date(d1.getTime() - 10000000l), new Date(d2.getTime() + 10000000l), TimeAtom.Month, null);

        assertNotNull(report);
        assertEquals(1, report.getAlertProfiles().size());

        final List<TimeRangesTemperatureStats> monthlyStats = report.getAlertProfiles().get(0).getTimedData();
        assertEquals(2, monthlyStats.size());

        //first month
        TimeRangesTemperatureStats ms = monthlyStats.get(0);
        assertEquals(1, ms.getNumShipments());

        ReportsWithAlertStats stats = ms.getAlertStats();
        assertEquals(1, stats.getHotAlerts());
        assertEquals(0, stats.getColdAlerts());
        assertEquals(0, stats.getHotAndColdAlerts());
        assertEquals(0, stats.getNotAlerts());

        //second month
        ms = monthlyStats.get(1);
        assertEquals(1, ms.getNumShipments());

        stats = ms.getAlertStats();
        assertEquals(0, stats.getHotAlerts());
        assertEquals(1, stats.getColdAlerts());
        assertEquals(0, stats.getHotAndColdAlerts());
        assertEquals(0, stats.getNotAlerts());
    }
    @Test
    public void testShipmentStatsWeeks() throws ParseException {
        final AlertProfile ap = createAlertProfile("JUnit-AlertProfile");
        final Shipment s1 = createShipment(ap);

        final DateFormat fmt = new SimpleDateFormat("yyyy.MM.dd");
        final Date d1 = fmt.parse("2016.10.03");
        final Date d2 = fmt.parse("2016.11.03");

        createEvent(s1, d1, 1.);
        createEvent(s1, d2, 1.);

        final PerformanceReportBean report = dao.createReport(ap.getCompany(), d1, d2, TimeAtom.Week, null);

        assertNotNull(report);
        assertEquals(1, report.getAlertProfiles().size());

        final List<TimeRangesTemperatureStats> weeklyStats = report.getAlertProfiles().get(0).getTimedData();
        assertEquals(5, weeklyStats.size());
    }
    @Test
    public void testShipmentStatsWeeksByNextYear() throws ParseException {
        final AlertProfile ap = createAlertProfile("JUnit-AlertProfile");
        final Shipment s1 = createShipment(ap);

        final DateFormat fmt = new SimpleDateFormat("yyyy.MM.dd");
        final Date d1 = fmt.parse("2015.12.23");
        final Date d2 = fmt.parse("2016.01.08");

        createEvent(s1, d1, 1.);
        createEvent(s1, d2, 1.);

        final PerformanceReportBean report = dao.createReport(ap.getCompany(), d1, d2, TimeAtom.Week, null);

        assertNotNull(report);
        assertEquals(1, report.getAlertProfiles().size());

        final List<TimeRangesTemperatureStats> weeklyStats = report.getAlertProfiles().get(0).getTimedData();
        assertEquals(3, weeklyStats.size());
    }
    @Test
    public void testShipmentStatsQuarters() throws ParseException {
        final AlertProfile ap = createAlertProfile("JUnit-AlertProfile");
        final Shipment s1 = createShipment(ap);

        final DateFormat fmt = new SimpleDateFormat("yyyy.MM.dd");
        final Date d1 = fmt.parse("2015.11.03");
        final Date d2 = fmt.parse("2016.04.01");

        createEvent(s1, d1, 1.);
        createEvent(s1, d2, 1.);

        final PerformanceReportBean report = dao.createReport(ap.getCompany(), d1, d2, TimeAtom.Quarter, null);

        assertNotNull(report);
        assertEquals(1, report.getAlertProfiles().size());

        final List<TimeRangesTemperatureStats> quearterStats = report.getAlertProfiles().get(0).getTimedData();
        assertEquals(3, quearterStats.size());
    }
    @Test
    public void testShipmentStatsTwoAlertProfiles() {
        final String name1 = "JUnit-AlertProfile-1";
        final AlertProfile ap1 = createAlertProfile(name1);
        final String name2 = "JUnit-AlertProfile-2";
        final AlertProfile ap2 = createAlertProfile(name2);

        final Shipment s1 = createShipment(ap1);
        final Shipment s2 = createShipment(ap2);

        final Date date = getMiddleOfMonth("2016.07");

        final TrackerEvent e1 = createEvent(s1, date, 1.);
        final TrackerEvent e2 = createEvent(s2, date, 1.);

        createAlert(e1, AlertType.Hot);
        createAlert(e2, AlertType.Cold);

        final PerformanceReportBean report = dao.createReport(ap1.getCompany(),
                new Date(date.getTime() - 10000000l), new Date(date.getTime() + 10000000l), TimeAtom.Month, null);

        assertNotNull(report);
        assertEquals(2, report.getAlertProfiles().size());

        final List<TimeRangesTemperatureStats> md1 = getAlertProfileStats(report, name1).getTimedData();
        assertEquals(1, md1.size());
        final List<TimeRangesTemperatureStats> md2 = getAlertProfileStats(report, name2).getTimedData();
        assertEquals(1, md2.size());

        //first month
        TimeRangesTemperatureStats ms = md1.get(0);
        assertEquals(1, ms.getNumShipments());

        ReportsWithAlertStats stats = ms.getAlertStats();
        assertEquals(1, stats.getHotAlerts());
        assertEquals(0, stats.getColdAlerts());
        assertEquals(0, stats.getHotAndColdAlerts());
        assertEquals(0, stats.getNotAlerts());

        //second month
        ms = md2.get(0);
        assertEquals(1, ms.getNumShipments());

        stats = ms.getAlertStats();
        assertEquals(0, stats.getHotAlerts());
        assertEquals(1, stats.getColdAlerts());
        assertEquals(0, stats.getHotAndColdAlerts());
        assertEquals(0, stats.getNotAlerts());
    }
    @Test
    public void testTemperatureStats() {
        final AlertProfile ap = createAlertProfile("JUnit");
        ap.setUpperTemperatureLimit(15.3);
        ap.setLowerTemperatureLimit(-11.2);
        context.getBean(AlertProfileDao.class).save(ap);

        final Shipment s1 = createShipment(ap);
        final Shipment s2 = createShipment(ap);

        final long dt = 10 * 60 * 1000l;
        final long startTime = getMiddleOfMonth("2016.08").getTime() - dt * 1000;

        createEvent(s1, new Date(startTime + 1 * dt), 20.7);
        createEvent(s2, new Date(startTime + 2 * dt), 15.7);
        createEvent(s1, new Date(startTime + 3 * dt), 10);
        createEvent(s2, new Date(startTime + 4 * dt), 8);
        createEvent(s1, new Date(startTime + 5 * dt), 6);
        createEvent(s2, new Date(startTime + 6 * dt), 0);
        createEvent(s1, new Date(startTime + 7 * dt), -11.5);
        createEvent(s2, new Date(startTime + 8 * dt), -20.3);
        createEvent(s1, new Date(startTime + 9 * dt), 0 * dt);
        createEvent(s2, new Date(startTime + 10 * dt), 0 * dt);

        final PerformanceReportBean report = dao.createReport(s1.getCompany(),
                new Date(startTime - 10000000l), new Date(startTime + dt * 15), TimeAtom.Month, null);

        final List<TrackerEvent> events = context.getBean(TrackerEventDao.class).findAll(
                null, null, null);
        double summ = 0;
        for (final TrackerEvent e : events) {
            summ += e.getTemperature();
        }

        final TemperatureStats stats = report.getAlertProfiles().get(0).getTimedData().get(0).getTemperatureStats();
        assertEquals(summ / events.size(), stats.getAvgTemperature(), 0.0001);
        assertEquals(-20.3, stats.getMinimumTemperature(), 0.0001);
        assertEquals(20.7, stats.getMaximumTemperature(), 0.0001);
        assertEquals(4 * dt, stats.getTimeAboveUpperLimit());
        assertEquals(4 * dt, stats.getTimeBelowLowerLimit());
        assertEquals((8 + 8) * dt, stats.getTotalTime());
    }
    @Test
    public void testTemperatureStatsTwoMonths() {
        final AlertProfile ap = createAlertProfile("JUnit");
        ap.setUpperTemperatureLimit(15.3);
        ap.setLowerTemperatureLimit(-11.2);
        context.getBean(AlertProfileDao.class).save(ap);

        final Shipment s1 = createShipment(ap);
        final Shipment s2 = createShipment(ap);

        final long dt = 10 * 60 * 1000l;
        final long startTime1 = getMiddleOfMonth("2016.07").getTime() - dt * 1000;

        createEvent(s1, new Date(startTime1 + 1 * dt), 20.7);
        createEvent(s2, new Date(startTime1 + 2 * dt), 20.7);
        createEvent(s1, new Date(startTime1 + 3 * dt), 20.7);
        createEvent(s2, new Date(startTime1 + 4 * dt), 20.7);

        final long startTime2 = getMiddleOfMonth("2016.08").getTime() - dt * 1000;

        createEvent(s1, new Date(startTime2 + 1 * dt), 17.7);
        createEvent(s2, new Date(startTime2 + 2 * dt), 17.7);
        createEvent(s1, new Date(startTime2 + 3 * dt), 17.7);
        createEvent(s2, new Date(startTime2 + 4 * dt), 17.7);

        final PerformanceReportBean report = dao.createReport(s1.getCompany(),
                new Date(startTime1 - 10000000l), new Date(startTime2 + dt * 15), TimeAtom.Month, null);

        TemperatureStats stats = report.getAlertProfiles().get(0).getTimedData().get(0).getTemperatureStats();
        assertEquals(20.7, stats.getAvgTemperature(), 0.0001);
        assertEquals(20.7, stats.getMinimumTemperature(), 0.0001);
        assertEquals(20.7, stats.getMaximumTemperature(), 0.0001);
        assertEquals((2 + 2) * dt, stats.getTotalTime());

        stats = report.getAlertProfiles().get(0).getTimedData().get(1).getTemperatureStats();
        assertEquals(17.7, stats.getAvgTemperature(), 0.0001);
        assertEquals(17.7, stats.getMinimumTemperature(), 0.0001);
        assertEquals(17.7, stats.getMaximumTemperature(), 0.0001);
        assertEquals((2 + 2) * dt, stats.getTotalTime());
    }
    @Test
    public void testTemperatureStatsTwoAlertProfiles() {
        final String name1 = "JUnit-1";
        final AlertProfile ap1 = createAlertProfile(name1);
        final String name2 = "JUnit-2";
        final AlertProfile ap2 = createAlertProfile(name2);

        final Shipment s1 = createShipment(ap1);
        final Shipment s2 = createShipment(ap2);

        final long dt = 10 * 60 * 1000l;
        final long startTime1 = getMiddleOfMonth("2016.07").getTime() - dt * 1000;

        createEvent(s1, new Date(startTime1 + 1 * dt), 20.7);
        createEvent(s1, new Date(startTime1 + 2 * dt), 20.7);
        createEvent(s1, new Date(startTime1 + 3 * dt), 20.7);
        createEvent(s1, new Date(startTime1 + 4 * dt), 20.7);

        final long startTime2 = getMiddleOfMonth("2016.08").getTime() - dt * 1000;

        createEvent(s2, new Date(startTime2 + 1 * dt), 17.7);
        createEvent(s2, new Date(startTime2 + 2 * dt), 17.7);
        createEvent(s2, new Date(startTime2 + 3 * dt), 17.7);
        createEvent(s2, new Date(startTime2 + 4 * dt), 17.7);

        final PerformanceReportBean report = dao.createReport(s1.getCompany(),
                new Date(startTime1 - 10000000l), new Date(startTime2 + dt * 15), TimeAtom.Month, null);

        assertEquals(2, report.getAlertProfiles().size());

        TemperatureStats stats = getAlertProfileStats(report, name1).getTimedData().get(0).getTemperatureStats();
        assertEquals(20.7, stats.getAvgTemperature(), 0.0001);
        assertEquals(20.7, stats.getMinimumTemperature(), 0.0001);
        assertEquals(20.7, stats.getMaximumTemperature(), 0.0001);
        assertEquals(3 * dt, stats.getTotalTime());

        stats = getAlertProfileStats(report, name2).getTimedData().get(1).getTemperatureStats();
        assertEquals(17.7, stats.getAvgTemperature(), 0.0001);
        assertEquals(17.7, stats.getMinimumTemperature(), 0.0001);
        assertEquals(17.7, stats.getMaximumTemperature(), 0.0001);
        assertEquals(3 * dt, stats.getTotalTime());
    }

    //biggest exceptions
    @Test
    public void testThreeBigestExceptions() {
        final AlertProfile ap = createAlertProfile("JUnit");
        ap.setUpperTemperatureLimit(15.3);
        ap.setLowerTemperatureLimit(-11.2);
        context.getBean(AlertProfileDao.class).save(ap);

        final Shipment s1 = createShipment(ap);
        final Shipment s2 = createShipment(ap);
        final Shipment s3 = createShipment(ap);

        final long dt = 10 * 60 * 1000l;
        final long startTime1 = getMiddleOfMonth("2016.07").getTime() - dt * 1000;

        createEvent(s1, new Date(startTime1 + 1 * dt), 20.7);
        createEvent(s2, new Date(startTime1 + 2 * dt), 20.7);
        createEvent(s1, new Date(startTime1 + 3 * dt), 20.7);
        createEvent(s2, new Date(startTime1 + 4 * dt), 20.7);

        final long startTime2 = getMiddleOfMonth("2016.08").getTime() - dt * 1000;

        createEvent(s1, new Date(startTime2 + 1 * dt), 17.7);
        createEvent(s3, new Date(startTime2 + 1 * dt), 0);
        createEvent(s2, new Date(startTime2 + 2 * dt), 17.7);
        createEvent(s3, new Date(startTime2 + 2 * dt), 0);
        createEvent(s1, new Date(startTime2 + 3 * dt), 17.7);
        createEvent(s3, new Date(startTime2 + 3 * dt), 0);
        createEvent(s2, new Date(startTime2 + 4 * dt), 17.7);
        createEvent(s3, new Date(startTime2 + 4 * dt), 0);

        final PerformanceReportBean report = dao.createReport(s1.getCompany(),
                new Date(startTime1 - 10000000l), new Date(startTime2 + dt * 15), TimeAtom.Month, null);

        assertEquals(2, report.getAlertProfiles().get(0).getTemperatureExceptions().size());
    }
    @Test
    public void testThreeBigestExceptionsTwoAlertProfiles() {
        final String name1 = "JUnit-1";
        final AlertProfile ap1 = createAlertProfile(name1);
        ap1.setUpperTemperatureLimit(15.3);
        ap1.setLowerTemperatureLimit(-11.2);

        final String name2 = "JUnit-2";
        final AlertProfile ap2 = createAlertProfile(name2);
        ap2.setUpperTemperatureLimit(15.3);
        ap2.setLowerTemperatureLimit(-11.2);

        context.getBean(AlertProfileDao.class).save(ap1);
        context.getBean(AlertProfileDao.class).save(ap2);

        final Shipment s1 = createShipment(ap1);
        final Shipment s2 = createShipment(ap2);
        final Shipment s3 = createShipment(ap1);

        final long dt = 10 * 60 * 1000l;
        final long startTime1 = getMiddleOfMonth("2016.07").getTime() - dt * 1000;

        createEvent(s1, new Date(startTime1 + 1 * dt), 20.7);
        createEvent(s2, new Date(startTime1 + 2 * dt), 20.7);
        createEvent(s1, new Date(startTime1 + 3 * dt), 20.7);
        createEvent(s2, new Date(startTime1 + 4 * dt), 20.7);

        final long startTime2 = getMiddleOfMonth("2016.08").getTime() - dt * 1000;

        createEvent(s1, new Date(startTime2 + 1 * dt), 17.7);
        createEvent(s3, new Date(startTime2 + 1 * dt), 0);
        createEvent(s2, new Date(startTime2 + 2 * dt), 17.7);
        createEvent(s3, new Date(startTime2 + 2 * dt), 0);
        createEvent(s1, new Date(startTime2 + 3 * dt), 17.7);
        createEvent(s3, new Date(startTime2 + 3 * dt), 0);
        createEvent(s2, new Date(startTime2 + 4 * dt), 17.7);
        createEvent(s3, new Date(startTime2 + 4 * dt), 0);

        final PerformanceReportBean report = dao.createReport(s1.getCompany(),
                new Date(startTime1 - 10000000l), new Date(startTime2 + dt * 15), TimeAtom.Month, null);

        assertEquals(1, getAlertProfileStats(report, name1).getTemperatureExceptions().size());
        assertEquals(1, getAlertProfileStats(report, name2).getTemperatureExceptions().size());
    }
    @Test
    public void testNotCrushWithoutMonthlyData() {
        final AlertProfile ap = createAlertProfile("JUnit");
        final PerformanceReportBean r = dao.createReport(ap.getCompany(),
                getMiddleOfMonth("2016.05"), getMiddleOfMonth("2016.08"), TimeAtom.Month, null);

        assertNotNull(r);
    }

    /**
     * @param ap alert profile
     * @return
     */
    private Shipment createShipment(final AlertProfile ap) {
        return createShipment(ap, ShipmentStatus.Arrived);
    }

    /**
     * @param ap
     * @param status
     * @return
     */
    protected Shipment createShipment(final AlertProfile ap,
            final ShipmentStatus status) {
        final Shipment s = new Shipment();
        s.setAlertProfile(ap);
        s.setCompany(device.getCompany());
        s.setDevice(device);
        s.setStatus(status);
        return context.getBean(ShipmentDao.class).save(s);
    }
    /**
     * @param name alert profile name.
     * @return alert profile.
     */
    protected AlertProfile createAlertProfile(final String name) {
        final AlertProfile ap = new AlertProfile();
        ap.setCompany(sharedCompany);
        ap.setDescription("JUnit test alert pforile");
        ap.setName(name);
        ap.setLowerTemperatureLimit(-2.5);
        ap.setUpperTemperatureLimit(8.5);

        final int normalTemperature = 3;
        TemperatureRule criticalHot = new TemperatureRule(AlertType.CriticalHot);
        criticalHot.setTemperature(normalTemperature + 15);
        criticalHot.setTimeOutMinutes(0);
        criticalHot.setCumulativeFlag(true);
        ap.getAlertRules().add(criticalHot);

        criticalHot = new TemperatureRule(AlertType.CriticalHot);
        criticalHot.setTemperature(normalTemperature + 14);
        criticalHot.setTimeOutMinutes(1);
        criticalHot.setCumulativeFlag(true);
        ap.getAlertRules().add(criticalHot);

        TemperatureRule criticalLow = new TemperatureRule(AlertType.CriticalCold);
        criticalLow.setTemperature(normalTemperature -15.);
        criticalLow.setTimeOutMinutes(0);
        criticalLow.setCumulativeFlag(true);
        ap.getAlertRules().add(criticalLow);

        criticalLow = new TemperatureRule(AlertType.CriticalCold);
        criticalLow.setTemperature(normalTemperature -14.);
        criticalLow.setTimeOutMinutes(1);
        criticalLow.setCumulativeFlag(true);
        ap.getAlertRules().add(criticalLow);

        TemperatureRule hot = new TemperatureRule(AlertType.Hot);
        hot.setTemperature(normalTemperature + 3);
        hot.setTimeOutMinutes(0);
        hot.setCumulativeFlag(true);
        ap.getAlertRules().add(hot);

        hot = new TemperatureRule(AlertType.Hot);
        hot.setTemperature(normalTemperature + 4.);
        hot.setTimeOutMinutes(2);
        hot.setCumulativeFlag(true);
        ap.getAlertRules().add(hot);

        TemperatureRule low = new TemperatureRule(AlertType.Cold);
        low.setTemperature(normalTemperature -10.);
        low.setTimeOutMinutes(40);
        low.setCumulativeFlag(true);
        ap.getAlertRules().add(low);

        low = new TemperatureRule(AlertType.Cold);
        low.setTemperature(normalTemperature-8.);
        low.setTimeOutMinutes(55);
        low.setCumulativeFlag(true);
        ap.getAlertRules().add(low);

        return context.getBean(AlertProfileDao.class).save(ap);
    }
    /**
     * @param company company.
     * @param imei device IMEI.
     * @return device.
     */
    private Device createDevice(final Company company, final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setActive(true);
        d.setName("JUnit-" + imei);
        d.setCompany(company);
        return context.getBean(DeviceDao.class).save(d);
    }
    /**
     * @param name location name.
     * @param lat latitude.
     * @param lon longitude.
     * @return location.
     */
    private LocationProfile createLocation(final String name, final double lat, final double lon) {
        final LocationProfile loc = new LocationProfile();
        loc.setName(name);
        loc.getLocation().setLatitude(lat);
        loc.getLocation().setLongitude(lon);
        loc.setCompany(sharedCompany);
        loc.setRadius(500);
        loc.setStop(true);
        loc.setAddress("Odessa");
        return context.getBean(LocationProfileDao.class).save(loc);
    }
    /**
     * @param s shipment.
     * @param time event time.
     * @param t temperature.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Shipment s, final Date time, final double t) {
        final TrackerEvent e = new TrackerEvent();
        e.setShipment(s);
        e.setDevice(s.getDevice());
        e.setCreatedOn(time);
        e.setTime(time);
        e.setTemperature(t);
        e.setType(TrackerEventType.AUT);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    /**
     * @param ap alert profile.
     * @param e tracker event.
     * @return temperature alert.
     */
    private TemperatureAlert createAlert(final TrackerEvent e, final AlertType type) {
        TemperatureRule rule = null;

        for (final TemperatureRule r : e.getShipment().getAlertProfile().getAlertRules()) {
            if (r.getType() == type) {
                rule = r;
                break;
            }
        }

        final TemperatureAlert a = new TemperatureAlert();
        a.setDate(e.getTime());
        a.setDevice(e.getShipment().getDevice());
        a.setRuleId(rule.getId());
        a.setShipment(e.getShipment());
        a.setTemperature(rule.getTemperature());
        a.setTrackerEventId(e.getId());
        a.setType(rule.getType());

        return context.getBean(AlertDao.class).save(a);
    }
    /**
     * @param month
     * @return
     */
    private static Date getMiddleOfMonth(final String month) {
        try {
            final Date date = new SimpleDateFormat("yyyy.MM").parse(month);
            final Calendar c = new GregorianCalendar();
            c.setTime(date);
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH) / 2);
            return c.getTime();
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @param report report bean.
     * @param name alert profile name.
     * @return alert profile statistics for profile by given name.
     */
    private AlertProfileStats getAlertProfileStats(
            final PerformanceReportBean report, final String name) {
        for (final AlertProfileStats s : report.getAlertProfiles()) {
            if (name.equals(s.getName())) {
                return s;
            }
        }
        return null;
    }
}
