/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSessionDaoTest extends BaseDaoTest<ShipmentSessionDao> {
    private Shipment shipment;
    /**
     * Default constructor.
     */
    public ShipmentSessionDaoTest() {
        super(ShipmentSessionDao.class);
    }

    @Before
    public void setUp() {
        Device d = new Device();
        d.setImei("9238470983274987");
        d.setName("Test Device");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        d.setTripCount(5);
        d = context.getBean(DeviceDao.class).save(d);

        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(ShipmentStatus.Arrived);
        this.shipment = getContext().getBean(ShipmentDao.class).save(s);
    }
    @Test
    public void testDeviceState() {
        //check not yet saved
        assertNull(dao.getSession(shipment));

        //check save
        final ShipmentSession s = new ShipmentSession(shipment.getId());
        s.setShipmentProperty("key1", "value1");

        dao.saveSession(shipment, s);
        assertEquals("value1", dao.getSession(shipment).getShipmentProperty("key1"));

        //check update
        s.setShipmentProperty("key1", "value2");
        dao.saveSession(shipment, s);

        assertEquals("value2", dao.getSession(shipment).getShipmentProperty("key1"));
    }
}
