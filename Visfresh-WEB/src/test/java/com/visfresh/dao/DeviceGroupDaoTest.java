/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceGroupDaoTest extends BaseCrudTest<DeviceGroupDao, DeviceGroup, DeviceGroup, Long> {
    private static final String NAME_PREFIX = "dg_";
    private int lastId = 0;

    /**
     * Default cosntructor.
     */
    public DeviceGroupDaoTest() {
        super(DeviceGroupDao.class);
    }

    @Test
    public void testGetShipmentGroups() {
        final Device d1 = createDevice("12938401823709238");
        final Device d2 = createDevice("23049832094890204");
        final Device d3 = createDevice("20348708708709875");
        createDevice("20348708708709875");

        final Shipment s1 = createShipment(d1);
        final Shipment s2 = createShipment(d2);
        final Shipment s3 = createShipment(d3);

        final DeviceGroup grp1 = dao.save(createTestEntity());
        final DeviceGroup grp2 = dao.save(createTestEntity());

        dao.addDevice(grp1, d1);
        dao.addDevice(grp1, d2);
        dao.addDevice(grp2, d2);

        assertEquals(1, dao.getShipmentGroups(getIds(s1)).size());
        assertEquals(1, dao.getShipmentGroups(getIds(s1)).get(s1.getId()).size());
        assertEquals(1, dao.getShipmentGroups(getIds(s2)).size());
        assertEquals(2, dao.getShipmentGroups(getIds(s2)).get(s2.getId()).size());
        assertEquals(1, dao.getShipmentGroups(getIds(s3)).size());
        assertEquals(0, dao.getShipmentGroups(getIds(s3)).get(s3.getId()).size());
    }
    @Test
    public void testFindByName() {
        final DeviceGroup g1 = createGroup("G");
        createGroup("G");
        final DeviceGroup g2 = createGroup("NotG");

        assertEquals(g1.getId(), dao.findByName("G").getId());
        assertEquals(g2.getId(), dao.findByName("NotG").getId());
        assertNull(dao.findByName("wqpoiu"));
    }
    @Test
    public void testMoveToNewDevice() {
        final Device d1 = createDevice("12938401823709238");
        final Device d2 = createDevice("23049832094890204");

        final DeviceGroup grp1 = dao.save(createTestEntity());
        dao.addDevice(grp1, d1);

        dao.moveToNewDevice(d1, d2);

        assertEquals(0, dao.findByDevice(d1).size());
        assertEquals(1, dao.findByDevice(d2).size());
    }
    /**
     * @param groupName group name.
     */
    private DeviceGroup createGroup(final String groupName) {
        final DeviceGroup g = createTestEntity();
        g.setName(groupName);
        return dao.save(g);
    }
    /**
     * @param imei
     * @return
     */
    private Device createDevice(final String imei) {
        final Device d = new Device();
        d.setName("Test Device");
        d.setImei(imei);
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        return context.getBean(DeviceDao.class).save(d);
    }
    /**
     * @param d device.
     * @return shipment.
     */
    private Shipment createShipment(final Device d) {
        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(ShipmentStatus.InProgress);
        return getContext().getBean(ShipmentDao.class).save(s);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final DeviceGroup g) {
        assertNotNull(g.getCompany());
        assertTrue(g.getName().startsWith(NAME_PREFIX));
        assertTrue(g.getDescription().endsWith(g.getName()));

    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected DeviceGroup createTestEntity() {
        final DeviceGroup group = new DeviceGroup();
        group.setCompany(sharedCompany);
        group.setName(NAME_PREFIX + (lastId++));
        group.setDescription("Description of group " + group.getName());
        return group;
    }

    /**
     * @param s
     * @return
     */
    private Set<Long> getIds(final Shipment... s) {
        final Set<Long> ids = new HashSet<>();
        for (final Shipment shipment : s) {
            ids.add(shipment.getId());
        }
        return ids;
    }
}
