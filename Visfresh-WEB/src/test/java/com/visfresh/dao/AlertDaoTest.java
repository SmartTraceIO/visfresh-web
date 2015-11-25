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
        d.setSn("12345");
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
        return createAlert(type, date);
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

        assertEquals(3, dao.getAlerts(shipment, startDate, endDate).size());

        //check left shipment
        final Shipment left = createShipment(device);
        assertEquals(0, dao.getAlerts(left, startDate, endDate).size());
    }

    /**
     * @param date
     */
    private Alert createAndSave(final Date date) {
        final Alert a = createAlert(AlertType.Hot, date);
        return dao.save(a);
    }
}
