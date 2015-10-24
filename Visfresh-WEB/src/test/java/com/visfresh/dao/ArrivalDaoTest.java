/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.Before;

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

        final Shipment s = new Shipment();
        s.setName("Default profile");
        s.setCompany(sharedCompany);
        s.getDevices().add(d);
        shipment = shipmentDao.save(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Arrival createTestEntity() {
        final Arrival a = new Arrival();
        a.setDate(new Date(System.currentTimeMillis() - 1000000l));
        a.setDevice(device);
        a.setShipment(shipment);
        a.setNumberOfMettersOfArrival(78);
        return a;
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
