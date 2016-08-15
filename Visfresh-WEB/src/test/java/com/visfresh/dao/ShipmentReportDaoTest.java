/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentReportDaoTest extends BaseDaoTest<ShipmentReportDao> {
    /**
     *
     */
    private static final long ONE_DAY = 24 * 60 * 60 * 1000l;
    private ShipmentDao shipmentDao;
    private Shipment shipment;
    private User user;

    /**
     * Default constructor.
     */
    public ShipmentReportDaoTest() {
        super(ShipmentReportDao.class);
    }

    @Before
    public void setUp() {
        shipmentDao = context.getBean(ShipmentDao.class);

        Device d = new Device();
        d.setImei("9238470983274987");
        d.setName("Test Device");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        d.setTripCount(5);
        d = context.getBean(DeviceDao.class).save(d);

        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(ShipmentStatus.Arrived);
        this.shipment = getContext().getBean(ShipmentDao.class).save(s);

        user = new User();
        user.setActive(true);
        user.setCompany(sharedCompany);
        user.setEmail("chapaev@mail.ru");
        user.setFirstName("Vasily");
        user.setLastName("Chapaev");
        user.setTitle("VCh");
        user.setTemperatureUnits(TemperatureUnits.Fahrenheit);
        user.setTimeZone(TimeZone.getTimeZone("UTC"));

        context.getBean(UserDao.class).save(user);
    }
    @Test
    public void testGoods() {
        final int tripCount = 12;
        final String shipmentDescription = "2398327 987opwrei u3lk4jh";
        final String palletId = "0239847098";
        final String comments = "Shipment comments for receiver";

        shipment.setTripCount(tripCount);
        shipment.setShipmentDescription(shipmentDescription);
        shipment.setPalletId(palletId);
        shipment.setCommentsForReceiver(comments);

        shipmentDao.save(shipment);

        final ShipmentReportBean report = dao.createReport(shipment, user);

        assertEquals(shipment.getDevice().getImei(), report.getDevice());
        assertEquals(tripCount, report.getTripCount());
        assertEquals(shipmentDescription, report.getDescription());
        assertEquals(palletId, report.getPalletId());
    }
    @Test
    public void testShipment() {
        final String locationFromName = "Deribasovskaya";
        final String locationToName = "Myasoedovskaya";
        final Date shipmentDate = new Date(System.currentTimeMillis() - 15 * ONE_DAY);
        final Date arrivalDate = new Date(System.currentTimeMillis() - 10 * ONE_DAY);

        final LocationProfile locTo = new LocationProfile();
        locTo.setAddress("Odessa city, Myasoedovskaya 1, apt.1");
        locTo.setName(locationToName);
        locTo.setCompany(sharedCompany);
        locTo.setRadius(500);
        locTo.getLocation().setLatitude(10.);
        locTo.getLocation().setLongitude(11.);

        final LocationProfile locFrom = new LocationProfile();
        locFrom.setAddress("Odessa city, Deribasovskaya 1, apt.1");
        locFrom.setName(locationFromName);
        locFrom.setCompany(sharedCompany);
        locFrom.setRadius(500);
        locFrom.getLocation().setLatitude(10.);
        locFrom.getLocation().setLongitude(11.);

        shipment.setShippedFrom(locFrom);
        shipment.setShippedTo(locTo);
        shipment.setShipmentDate(shipmentDate);
        shipment.setArrivalDate(arrivalDate);

        shipmentDao.save(shipment);

        final ShipmentReportBean report = dao.createReport(shipment, user);

        final DateFormat fmt = DateTimeUtils.createPrettyFormat(
                user.getLanguage(), user.getTimeZone());

        assertEquals(locationFromName, report.getShippedFrom());
        assertEquals(locationToName, report.getShippedTo());
        assertEquals(fmt.format(shipmentDate), fmt.format(report.getDateShipped()));
        assertEquals(fmt.format(arrivalDate), fmt.format(report.getDateArrived()));
    }
    @Test
    public void testTemperature() {
        final AlertProfile ap = new AlertProfile();

        final double upperTemperatureLimit = 15.3;
        final double lowerTemperatureLimit = -11.2;

        ap.setUpperTemperatureLimit(upperTemperatureLimit);
        ap.setLowerTemperatureLimit(lowerTemperatureLimit);
        context.getBean(AlertProfileDao.class).save(ap);

        shipment.setAlertProfile(ap);
        shipmentDao.save(shipment);

        final long dt = 10 * 60 * 1000l;
        final long startTime = System.currentTimeMillis() - dt * 1000;

        createTrackerEvent(startTime + 1 * dt, 20.7);
        createTrackerEvent(startTime + 2 * dt, 15.7);
        createTrackerEvent(startTime + 3 * dt, 10);
        createTrackerEvent(startTime + 4 * dt, 8);
        createTrackerEvent(startTime + 5 * dt, 6);
        createTrackerEvent(startTime + 6 * dt, 0);
        createTrackerEvent(startTime + 6 * dt, -11.5);
        createTrackerEvent(startTime + 7 * dt, -20.3);

        final ShipmentReportBean report = dao.createReport(shipment, user);

        final List<TrackerEvent> events = context.getBean(TrackerEventDao.class).findAll(
                null, null, null);
        double summ = 0;
        for (final TrackerEvent e : events) {
            summ += e.getTemperature();
        }

        assertEquals(summ / events.size(), report.getAvgTemperature(), 0.0001);
        assertTrue(report.getStandardDevitation() < 1.);
        assertEquals(-20.3, report.getMinimumTemperature(), 0.0001);
        assertEquals(20.7, report.getMaximumTemperature(), 0.0001);
        assertEquals(2 * dt, report.getTimeAboveUpperLimit());
        assertEquals(2 * dt, report.getTimeBelowLowerLimit());
        assertEquals(8 * dt, report.getTotalTime());
    }

    @Test
    public void testAlertFired() {

    }

    /**
     * @param time reading time.
     * @param temperature temperature.
     */
    private TrackerEvent createTrackerEvent(final long time, final double temperature) {
        final TrackerEvent e = new TrackerEvent();
        e.setTime(new Date(time));
        e.setCreatedOn(new Date());
        e.setTemperature(temperature);
        e.setShipment(shipment);
        e.setType(TrackerEventType.AUT);
        e.setDevice(shipment.getDevice());

        return context.getBean(TrackerEventDao.class).save(e);
    }
}
