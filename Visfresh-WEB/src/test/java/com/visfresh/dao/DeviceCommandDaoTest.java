/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;

import com.visfresh.entities.Company;
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
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final DeviceCommand cmd) {
        assertEquals("start", cmd.getCommand());

        final Device d = cmd.getDevice();
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
            final List<DeviceCommand> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final DeviceCommand a = all.get(0);

        assertEquals("start", a.getCommand());

        final Device d = a.getDevice();
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
}
