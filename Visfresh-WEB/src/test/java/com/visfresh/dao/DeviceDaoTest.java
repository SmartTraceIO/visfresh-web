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
    private int id;
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
        d.setImei(Long.toString(3984709382475L + (++id)));
        d.setId(d.getImei() + ".1234");
        d.setName("Test Device");
        d.setSn("124");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        return d;
    }
}
