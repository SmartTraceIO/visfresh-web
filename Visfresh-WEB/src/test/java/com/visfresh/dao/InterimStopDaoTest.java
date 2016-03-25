/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");

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
    public void testSave() {
        final InterimStop stop1 = createStop("A");
        final InterimStop stop2 = createStop("B");

        dao.add(shipment, stop1);
        dao.add(shipment, stop2);

        final List<InterimStop> stops = dao.getByShipment(shipment);
        assertEquals(2, stops.size());

        final InterimStop stop = stops.get(0);
        assertEquals(stop1.getId(), stop.getId());

        assertEquals(format.format(stop1.getDate()), format.format(stop.getDate()));
        assertEquals(stop1.getLatitude(), stop.getLatitude(), 0.00001);
        assertEquals(stop1.getLongitude(), stop.getLongitude(), 0.00001);
        assertEquals(stop1.getLocation().getId(), stop.getLocation().getId());
        assertEquals(stop1.getTime(), stop.getTime());
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
        stop.setLatitude(1.0);
        stop.setLongitude(2.0);
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
