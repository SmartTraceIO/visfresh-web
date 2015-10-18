/**
 *
 */
package com.visfresh.dao;

import org.junit.Before;

import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommandDaoTest extends BaseCrudTest<DeviceCommandDao, DeviceCommand, Long> {
    private DeviceDao deviceDao;
    private Device device;

    /**
     * Default constructor.
     */
    public DeviceCommandDaoTest() {
        super(DeviceCommandDao.class);
    }

    @Before
    public void beforeTest() {
        deviceDao = getContext().getBean(DeviceDao.class);

        final Device d = new Device();
        d.setCompany(sharedCompany);
        final String imei = "932487032487";
        d.setName("Test Device");
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
    protected DeviceCommand createTestEntity() {
        final DeviceCommand cmd = new DeviceCommand();
        cmd.setCommand("start");
        cmd.setDevice(device);
        return cmd;
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
