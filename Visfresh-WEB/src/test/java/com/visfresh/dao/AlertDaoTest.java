/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertDaoTest extends BaseCrudTest<AlertDao, Alert, Long> {
    /**
     * Device DAO.
     */
    private DeviceDao deviceDao;
    /**
     * Device.
     */
    private Device device;
    private ShipmentDao shipmentDao;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public AlertDaoTest() {
        super(AlertDao.class);
    }

    @Before
    public void beforeTest() {
        deviceDao = getContext().getBean(DeviceDao.class);
        shipmentDao = getContext().getBean(ShipmentDao.class);

        final Device d = new Device();
        d.setCompany(sharedCompany);
        final String imei = "932487032487";
        d.setImei(imei);
        d.setDescription("JUnit device");
        d.setName("Test device");

        this.device = deviceDao.save(d);
        shipment = createShipment(d);
    }
    /**
     * @param d
     * @return
     */
    private Shipment createShipment(final Device d) {
        Shipment s = new Shipment();
        s.setCompany(sharedCompany);
        s.setDevice(d);
        s = shipmentDao.save(s);
        return s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Alert createTestEntity() {
        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final AlertType type = AlertType.CriticalHot;
        final Alert a = createAlert(type, date);
        a.setTrackerEventId(77777l);
        return a;
    }

    /**
     * @param type alert type.
     * @param date date.
     * @return
     */
    protected Alert createAlert(final AlertType type, final Date date) {
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(date);
        alert.setDevice(device);
        alert.setShipment(shipment);
        alert.setType(type);
        alert.setTemperature(100);
        alert.setCumulative(true);
        alert.setMinutes(15);
        alert.setRuleId(77l);
        return alert;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Alert alert) {
        assertTrue(alert instanceof TemperatureAlert);
        final TemperatureAlert a = (TemperatureAlert) alert;

        assertNotNull(a.getDate());
        assertEquals(AlertType.CriticalHot, a.getType());
        assertEquals(100, a.getTemperature(), 0.00001);
        assertEquals(15, a.getMinutes());
        assertTrue(a.isCumulative());
        assertEquals(77777l, alert.getTrackerEventId().longValue());
        assertEquals(77l, a.getRuleId().longValue());

        final Device d = a.getDevice();
        assertNotNull(d);

        assertEquals(device.getDescription(), d.getDescription());
        assertEquals(device.getId(), d.getId());
        assertEquals(device.getImei(), d.getImei());
        assertEquals(device.getName(), d.getName());
        assertEquals(device.getSn(), d.getSn());

        assertNotNull(alert.getShipment());

        final Company c = d.getCompany();
        assertNotNull(c);

        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<Alert> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final TemperatureAlert a = (TemperatureAlert) all.get(0);

        assertNotNull(a.getDate());
        assertEquals(AlertType.CriticalHot, a.getType());
        assertEquals(100, a.getTemperature(), 0.00001);
        assertEquals(15, a.getMinutes());
        assertEquals(77777l, a.getTrackerEventId().longValue());
        assertEquals(77l, a.getRuleId().longValue());

        final Device d = a.getDevice();
        assertNotNull(d);

        assertEquals(device.getDescription(), d.getDescription());
        assertEquals(device.getId(), d.getId());
        assertEquals(device.getImei(), d.getImei());
        assertEquals(device.getName(), d.getName());
        assertEquals(device.getSn(), d.getSn());

        assertNotNull(a.getShipment());

        final Company c = d.getCompany();
        assertNotNull(c);

        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }
    @Test
    public void testGetAlertsByShipmentAndTimeRanges() {
        final Date startDate = new Date(System.currentTimeMillis() - 1000000000l);
        final Date endDate = new Date(System.currentTimeMillis() - 1000000l);

        createAndSave(new Date(startDate.getTime() - 1000l));
        createAndSave(new Date(startDate.getTime()));
        createAndSave(new Date(startDate.getTime() + 10000l));
        createAndSave(new Date(endDate.getTime()));
        createAndSave(new Date(endDate.getTime() + 1000l));

        assertEquals(5, dao.getAlerts(shipment).size());

        //check left shipment
        final Shipment left = createShipment(device);
        assertEquals(0, dao.getAlerts(left).size());
    }
    @Test
    public void testMoveToNewDevice() {
        final Device d1 = createDevice("390248703928740");
        final Device d2 = createDevice("293087098709870");

        final Alert a = createAlert(AlertType.Cold, new Date());
        a.setDevice(d1);
        dao.save(a);

        dao.moveToNewDevice(d1, d2);
        assertEquals(d2.getImei(), dao.findOne(a.getId()).getDevice().getImei());
    }
    /**
     * @param imei
     * @return
     */
    private Device createDevice(final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setActive(true);
        d.setName("JUnit-" + imei);
        d.setCompany(sharedCompany);
        return context.getBean(DeviceDao.class).save(d);
    }

    /**
     * @param date
     */
    private Alert createAndSave(final Date date) {
        final Alert a = createAlert(AlertType.Hot, date);
        return dao.save(a);
    }
}
