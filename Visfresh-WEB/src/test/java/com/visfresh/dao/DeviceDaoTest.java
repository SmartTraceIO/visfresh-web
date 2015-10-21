/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.visfresh.entities.Company;
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
        return createDevice("3984709382475");
    }

    /**
     * @param imei
     * @return
     */
    protected Device createDevice(final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setId(d.getImei() + "." + (++id));
        d.setName("Test Device");
        d.setSn("124");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        return d;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCorrectSaved(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Device d) {
        assertEquals("3984709382475", d.getImei());
        assertEquals("Test Device", d.getName());
        assertEquals("124", d.getSn());
        assertEquals("Test device", d.getDescription());

        //test company
        final Company c = d.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<Device> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        //check first entity
        final Device d = all.get(0);

        assertEquals("3984709382475", d.getImei());
        assertEquals("Test Device", d.getName());
        assertEquals("124", d.getSn());
        assertEquals("Test device", d.getDescription());

        //test company
        final Company c = d.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }

    @Test
    public void testAllGetByImei() {
        final String imei = "3984709382475";
        final Device d1 = createDevice(imei);
        final Device d2 = createDevice(imei);
        final Device d3 = createDevice("234870432987");

        dao.save(d1);
        dao.save(d2);
        dao.save(d3);

        final List<Device> allByImei = dao.findAllByImei(imei);
        assertEquals(2, allByImei.size());

        //test one from found
        final Device d = allByImei.get(0);
        assertEquals("Test Device", d.getName());
        assertEquals("124", d.getSn());
        assertEquals("Test device", d.getDescription());

        //test company
        final Company c = d.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }
}
