/**
 *
 */
package com.visfresh.dao;

import java.util.Date;

import org.junit.Before;

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
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        super.clear();
        deviceDao.deleteAll();
    }
}
