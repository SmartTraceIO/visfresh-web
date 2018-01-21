/**
 *
 */
package com.visfresh.dao.impl;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.reports.TemperatureStats;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureStatsCollectorTest {
    private AlertProfileTemperatureStatsCollector collector;
    private Device device;
    private AlertProfile alertProfile;
    private long lastId = 1l;

    /**
     * Default constructor.
     */
    public TemperatureStatsCollectorTest() {
        super();
    }

    @Before
    public void setUp() {
        final Company company = new Company();
        company.setId(7l);

        final Device d = new Device();
        d.setImei("239847098983748");
        d.setActive(true);
        d.setName("JUnit-" + d.getImei());
        d.setCompany(company);
        this.device = d;

        final AlertProfile ap = createAlertProfile("JUnit");
        this.alertProfile = ap;

        collector = new AlertProfileTemperatureStatsCollector();
    }
    @Test
    public void testTemperatureStats() {
        final Shipment s1 = createShipment();
        final Shipment s2 = createShipment();

        final long dt = 10 * 60 * 1000l;
        final long startTime = getMiddleOfMonth("2016.08").getTime() - dt * 1000;

        final List<TrackerEvent> events = new LinkedList<>();

        events.add(createEvent(s1, new Date(startTime + 1 * dt), 20.7));
        events.add(createEvent(s2, new Date(startTime + 2 * dt), 15.7));
        events.add(createEvent(s1, new Date(startTime + 3 * dt), 10));
        events.add(createEvent(s2, new Date(startTime + 4 * dt), 8));
        events.add(createEvent(s1, new Date(startTime + 5 * dt), 6));
        events.add(createEvent(s2, new Date(startTime + 6 * dt), 0));
        events.add(createEvent(s1, new Date(startTime + 7 * dt), -11.5));
        events.add(createEvent(s2, new Date(startTime + 8 * dt), -20.3));
        events.add(createEvent(s1, new Date(startTime + 9 * dt), 0 * dt));
        events.add(createEvent(s2, new Date(startTime + 10 * dt), 0 * dt));

        for (final TrackerEvent e : events) {
            final AlertProfile ap = e.getShipment().getAlertProfile();
            collector.processEvent(e, ap.getLowerTemperatureLimit(), ap.getUpperTemperatureLimit());
        }

        double summ = 0;
        for (final TrackerEvent e : events) {
            summ += e.getTemperature();
        }

        final TemperatureStats stats = collector.getStatistics();
        assertEquals(summ / events.size(), stats.getAvgTemperature(), 0.0001);
        assertEquals(-20.3, stats.getMinimumTemperature(), 0.0001);
        assertEquals(20.7, stats.getMaximumTemperature(), 0.0001);
        assertEquals(4 * dt, stats.getTimeAboveUpperLimit());
        assertEquals(4 * dt, stats.getTimeBelowLowerLimit());
        assertEquals((8 + 8) * dt, stats.getTotalTime());
    }
    @Test
    public void testIgnoreTimeAfterArrival() {
        final Shipment s1 = createShipment();

        final long dt = 10 * 60 * 1000l;
        final long startTime = getMiddleOfMonth("2016.08").getTime() - dt * 1000;
        s1.setArrivalDate(new Date(startTime + 2 * dt));

        final List<TrackerEvent> events = new LinkedList<>();
        events.add(createEvent(s1, new Date(startTime + 1 * dt), 20.7));
        events.add(createEvent(s1, new Date(startTime + 3 * dt), 15.7));

        for (final TrackerEvent e : events) {
            final AlertProfile ap = e.getShipment().getAlertProfile();
            collector.processEvent(e, ap.getLowerTemperatureLimit(), ap.getUpperTemperatureLimit());
        }

        final TemperatureStats stats = collector.getStatistics();
        assertEquals(20.7, stats.getAvgTemperature(), 0.0001);
    }
    @Test
    public void testAvg() {
        final Shipment s1 = createShipment();

        final long dt = 10 * 60 * 1000l;
        final long startTime = getMiddleOfMonth("2016.08").getTime() - dt * 1000;

        final List<TrackerEvent> events = new LinkedList<>();
        events.add(createEvent(s1, new Date(startTime + 1 * dt), 20.7));
        events.add(createEvent(s1, new Date(startTime + 2 * dt), 15.7));
        events.add(createEvent(s1, new Date(startTime + 3 * dt), 18.7));
        events.add(createEvent(s1, new Date(startTime + 4 * dt), 19.7));

        double summ = 0;
        for (final TrackerEvent e : events) {
            final AlertProfile ap = e.getShipment().getAlertProfile();
            collector.processEvent(e, ap.getLowerTemperatureLimit(), ap.getUpperTemperatureLimit());
            summ += e.getTemperature();
        }

        final TemperatureStats stats = collector.getStatistics();
        assertEquals(summ / events.size(), stats.getAvgTemperature(), 0.0001);
    }
    @Test
    public void testSd() {
        final Shipment s1 = createShipment();

        final long dt = 10 * 60 * 1000l;
        final long startTime = getMiddleOfMonth("2016.08").getTime() - dt * 1000;

        final List<TrackerEvent> events = new LinkedList<>();
        events.add(createEvent(s1, new Date(startTime + 1 * dt), 20.7));
        events.add(createEvent(s1, new Date(startTime + 2 * dt), 15.7));
        events.add(createEvent(s1, new Date(startTime + 3 * dt), 18.7));
        events.add(createEvent(s1, new Date(startTime + 4 * dt), 19.7));

        double summ = 0;
        for (final TrackerEvent e : events) {
            final AlertProfile ap = e.getShipment().getAlertProfile();
            collector.processEvent(e, ap.getLowerTemperatureLimit(), ap.getUpperTemperatureLimit());
            summ += e.getTemperature();
        }
        final double avg = summ / events.size();

        final TemperatureStats stats = collector.getStatistics();

        //calculate SD on standard manner
        double sd = 0;
        for (final TrackerEvent e : events) {
            final double dx = e.getTemperature() - avg;
            sd += dx * dx;
        }
        sd = Math.sqrt(sd / (events.size() - 1));

        assertEquals(sd, stats.getStandardDevitation(), 0.0001);
    }
    /**
     * @param s shipment.
     * @param time event time.
     * @param t temperature.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Shipment s, final Date time, final double t) {
        final TrackerEvent e = new TrackerEvent();
        e.setId(lastId++);
        e.setShipment(s);
        e.setDevice(s.getDevice());
        e.setCreatedOn(time);
        e.setTime(time);
        e.setTemperature(t);
        e.setType(TrackerEventType.AUT);
        return e;
    }
    /**
     * @return
     */
    private Shipment createShipment() {
        return createShipment(ShipmentStatus.Arrived);
    }

    /**
     * @param status
     * @return
     */
    protected Shipment createShipment(final ShipmentStatus status) {
        final Shipment s = new Shipment();
        s.setId(lastId++);
        s.setAlertProfile(alertProfile);
        s.setCompany(device.getCompany());
        s.setDevice(device);
        s.setStatus(status);
        return s;
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
     * @param name alert profile name.
     * @return alert profile.
     */
    private AlertProfile createAlertProfile(final String name) {
        final AlertProfile ap = new AlertProfile();
        ap.setId(lastId++);
        ap.setCompany(device.getCompany());
        ap.setDescription("JUnit test alert pforile");
        ap.setName(name);
        ap.setUpperTemperatureLimit(15.3);
        ap.setLowerTemperatureLimit(-11.2);

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

        return ap;
    }
}
