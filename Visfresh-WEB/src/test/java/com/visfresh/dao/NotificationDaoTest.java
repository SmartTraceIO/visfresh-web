/**
 *
 */
package com.visfresh.dao;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationDaoTest extends BaseCrudTest<NotificationDao, Notification, Long> {
    private DeviceDao deviceDao;
    private Device device;
    private ArrivalDao arrivalDao;
    private Arrival arrival;

    /**
     * Default constructor.
     */
    public NotificationDaoTest() {
        super(NotificationDao.class);
    }

    @Before
    public void beforeTest() {
        deviceDao = getContext().getBean(DeviceDao.class);

        //create device
        final Device d = new Device();
        d.setCompany(sharedCompany);
        final String imei = "932487032487";
        d.setImei(imei);
        d.setId(imei + ".1234");
        d.setDescription("JUnit device");
        d.setSn("12345");

        this.device = deviceDao.save(d);

        //create arrival
        arrivalDao = getContext().getBean(ArrivalDao.class);

        final Arrival a = new Arrival();
        a.setDate(new Date(System.currentTimeMillis() - 1000000l));
        a.setDevice(device);
        a.setNumberOfMettersOfArrival(78);
        this.arrival = arrivalDao.save(a);
    }

    @Test
    public void testFindForUser() {

    }
    @Test
    public void testDeleteByUserAndId() {

    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Notification createTestEntity() {
        final Notification n = new Notification();
        n.setType(NotificationType.Arrival);
        n.setIssue(arrival);
        return n;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        super.clear();
        arrivalDao.deleteAll();
        deviceDao.deleteAll();
    }
}
