/**
 *
 */
package com.visfresh.impl.singleshipment;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.BaseDbTest;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Color;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class BaseBuilderTest extends BaseDbTest {
    protected NamedParameterJdbcTemplate jdbc;
    protected Device device;
    protected ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public BaseBuilderTest() {
        super();
    }

    @Before
    public void setUp() {
        jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        shipmentDao = context.getBean(ShipmentDao.class);
        device = createDevice("234897029345798");
    }

    /**
     * @param device device.
     * @return shipment.
     */
    protected Shipment createDefaultNotSavedShipment(final Device device) {
        final Shipment s = new Shipment();
        s.setDevice(device);
        s.setCompany(sharedCompany.getCompanyId());
        s.setStatus(ShipmentStatus.InProgress);
        s.setShipmentDescription("JUnit shipment");
        return s;
    }
    /**
     * @param device device IMEI.
     * @return
     */
    protected Device createDevice(final String device) {
        final Device d = new Device();
        d.setImei(device);
        d.setName("JUnit-" + device);
        d.setCompany(sharedCompany.getCompanyId());
        d.setDescription("JUnit device");
        d.setColor(Color.Brown);
        return context.getBean(DeviceDao.class).save(d);
    }
    /**
     * @param s shipment.
     * @return tracker event.
     */
    protected TrackerEvent createTrackerEvent(final Shipment s) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(700);
        e.setCreatedOn(new Date());
        e.setDevice(s.getDevice());
        e.setLatitude(30.);
        e.setLongitude(20.);
        e.setShipment(s);
        e.setTemperature(12.);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    /**
     * @param s
     * @return
     */
    protected AlertProfile createAlertProfile(final Shipment s) {
        final AlertProfile ap = new AlertProfile();
        ap.setCompany(sharedCompany.getCompanyId());
        ap.setName("JUnit");
        s.setAlertProfile(ap);
        context.getBean(AlertProfileDao.class).save(ap);
        shipmentDao.save(s);
        return ap;
    }
    /**
     * @param type
     * @param t
     * @return
     */
    protected TemperatureRule createTemperatureRule(final AlertProfile ap, final AlertType type, final double t) {
        final TemperatureRule rule = new TemperatureRule(type);
        rule.setCumulativeFlag(true);
        rule.setMaxRateMinutes(100);
        rule.setTemperature(t);
        rule.setTimeOutMinutes(200);

        ap.getAlertRules().add(rule);
        context.getBean(AlertProfileDao.class).save(ap);
        return rule;
    }
    /**
     * @param e tracker event.
     * @return alert.
     */
    protected Alert createAlert(final TrackerEvent e) {
        final Alert a = new Alert(AlertType.Battery);
        a.setDate(e.getTime());
        a.setDevice(e.getDevice());
        a.setShipment(e.getShipment());
        a.setTrackerEventId(e.getId());
        return context.getBean(AlertDao.class).save(a);
    }
    /**
     * @param siblings
     */
    protected void setAsSiblings(final Shipment... siblings) {
        //create ID set
        final Set<Long> allIds = new HashSet<>();
        for (final Shipment shipment : siblings) {
            allIds.add(shipment.getId());
        }

        //set new sibling list to siblings
        for (final Shipment shipment : siblings) {
            final Set<Long> ids = new HashSet<>(allIds);
            ids.remove(shipment.getId());

            shipment.getSiblings().clear();
            shipment.getSiblings().addAll(ids);
            shipment.setSiblingCount(shipment.getSiblings().size());
            shipmentDao.save(shipment);
        }
    }
    /**
     * @param d device.
     * @return shipment.
     */
    protected Shipment createShipment(final Device d) {
        return shipmentDao.save(createDefaultNotSavedShipment(d));
    }
    /**
     * @param d1 first date.
     * @param d2 second date.
     */
    protected void assertEqualsDates(final Date d1, final Date d2) {
        assertTrue(Math.abs(d1.getTime() - d2.getTime()) < 1000l);
    }
}
