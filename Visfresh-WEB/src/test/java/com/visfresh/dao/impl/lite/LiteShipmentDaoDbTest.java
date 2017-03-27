/**
 *
 */
package com.visfresh.dao.impl.lite;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.lite.LiteShipment;
import com.visfresh.controllers.lite.LiteShipmentResult;
import com.visfresh.dao.BaseDaoTest;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.LiteShipmentDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteShipmentDaoDbTest extends BaseDaoTest<LiteShipmentDao> {
    private Device device;

    /**
     * Default constructor.
     */
    public LiteShipmentDaoDbTest() {
        super(LiteShipmentDao.class);
    }

    @Before
    public void setUp() {
        device = createDevice("20394702394857");
    }
    /**
     * @param imei
     * @return
     */
    protected Device createDevice(final String imei) {
        final Device d = new Device();
        d.setCompany(sharedCompany);
        d.setImei(imei);
        d.setName("Test Device");
        d.setDescription("JUnit device");
        context.getBean(DeviceDao.class).save(d);
        return d;
    }
    /**
     * @param d
     * @return
     */
    private Shipment createShipment(final Device d) {
        return createShipment(d, ShipmentStatus.Default);
    }
    /**
     * @param d
     * @return
     */
    private Shipment createShipment(final Device d, final ShipmentStatus status) {
        Shipment s = new Shipment();
        s.setStatus(status);
        s.setCompany(sharedCompany);
        s.setDevice(d);
        s = context.getBean(ShipmentDao.class).save(s);
        return s;
    }
    /**
     * @param time
     * @param temperature temperature
     * @return
     */
    protected TrackerEvent createEvent(final Shipment shipment, final Date time, final double temperature) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(27);
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTemperature(temperature);
        e.setTime(time);
        e.setType(TrackerEventType.INIT);
        e.setLatitude(0.);
        e.setLongitude(0.);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    @Test
    public void testGetShipments() {
        createShipment(device);
        createShipment(device);
        final Shipment s3 = createShipment(device);

        List<LiteShipment> shipments = dao.getShipments(sharedCompany, null, null, null).getResult();
        assertEquals(3, shipments.size());

        shipments = dao.getShipments(sharedCompany, null, null, new Page(1, 2)).getResult();
        assertEquals(2, shipments.size());

        final LiteShipmentResult result = dao.getShipments(sharedCompany, null, null, new Page(3, 1));
        shipments = result.getResult();
        assertEquals(1, shipments.size());
        assertEquals(3, result.getTotalCount());
        assertEquals(s3.getId(), shipments.get(0).getShipmentId());
    }
    @Test
    public void testGetShipmentsKeyLocations() {
        final Shipment s = createShipment(device);

        final long t = System.currentTimeMillis() - 1000000l;
        createEvent(s, new Date(t + 1000l), 2.);
        createEvent(s, new Date(t + 2000l), 2.);
        createEvent(s, new Date(t + 3000l), 2.);
        createEvent(s, new Date(t + 4000l), 2.);
        createEvent(s, new Date(t + 5000l), 2.);

        final List<LiteShipment> result = dao.getShipments(sharedCompany, null, null, null).getResult();
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getKeyLocations().size());
    }
}
