/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.rules.state.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDaoTest extends BaseCrudTest<DeviceDao, Device, String> {
    private int num;
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
        return createDevice("3984709300000" + (++num));
    }

    /**
     * @param imei
     * @return
     */
    protected Device createDevice(final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setName("Test Device");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        d.setTripCount(5);
        return d;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCorrectSaved(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Device d) {
        assertNotNull(d.getImei());
        assertEquals("Test Device", d.getName());
        assertEquals("Test device", d.getDescription());
        assertEquals(5, d.getTripCount());

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

        assertNotNull(d.getImei());
        assertEquals("Test Device", d.getName());
        assertEquals("Test device", d.getDescription());
        assertEquals(5, d.getTripCount());

        //test company
        final Company c = d.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }
    @Test
    public void testFindByCompany() {
        createAndSaveDevice(sharedCompany, "293487032784");
        createAndSaveDevice(sharedCompany, "834270983474");

        assertEquals(2, dao.findByCompany(sharedCompany, null, null, null).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.findByCompany(left, null, null, null).size());
    }

    /**
     * @param c
     * @param imei
     */
    private Device createAndSaveDevice(final Company c, final String imei) {
        final Device d = createDevice(imei);
        d.setCompany(c);
        return dao.save(d);
    }

    @Test
    public void testGetByImei() {
        final String imei = "3984709382475";
        final Device d1 = createDevice(imei);
        final Device d3 = createDevice("234870432987");

        dao.save(d1);
        dao.save(d3);

        final Device d = dao.findByImei(imei);
        assertNotNull(d);

        //test one from found
        assertEquals("Test Device", d.getName());
        assertEquals("Test device", d.getDescription());

        //test company
        final Company c = d.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }
    @Test
    public void testDeviceState() {
        final Device d = createDevice("3984709382475");
        dao.save(d);

        final DeviceState s = new DeviceState();

        dao.saveState(d.getImei(), s);
        assertNotNull(dao.getState(d.getImei()));

        //test update state
        dao.saveState(d.getImei(), s);
        assertNotNull(dao.getState(d.getImei()));

        //test on delete trigger
        dao.delete(d);
        assertNull(dao.getState(d.getImei()));
    }
}
