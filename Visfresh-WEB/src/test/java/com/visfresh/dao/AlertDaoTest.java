/**
 *
 */
package com.visfresh.dao;

import java.util.Date;

import org.junit.Before;

import com.visfresh.entities.AbstractAlert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.TemperatureAlert;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertDaoTest extends BaseCrudTest<AlertDao, AbstractAlert, Long> {
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
    public AlertDaoTest() {
        super(AlertDao.class);
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
        d.setName("Test device");

        this.device = deviceDao.save(d);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected AbstractAlert createTestEntity() {
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(new Date(System.currentTimeMillis() - 100000000l));
        alert.setDescription("Alert description");
        alert.setName("Any name");
        alert.setDevice(device);
        alert.setType(AlertType.CriticalHighTemperature);
        alert.setTemperature(100);
        alert.setMinutes(15);

        return alert;
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
