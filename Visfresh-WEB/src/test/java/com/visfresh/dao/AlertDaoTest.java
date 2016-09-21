/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    @Test
    public void testGetAlertsForShipmentIds() {
        final Device d1 = createDevice("02398470238472");
        final Shipment s1 = createShipment(d1);
        final Shipment s2 = createShipment(d1);
        final Shipment s3 = createShipment(createDevice("390248032847093"));

        createEvent(s1);
        createEvent(s1);

        createEvent(s2);
        createEvent(s2);

        createEvent(s3);
        createEvent(s3);

        final List<Long> ids = new LinkedList<>();
        ids.add(s1.getId());
        ids.add(s2.getId());

        final Map<Long, List<Alert>> map = dao.getAlertsForShipmentIds(ids);
        assertEquals(2, map.size());
        assertEquals(2, map.get(s1.getId()).size());
        assertEquals(2, map.get(s2.getId()).size());
    }
    @Test
    public void testGetAlertsForDeviceDateRanges() {
        final Device d1 = createDevice("02398470238472");
        final Shipment s1 = createShipment(d1);
        final Shipment s2 = createShipment(d1);
        final Shipment s3 = createShipment(createDevice("390248032847093"));

        final long dt = 100000l;
        final long startDate = System.currentTimeMillis() - 100 * dt;

        createEvent(s1, new Date(startDate + 5 * dt));
        createEvent(s1, new Date(startDate + 10 * dt));

        createEvent(s2, new Date(startDate + 15 * dt));
        createEvent(s2, new Date(startDate + 20 * dt));

        createEvent(s3, new Date(startDate + 25 * dt));
        createEvent(s3, new Date(startDate + 30 * dt));

        assertEquals(4, dao.getAlerts(s1.getDevice().getImei(), null, null).size());
        assertEquals(4, dao.getAlerts(s1.getDevice().getImei(),
                new Date(startDate + 5 * dt), new Date(startDate + 20 * dt)).size());

        assertEquals(3, dao.getAlerts(s1.getDevice().getImei(),
                new Date(startDate + 10 * dt), new Date(startDate + 20 * dt)).size());
        assertEquals(2, dao.getAlerts(s1.getDevice().getImei(),
                new Date(startDate + 10 * dt), new Date(startDate + 15 * dt)).size());
    }
    @Test
    public void testGetAlertsForCompanyDateRanges() {
        final Company c1 = createCompany("C1");
        final Company c2 = createCompany("C2");

        final Device d1 = createDevice(c1, "32908470987908");
        final Device d2 = createDevice(c2, "02398470238472");
        final Shipment s1 = createShipment(d1);
        final Shipment s2 = createShipment(d1);
        final Shipment s3 = createShipment(d2);

        final long dt = 100000l;
        final long startDate = System.currentTimeMillis() - 100 * dt;

        createEvent(s1, new Date(startDate + 5 * dt));
        createEvent(s1, new Date(startDate + 10 * dt));

        createEvent(s2, new Date(startDate + 15 * dt));
        createEvent(s2, new Date(startDate + 20 * dt));

        createEvent(s3, new Date(startDate + 25 * dt));
        createEvent(s3, new Date(startDate + 30 * dt));

        assertEquals(4, dao.getAlerts(c1, null, null).size());
        assertEquals(4, dao.getAlerts(c1,
                new Date(startDate + 5 * dt), new Date(startDate + 20 * dt)).size());

        assertEquals(3, dao.getAlerts(c1,
                new Date(startDate + 10 * dt), new Date(startDate + 20 * dt)).size());
        assertEquals(2, dao.getAlerts(c1,
                new Date(startDate + 10 * dt), new Date(startDate + 15 * dt)).size());
    }
    /**
     * @param s
     * @return alert.
     */
    private Alert createEvent(final Shipment s) {
        final Alert a = createTestEntity();
        a.setDevice(s.getDevice());
        a.setShipment(s);
        return dao.save(a);
    }
    /**
     * @param s
     * @return alert.
     */
    private Alert createEvent(final Shipment s, final Date date) {
        final Alert a = createTestEntity();
        a.setDevice(s.getDevice());
        a.setShipment(s);
        a.setDate(date);
        return dao.save(a);
    }
    /**
     * @param imei device IMEI.
     * @return device.
     */
    private Device createDevice(final String imei) {
        return createDevice(sharedCompany, imei);
    }
    /**
     * @param company
     * @param imei
     * @return
     */
    protected Device createDevice(final Company company, final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setActive(true);
        d.setName("JUnit-" + imei);
        d.setCompany(company);
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
