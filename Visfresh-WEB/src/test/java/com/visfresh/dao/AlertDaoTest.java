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
        d.setId(imei + ".1234");
        d.setDescription("JUnit device");
        d.setSn("12345");
        d.setName("Test device");

        this.device = deviceDao.save(d);

        final Shipment s = new Shipment();
        s.setName("Default profile");
        s.setCompany(sharedCompany);
        s.getDevices().add(d);
        shipment = shipmentDao.save(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Alert createTestEntity() {
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(new Date(System.currentTimeMillis() - 100000000l));
        alert.setDescription("Alert description");
        alert.setName("Any name");
        alert.setDevice(device);
        alert.setShipment(shipment);
        alert.setType(AlertType.CriticalHighTemperature);
        alert.setTemperature(100);
        alert.setMinutes(15);

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
        assertEquals("Alert description", a.getDescription());
        assertEquals("Any name", a.getName());
        assertEquals(AlertType.CriticalHighTemperature, a.getType());
        assertEquals(100, a.getTemperature(), 0.00001);
        assertEquals(15, a.getMinutes());

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
        assertEquals("Alert description", a.getDescription());
        assertEquals("Any name", a.getName());
        assertEquals(AlertType.CriticalHighTemperature, a.getType());
        assertEquals(100, a.getTemperature(), 0.00001);
        assertEquals(15, a.getMinutes());

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
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        super.clear();
        shipmentDao.deleteAll();
        deviceDao.deleteAll();
    }
}
