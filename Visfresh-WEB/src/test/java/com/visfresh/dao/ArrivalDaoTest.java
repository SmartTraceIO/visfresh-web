/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalDaoTest extends BaseCrudTest<ArrivalDao, Arrival, Long> {
    private DeviceDao deviceDao;
    private ShipmentDao shipmentDao;
    private Device device;
    private Shipment shipment;
    /**
     * Default constructor.
     */
    public ArrivalDaoTest() {
        super(ArrivalDao.class);
    }

    @Before
    public void beforeTest() {
        deviceDao = getContext().getBean(DeviceDao.class);
        shipmentDao = getContext().getBean(ShipmentDao.class);

        final Device d = new Device();
        d.setCompany(sharedCompany);
        final String imei = "932487032487";
        d.setImei(imei);
        d.setName("Test Device");
        d.setId(imei + ".1234");
        d.setDescription("JUnit device");
        d.setSn("12345");

        this.device = deviceDao.save(d);
        shipment = createShipment(d);
    }
    /**
     * @param d
     * @return
     */
    private Shipment createShipment(final Device d) {
        Shipment s = new Shipment();
        s.setName("Default profile");
        s.setCompany(sharedCompany);
        s.setDevice(d);
        s = shipmentDao.save(s);
        return s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Arrival createTestEntity() {
        final Date date = new Date(System.currentTimeMillis() - 1000000l);
        return createArrival(date);
    }

    /**
     * @param date
     * @return
     */
    protected Arrival createArrival(final Date date) {
        final Arrival a = new Arrival();
        a.setDate(date);
        a.setDevice(device);
        a.setShipment(shipment);
        a.setNumberOfMettersOfArrival(78);
        return a;
    }
    @Test
    public void testGetAlertsByShipmentAndTimeRanges() {
        final Date startDate = new Date(System.currentTimeMillis() - 1000000000l);
        final Date endDate = new Date(System.currentTimeMillis() - 1000000l);

        createAndSave(new Date(startDate.getTime() - 1000l));
        createAndSave(new Date(startDate.getTime()));
        createAndSave(new Date(startDate.getTime() + 10000l));
        createAndSave(new Date(endDate.getTime()));
        createAndSave(new Date(endDate.getTime() + 1000l));

        assertEquals(3, dao.getArrivals(shipment, startDate, endDate).size());

        //check left shipment
        final Shipment left = createShipment(device);
        assertEquals(0, dao.getArrivals(left, startDate, endDate).size());
    }

    /**
     * @param date the date.
     */
    private Arrival createAndSave(final Date date) {
        final Arrival a = createArrival(date);
        return dao.save(a);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Arrival a) {
        assertNotNull(a.getDate());
        assertEquals(78, a.getNumberOfMettersOfArrival());

        final Device d = a.getDevice();
        assertNotNull(d);

        assertEquals(device.getDescription(), d.getDescription());
        assertEquals(device.getId(), d.getId());
        assertEquals(device.getImei(), d.getImei());
        assertEquals(device.getName(), d.getName());
        assertEquals(device.getSn(), d.getSn());

        final Company c = d.getCompany();
        assertNotNull(c);

        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());

        assertNotNull(a.getShipment());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<Arrival> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final Arrival a = all.get(0);

        assertNotNull(a.getDate());
        assertEquals(78, a.getNumberOfMettersOfArrival());

        final Device d = a.getDevice();
        assertNotNull(d);

        assertEquals(device.getDescription(), d.getDescription());
        assertEquals(device.getId(), d.getId());
        assertEquals(device.getImei(), d.getImei());
        assertEquals(device.getName(), d.getName());
        assertEquals(device.getSn(), d.getSn());

        final Company c = d.getCompany();
        assertNotNull(c);

        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());

        assertNotNull(a.getShipment());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#clear()
     */
    @Override
    public void clear() {
        super.clear();
        shipmentDao.deleteAll();
        deviceDao.deleteAll();
    }
}
