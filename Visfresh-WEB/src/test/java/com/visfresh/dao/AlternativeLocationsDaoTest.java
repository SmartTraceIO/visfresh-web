/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlternativeLocationsDaoTest extends BaseDaoTest<AlternativeLocationsDao> {
    private Shipment shipment;
    /**
     * Default constructor.
     */
    public AlternativeLocationsDaoTest() {
        super(AlternativeLocationsDao.class);
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
        AlternativeLocations loc = new AlternativeLocations();
        loc.getFrom().add(createLocation("A"));
        loc.getTo().add(createLocation("B"));
        loc.getInterim().add(createLocation("C"));

        dao.save(shipment, loc);

        loc = dao.getBy(shipment);
        assertEquals(1, loc.getFrom().size());
        assertEquals(1, loc.getTo().size());
        assertEquals(1, loc.getInterim().size());

        assertEquals("A", loc.getFrom().get(0).getName());
        assertEquals("B", loc.getTo().get(0).getName());
        assertEquals("C", loc.getInterim().get(0).getName());
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
