/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopDaoTest extends BaseDaoTest<InterimStopDao> {
    private Shipment shipment;
//    private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");

    /**
     * Default constructor.
     */
    public InterimStopDaoTest() {
        super(InterimStopDao.class);
    }
    @Before
    public void setUp() {
        final Device d = new Device();
        d.setName("Test Device");
        d.setImei("23984293087034");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        getContext().getBean(DeviceDao.class).save(d);

        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(ShipmentStatus.InProgress);
        this.shipment = getContext().getBean(ShipmentDao.class).save(s);
    }
    @Test
    public void testGetById() {
        final Shipment s1 = createShipment(shipment.getDevice());
        final Shipment s2 = createShipment(shipment.getDevice());

        final InterimStop stop = createStop("A1");
        dao.save(s1, stop);

        assertNotNull(dao.findOne(s1, stop.getId()));
        assertNull(dao.findOne(s2, stop.getId()));
    }
    @Test
    public void testDelete() {
        final Shipment s1 = createShipment(shipment.getDevice());
        final Shipment s2 = createShipment(shipment.getDevice());

        final InterimStop stop = createStop("A1");
        dao.save(s1, stop);

        dao.delete(s2, stop);
        assertNotNull(dao.findOne(s1, stop.getId()));

        dao.delete(s1, stop);
        assertNull(dao.findOne(s1, stop.getId()));
    }
    @Test
    public void testSaveNew() {
        final InterimStop stop1 = createStop("A");
        final InterimStop stop2 = createStop("B");

        dao.save(shipment, stop1);
        dao.save(shipment, stop2);

        final List<InterimStop> stops = dao.getByShipment(shipment);
        assertEquals(2, stops.size());

        final InterimStop stop = stops.get(0);
        assertEquals(stop1.getId(), stop.getId());

        assertTrue(Math.abs(stop1.getDate().getTime() - stop.getDate().getTime()) < 1000);
        assertEquals(stop1.getLocation().getId(), stop.getLocation().getId());
        assertEquals(stop1.getTime(), stop.getTime());
    }
    @Test
    public void testUpate() {
        InterimStop stop = createStop("A");

        final LocationProfile loc = createLocation("B");
        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final int time = 300;

        stop.setLocation(loc);
        stop.setDate(date);
        stop.setTime(time);

        dao.save(shipment, stop);

        final Long id = stop.getId();
        stop = dao.findOne(shipment, id);

        assertEquals(id, stop.getId());

        assertTrue(Math.abs(date.getTime() - stop.getDate().getTime()) < 1000);
        assertEquals(loc.getId(), stop.getLocation().getId());
        assertEquals(time, stop.getTime());
    }
    @Test
    public void testUpdateTime() {
        final int minutes = 777737;
        InterimStop stop = createStop("A");
        dao.save(shipment, stop);

        dao.updateTime(stop.getId(), minutes);

        stop = dao.getByShipment(shipment).get(0);
        assertEquals(minutes, stop.getTime());
    }
    @Test
    public void testGetByShipmentIds() {
        final Shipment s1 = createShipment(shipment.getDevice());
        final Shipment s2 = createShipment(shipment.getDevice());
        final Shipment s3 = createShipment(shipment.getDevice());

        dao.save(s1, createStop("A1"));
        dao.save(s1, createStop("A2"));
        dao.save(s2, createStop("B1"));
        dao.save(s2, createStop("B2"));
        dao.save(s3, createStop("C1"));
        dao.save(s3, createStop("C3"));

        final List<Long> ids = new LinkedList<>();
        ids.add(s1.getId());
        ids.add(s2.getId());

        final Map<Long, List<InterimStop>> map = dao.getByShipmentIds(ids);
        assertEquals(2, map.size());
        assertEquals(2, map.get(s1.getId()).size());
        assertEquals(2, map.get(s2.getId()).size());
    }
    /**
     * @param device
     * @return
     */
    private Shipment createShipment(final Device device) {
        final Shipment s = new Shipment();
        s.setCompany(device.getCompany());
        s.setStatus(ShipmentStatus.Default);
        s.setDevice(device);
        s.setShipmentDescription("Created by autostart shipment rule");
        return context.getBean(ShipmentDao.class).save(s);
    }
    /**
     * @param locationName
     * @return
     */
    private InterimStop createStop(final String locationName) {
        final LocationProfile loc = createLocation(locationName);

        final InterimStop stop = new InterimStop();
        stop.setLocation(loc);
        stop.setDate(new Date());
        stop.setTime(15);

        return stop;
    }
    /**
     * @param name location name.
     * @return location.
     */
    private LocationProfile createLocation(final String name) {
        final LocationProfile l = new LocationProfile();
        l.setName(name);
        l.setCompany(sharedCompany);
        l.setRadius(300);
        l.setAddress("adderss of " + name);
        return getContext().getBean(LocationProfileDao.class).save(l);
    }
}
