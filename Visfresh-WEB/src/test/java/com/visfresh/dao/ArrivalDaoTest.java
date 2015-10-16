/**
 *
 */
package com.visfresh.dao;

import java.util.Date;

import org.junit.Before;

import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalDaoTest extends BaseCrudTest<ArrivalDao, Arrival, Long> {
    private DeviceDao deviceDao;
    private Device device;
    /**
     * Default constructor.
     */
    public ArrivalDaoTest() {
        super(ArrivalDao.class);
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
    protected Arrival createTestEntity() {
        final Arrival a = new Arrival();
        a.setDate(new Date(System.currentTimeMillis() - 1000000l));
        a.setDevice(device);
        a.setNumberOfMettersOfArrival(78);
        return a;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        super.clear();
        deviceDao.deleteAll();;
    }
}
