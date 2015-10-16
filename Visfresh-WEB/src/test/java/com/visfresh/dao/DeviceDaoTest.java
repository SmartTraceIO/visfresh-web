/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDaoTest extends BaseCrudTest<DeviceDao, Device, String> {
    /**
     * Default constructor.
     */
    public DeviceDaoTest() {
        super(DeviceDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Device createTestEntity() {
        final Device d = new Device();
        d.setImei("3984709382475");
        d.setId(d.getImei() + ".1234");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        return d;
    }
}
