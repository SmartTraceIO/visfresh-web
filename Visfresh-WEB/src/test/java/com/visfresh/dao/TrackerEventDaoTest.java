/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEventDaoTest extends BaseCrudTest<TrackerEventDao, TrackerEvent, Long> {
    /**
     * Device DAO.
     */
    private DeviceDao deviceDao;
    private ShipmentDao shipmentDao;
    /**
     * Device.
     */
    private Device device;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public TrackerEventDaoTest() {
        super(TrackerEventDao.class);
    }

    @Before
    public void beforeTest() {
        deviceDao = getContext().getBean(DeviceDao.class);
        shipmentDao = getContext().getBean(ShipmentDao.class);

        final Device d = new Device();
        d.setCompany(sharedCompany);
        final String imei = "932487032487";
        d.setImei(imei);
        d.setName("Test Device");
        d.setDescription("JUnit device");
        d.setSn("12345");

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
    protected TrackerEvent createTestEntity() {
        final Date time = new Date();
        return createEvent(time, 5.5);
    }

    /**
     * @param time
     * @param temperature temperature
     * @return
     */
    protected TrackerEvent createEvent(final Date time, final double temperature) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(27);
        e.setDevice(device);
        e.setShipment(shipment);
        e.setTemperature(temperature);
        e.setTime(time);
        e.setType(TrackerEventType.INIT);
        return e;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final TrackerEvent e) {
        assertNotNull(e.getTime());
        assertEquals(27, e.getBattery());
        assertEquals(5.5, e.getTemperature(), 0.00001);
        assertEquals(TrackerEventType.INIT, e.getType());

        final Device d = e.getDevice();
        assertNotNull(d);

        assertEquals(device.getDescription(), d.getDescription());
        assertEquals(device.getId(), d.getId());
        assertEquals(device.getImei(), d.getImei());
        assertEquals(device.getName(), d.getName());
        assertEquals(device.getSn(), d.getSn());

        final Company c = d.getCompany();
        assertNotNull(c);

        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());

        assertNotNull(e.getShipment());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<TrackerEvent> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final TrackerEvent e = all.get(0);

        assertNotNull(e.getTime());
        assertEquals(27, e.getBattery());
        assertEquals(5.5, e.getTemperature(), 0.00001);
        assertEquals(TrackerEventType.INIT, e.getType());

        final Device d = e.getDevice();
        assertNotNull(d);

        assertEquals(device.getDescription(), d.getDescription());
        assertEquals(device.getId(), d.getId());
        assertEquals(device.getImei(), d.getImei());
        assertEquals(device.getName(), d.getName());
        assertEquals(device.getSn(), d.getSn());

        final Company c = d.getCompany();
        assertNotNull(c);

        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());

        assertNotNull(e.getShipment());
    }
    @Test
    public void testGetAlertsByShipmentAndTimeRanges() {
        final Date startDate = new Date(System.currentTimeMillis() - 1000000000l);
        final Date endDate = new Date(System.currentTimeMillis() - 1000000l);

        createAndSave(new Date(startDate.getTime() - 1000l), 5.5);
        createAndSave(new Date(startDate.getTime()), 5.5);
        createAndSave(new Date(startDate.getTime() + 10000l), 5.5);
        createAndSave(new Date(endDate.getTime()), 5.5);
        createAndSave(new Date(endDate.getTime() + 1000l), 5.5);

        assertEquals(3, dao.getEvents(shipment, startDate, endDate).size());

        //check left shipment
        final Shipment left = createShipment(device);
        assertEquals(0, dao.getEvents(left, startDate, endDate).size());
    }
    @Test
    public void testGetPreviousEvent() {
        long time = System.currentTimeMillis() - 100000000l;

        final TrackerEvent first = createAndSave(new Date((time+=10000)), 12);
        createAndSave(new Date((time+=10000)), 11);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 9);
        createAndSave(new Date((time+=10000)), 8);
        final TrackerEvent prev = createAndSave(new Date((time+=10000)), 9);
        final TrackerEvent curr = createAndSave(new Date((time+=10000)), 11);
        createAndSave(new Date((time+=10000)), 12);
        createAndSave(new Date((time+=10000)), 13);
        createAndSave(new Date((time+=10000)), 12);

        final TrackerEvent e = dao.getPreviousEvent(curr);
        assertEquals(prev.getId(), e.getId());
        assertNull(dao.getPreviousEvent(first));
    }
    /**
     * @param date
     * @param temperature temperature
     */
    private TrackerEvent createAndSave(final Date date, final double temperature) {
        return dao.save(createEvent(date, temperature));
    }
}
