/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.Before;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
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
    /**
     * Device.
     */
    private Device device;

    /**
     * Default constructor.
     */
    public TrackerEventDaoTest() {
        super(TrackerEventDao.class);
    }

    @Before
    public void beforeTest() {
        deviceDao = getContext().getBean(DeviceDao.class);

        final Device d = new Device();
        d.setCompany(sharedCompany);
        final String imei = "932487032487";
        d.setImei(imei);
        d.setName("Test Device");
        d.setId(imei + ".1234");
        d.setDescription("JUnit device");
        d.setSn("12345");

        this.device = deviceDao.save(d);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected TrackerEvent createTestEntity() {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(27);
        e.setDevice(device);
        e.setTemperature(5.5);
        e.setTime(new Date());
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
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        super.clear();
        deviceDao.deleteAll();
    }
}
