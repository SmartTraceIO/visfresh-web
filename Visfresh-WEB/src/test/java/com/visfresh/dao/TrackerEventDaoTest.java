/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.TrackerEventDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEventDaoTest extends BaseCrudTest<TrackerEventDao, TrackerEvent, TrackerEvent, Long> {
    /**
     * Device DAO.
     */
    private DeviceDao deviceDao;
    private ShipmentDao shipmentDao;
    /**
     * Device.
     */
    private Device device;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public TrackerEventDaoTest() {
        super(TrackerEventDao.class);
    }

    @Before
    public void beforeTest() {
        deviceDao = getContext().getBean(DeviceDao.class);
        shipmentDao = getContext().getBean(ShipmentDao.class);

        this.device = createDevice("932487032487");
        shipment = createShipment(device);
    }

    /**
     * @param imei
     * @return
     */
    protected Device createDevice(final String imei) {
        return createDevice(sharedCompany, imei);
    }

    /**
     * @param company
     * @param imei
     * @return
     */
    protected Device createDevice(final Company company, final String imei) {
        final Device d = new Device();
        d.setCompany(company.getCompanyId());
        d.setImei(imei);
        d.setName("Test Device");
        d.setDescription("JUnit device");
        deviceDao.save(d);
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
        s.setCompany(sharedCompany.getCompanyId());
        s.setDevice(d);
        s = shipmentDao.save(s);
        return s;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected TrackerEvent createTestEntity() {
        final Date time = new Date();
        return createEvent(time, 5.5);
    }

    /**
     * @param time
     * @param temperature temperature
     * @return
     */
    protected TrackerEvent createEvent(final Date time, final double temperature) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(27);
        e.setDevice(device);
        e.setShipment(shipment);
        e.setTemperature(temperature);
        e.setTime(time);
        e.setType(TrackerEventType.INIT);
        e.setLatitude(0.);
        e.setLongitude(0.);
        e.setHumidity(55);
        return e;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final TrackerEvent e) {
        assertNotNull(e.getTime());
        assertNotNull(e.getCreatedOn());
        assertEquals(27, e.getBattery());
        assertEquals(5.5, e.getTemperature(), 0.00001);
        assertEquals(TrackerEventType.INIT, e.getType());
        assertEquals(55, e.getHumidity().intValue());

        final Device d = e.getDevice();
        assertNotNull(d);

        assertEquals(device.getDescription(), d.getDescription());
        assertEquals(device.getId(), d.getId());
        assertEquals(device.getImei(), d.getImei());
        assertEquals(device.getName(), d.getName());
        assertEquals(device.getSn(), d.getSn());

        final Long c = d.getCompanyId();
        assertNotNull(c);

        assertEquals(sharedCompany.getId(), c);

        assertNotNull(e.getShipment());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<TrackerEvent> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        final TrackerEvent e = all.get(0);

        assertNotNull(e.getTime());
        assertEquals(27, e.getBattery());
        assertEquals(5.5, e.getTemperature(), 0.00001);
        assertEquals(TrackerEventType.INIT, e.getType());
        assertEquals(55, e.getHumidity().intValue());

        final Device d = e.getDevice();
        assertNotNull(d);

        assertEquals(device.getDescription(), d.getDescription());
        assertEquals(device.getId(), d.getId());
        assertEquals(device.getImei(), d.getImei());
        assertEquals(device.getName(), d.getName());
        assertEquals(device.getSn(), d.getSn());

        final Long c = d.getCompanyId();
        assertNotNull(c);

        assertEquals(sharedCompany.getId(), c);

        assertNotNull(e.getShipment());
    }
    @Test
    public void testGetAlertsByShipmentAndTimeRanges() {
        final Date startDate = new Date(System.currentTimeMillis() - 1000000000l);
        final Date endDate = new Date(System.currentTimeMillis() - 1000000l);

        createAndSave(new Date(startDate.getTime() - 1000l), 5.5);
        createAndSave(new Date(startDate.getTime()), 5.5);
        createAndSave(new Date(startDate.getTime() + 10000l), 5.5);
        createAndSave(new Date(endDate.getTime()), 5.5);
        createAndSave(new Date(endDate.getTime() + 1000l), 5.5);

        assertEquals(5, dao.getEvents(shipment).size());

        //check left shipment
        final Shipment left = createShipment(device);
        assertEquals(0, dao.getEvents(left).size());
    }
    @Test
    public void testGetPreviousEvent() {
        long time = System.currentTimeMillis() - 100000000l;

        final TrackerEvent first = createAndSave(new Date((time+=10000)), 12);
        createAndSave(new Date((time+=10000)), 11);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 9);
        createAndSave(new Date((time+=10000)), 8);
        final TrackerEvent prev = createAndSave(new Date((time+=10000)), 9);
        final TrackerEvent curr = createAndSave(new Date((time+=10000)), 11);
        createAndSave(new Date((time+=10000)), 12);
        createAndSave(new Date((time+=10000)), 13);
        createAndSave(new Date((time+=10000)), 12);

        final TrackerEvent e = dao.getPreviousEvent(curr);
        assertEquals(prev.getId(), e.getId());
        assertNull(dao.getPreviousEvent(first));
    }
    @Test
    public void createdOnNotUpdatable() {
        final long time = System.currentTimeMillis() - 100000000l;
        final TrackerEvent e = createAndSave(new Date((time)), 12);

        e.setCreatedOn(new Date(time));
        dao.save(e);

        assertTrue(dao.findOne(e.getId()).getCreatedOn().after(new Date(time + 100000l)));
    }
    @Test
    public void testGetLastEvent() {
        long time = System.currentTimeMillis() - 100000000l;

        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        final TrackerEvent e = createAndSave(new Date((time+=10000)), 10);

        assertEquals(e.getId(), dao.getLastEvent(shipment).getId());

        //create left shipment.
        Shipment s = shipmentDao.findOne(shipment.getId());
        s.setId(null);
        s = shipmentDao.save(s);

        //add event for left shipment
        final TrackerEvent eLeft = createEvent(new Date((time+=10000)), 10);
        eLeft.setShipment(s);
        dao.save(eLeft);

        assertEquals(e.getId(), dao.getLastEvent(shipment).getId());
    }
    @Test
    public void testGetFirstEvent() {
        long time = System.currentTimeMillis() - 100000000l;


        //add event for left shipment

        //create left shipment.
        Shipment s = shipmentDao.findOne(shipment.getId());
        s.setId(null);
        s = shipmentDao.save(s);

        final TrackerEvent eLeft = createEvent(new Date((time+=10000)), 10);
        eLeft.setShipment(s);
        dao.save(eLeft);

        //first event.
        final TrackerEvent e = createAndSave(new Date((time+=10000)), 10);
        //other events.
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);

        assertEquals(e.getId(), dao.getFirstEvent(shipment).getId());
    }
    @Test
    public void testGetEvents() {
        long time = System.currentTimeMillis() - 100000000l;

        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);
        createAndSave(new Date((time+=10000)), 10);

        assertEquals(7, dao.getEvents(shipment).size());

        //create left shipment.
        Shipment s = shipmentDao.findOne(shipment.getId());
        s.setId(null);
        s = shipmentDao.save(s);

        //add event for left shipment
        final TrackerEvent eLeft = createEvent(new Date((time+=10000)), 10);
        eLeft.setShipment(s);
        dao.save(eLeft);

        assertEquals(7, dao.getEvents(shipment).size());
    }
    @Test
    public void testGetLastEvents() {
        final Device d1 = createDevice("230984752039847");
        final Device d2 = createDevice("329048750298374");

        final Shipment s1 = createShipment(d1);
        createEvent(d1, s1);
        createEvent(d1, s1);
        createEvent(d1, s1);
        createEvent(d1, s1);
        final TrackerEvent e1 = createEvent(d1, s1);

        createEvent(d2, (Shipment) null);
        createEvent(d2, (Shipment) null);
        createEvent(d2, (Shipment) null);
        createEvent(d2, (Shipment) null);
        final TrackerEvent e2 = createEvent(d2, (Shipment) null);

        final List<Device> devices = new LinkedList<>();
        devices.add(d1);
        devices.add(d2);

        final List<ShortTrackerEvent> events = dao.getLastEvents(devices);
        assertEquals(2, events.size());

        Collections.sort(events);

        final ShortTrackerEvent ute1 = events.get(0);
        final ShortTrackerEvent ute2 = events.get(1);

        //check any fields of second event
        assertEquals(e2.getId(), ute2.getId());
        assertEquals(e2.getDevice().getImei(), ute2.getDeviceImei());
        assertEquals(e2.getDevice().getModel(), ute2.getDeviceModel());
        assertNull(ute2.getShipmentId());

        //fully check fields of first tracker event.
        assertEquals(e1.getId(), ute1.getId());
        assertEquals(e1.getDevice().getImei(), ute1.getDeviceImei());
        assertEquals(e1.getShipment().getId(), ute1.getShipmentId());
        assertEquals(e1.getBattery(), ute1.getBattery());
        assertEquals(e1.getLatitude(), ute1.getLatitude(), 0.001);
        assertEquals(e1.getLongitude(), ute1.getLongitude(), 0.001);
        assertEquals(e1.getTemperature(), ute1.getTemperature(), 0.001);
        assertEquals(e1.getTime().getTime(), ute1.getTime().getTime(), 2000);
        assertEquals(e1.getType(), ute1.getType());
    }
    @Test
    public void testFindByDeviceDateRanges() {
        final Device d1 = createDevice("234949838243985298");
        final Device d2 = createDevice("232398340987908790");

        final long dt = 100000l;
        final long t0 = System.currentTimeMillis() - 20 * dt;

        final TrackerEvent e1 = createEvent(d1, new Date(t0 + 1 * dt));
        createEvent(d2, new Date(t0 + 2 * dt));
        final TrackerEvent e2 = createEvent(d1, new Date(t0 + 3 * dt));
        createEvent(d2, new Date(t0 + 4 * dt));
        final TrackerEvent e3 = createEvent(d1, new Date(t0 + 5 * dt));
        createEvent(d2, new Date(t0 + 6 * dt));
        final TrackerEvent e4 = createEvent(d1, new Date(t0 + 7 * dt));
        createEvent(d2, new Date(t0 + 8 * dt));

        List<ShortTrackerEvent> events = dao.findBy(d1.getImei(), null, null);
        assertEquals(4, events.size());
        assertEquals(e1.getId(), events.get(0).getId());
        assertEquals(e4.getId(), events.get(events.size() - 1).getId());

        events = dao.findBy(d1.getImei(), new Date(t0 + 2 * dt), new Date(t0 + 6 * dt));
        assertEquals(2, events.size());
        assertEquals(e2.getId(), events.get(0).getId());
        assertEquals(e3.getId(), events.get(events.size() - 1).getId());
    }
    @Test
    public void testGetEventsAfterDate() {
        final Device d1 = createDevice("234949838243985298");
        final Device d2 = createDevice("232398340987908790");
        final Shipment s = createShipment(d1);

        final long dt = 100000l;
        final long t0 = System.currentTimeMillis() - 20 * dt;

        final TrackerEvent e1 = createEvent(d1, new Date(t0 + 1 * dt));
        createEvent(d2, new Date(t0 + 2 * dt));
        final TrackerEvent e2 = createEvent(d1, new Date(t0 + 3 * dt));
        createEvent(d2, new Date(t0 + 4 * dt));
        createEvent(d2, new Date(t0 + 6 * dt));
        final TrackerEvent e4 = createEvent(d1, new Date(t0 + 7 * dt));

        List<TrackerEvent> events = dao.getEventsAfterDate(s, null);
        assertEquals(3, events.size());
        assertEquals(e1.getId(), events.get(0).getId());
        assertEquals(e4.getId(), events.get(events.size() - 1).getId());

        events = dao.getEventsAfterDate(s, new Date(t0 + 2 * dt));
        assertEquals(2, events.size());
        assertEquals(e2.getId(), events.get(0).getId());
        assertEquals(e4.getId(), events.get(events.size() - 1).getId());

        //check shipment and device resolved
        final TrackerEvent one = events.get(0);
        assertNotNull(one.getShipment());
        assertNotNull(one.getDevice());
    }
    @Test
    public void testMoveToNewDevice() {
        final Device d1 = createDevice("390248703928740");
        final Device d2 = createDevice("293087098709870");

        final TrackerEvent e = createEvent(d1, (Shipment) null);

        dao.moveToNewDevice(d1, d2);
        assertEquals(d2.getImei(), dao.findOne(e.getId()).getDevice().getImei());
    }
    @Test
    public void testGetEventsForShipmentIds() {
        final Shipment s1 = createShipment(device);
        final Shipment s2 = createShipment(device);
        final Shipment s3 = createShipment(device);

        createEvent(s1.getDevice(), s1);
        createEvent(s1.getDevice(), s1);

        createEvent(s2.getDevice(), s2);
        createEvent(s2.getDevice(), s2);

        createEvent(s3.getDevice(), s3);
        createEvent(s3.getDevice(), s3);

        final List<Long> ids = new LinkedList<>();
        ids.add(s1.getId());
        ids.add(s2.getId());

        final Map<Long, List<TrackerEventDto>> map = dao.getEventsForShipmentIds(ids);
        assertEquals(2, map.size());
        assertEquals(2, map.get(s1.getId()).size());
        assertEquals(2, map.get(s2.getId()).size());
    }
    @Test
    public void testGetOrderedByTimeNotNullLocations() {
        final Shipment s = createShipment(device);

        long time = System.currentTimeMillis() - 1000000l;
        final TrackerEvent e1 = createEvent(s, new Date((time += 10000)));
        final TrackerEvent e2 = createEvent(s, new Date((time += 10000)));
        final TrackerEvent e3 = createEvent(s, new Date((time += 10000)));

        //test without limit
        List<TrackerEventDto> list = dao.getOrderedByTimeNotNullLocations(
                s.getId(), new Page(1, 1000), true);
        assertEquals(e1.getId(), list.get(0).getId());
        assertEquals(e3.getId(), list.get(2).getId());

        list = dao.getOrderedByTimeNotNullLocations(
                s.getId(), new Page(1, 1000), false);
        assertEquals(e3.getId(), list.get(0).getId());
        assertEquals(e1.getId(), list.get(2).getId());

        //test with limit
        list = dao.getOrderedByTimeNotNullLocations(
                s.getId(), new Page(1, 2), true);
        assertEquals(e1.getId(), list.get(0).getId());
        assertEquals(e2.getId(), list.get(1).getId());

        //next page
        list = dao.getOrderedByTimeNotNullLocations(
                s.getId(), new Page(2, 2), true);
        assertEquals(1, list.size());
        assertEquals(e3.getId(), list.get(0).getId());

        //reverse order
        list = dao.getOrderedByTimeNotNullLocations(
                s.getId(), new Page(1, 2), false);
        assertEquals(e3.getId(), list.get(0).getId());
        assertEquals(e2.getId(), list.get(1).getId());

        //next page
        list = dao.getOrderedByTimeNotNullLocations(
                s.getId(), new Page(2, 2), false);
        assertEquals(1, list.size());
        assertEquals(e1.getId(), list.get(0).getId());
    }
    @Test
    public void testAssignShipment() {
        final Shipment s = createShipment(device);

        final TrackerEvent e = createEvent(s.getDevice(), (Shipment) null);
        dao.assignShipment(e.getId(), s);

        assertEquals(s.getId(), dao.findOne(e.getId()).getShipment().getId());
    }
    @Test
    public void testFindByCompanyDateRanges() {
        final Company c1 = createCompany("C1");
        final Company c2 = createCompany("C2");

        final Device d1 = createDevice(c1, "32908470987908");
        final Device d2 = createDevice(c2, "02398470238472");
        final Shipment s1 = createShipment(d1, ShipmentStatus.Arrived);
        final Shipment s2 = createShipment(d1, ShipmentStatus.Arrived);
        final Shipment s3 = createShipment(d2, ShipmentStatus.Arrived);

        final long dt = 100000l;
        final long startDate = System.currentTimeMillis() - 100 * dt;

        createEvent(s1, new Date(startDate + 5 * dt));
        createEvent(s1, new Date(startDate + 10 * dt));

        createEvent(s2, new Date(startDate + 15 * dt));
        createEvent(s2, new Date(startDate + 20 * dt));

        createEvent(s3, new Date(startDate + 25 * dt));
        createEvent(s3, new Date(startDate + 30 * dt));

        assertEquals(4, dao.findForArrivedShipmentsInDateRanges(c1, null, null, null, null).size());
        assertEquals(4, dao.findForArrivedShipmentsInDateRanges(c1,
                new Date(startDate + 4 * dt), new Date(startDate + 21 * dt), null, null).size());

        assertEquals(3, dao.findForArrivedShipmentsInDateRanges(c1,
                new Date(startDate + 9 * dt), new Date(startDate + 21 * dt), null, null).size());
        assertEquals(2, dao.findForArrivedShipmentsInDateRanges(c1,
                new Date(startDate + 9 * dt), new Date(startDate + 16 * dt), null, null).size());

        //test pages
        assertEquals(2, dao.findForArrivedShipmentsInDateRanges(c1,
                new Date(startDate + 9 * dt), new Date(startDate + 21 * dt), null, new Page(1, 2)).size());
        assertEquals(1, dao.findForArrivedShipmentsInDateRanges(c1,
                new Date(startDate + 9 * dt), new Date(startDate + 21 * dt), null, new Page(2, 2)).size());
        assertEquals(0, dao.findForArrivedShipmentsInDateRanges(c1,
                new Date(startDate + 9 * dt), new Date(startDate + 21 * dt), null, new Page(3, 2)).size());
    }
    @Test
    public void testSaveWithGateway() {
        final String gateway = "any-gateway";

        final TrackerEvent e = createTestEntity();
        e.setGateway(gateway);
        dao.save(e);

        final Set<Long> shipments = new HashSet<>();
        shipments.add(e.getShipment().getId());

        final TrackerEventDto e1 = dao.getEventPart(shipments, 0, 1000).get(0);
        assertEquals(gateway, e1.getGateway());
        assertEquals(gateway, dao.findOne(e.getId()).getGateway());
    }
    /**
     * @param device device.
     * @param shipment shipment.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Device device, final Shipment shipment) {
        final TrackerEvent e = createTestEntity();
        e.setDevice(device);
        e.setShipment(shipment);
        return dao.save(e);
    }
    /**
     * @param device device.
     * @param shipment shipment.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Device device, final Date date) {
        final TrackerEvent e = createEvent(date, 1.0);
        e.setDevice(device);
        return dao.save(e);
    }
    /**
     * @param shipment device.
     * @param date event date.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Shipment shipment, final Date date) {
        final TrackerEvent e = createEvent(date, 1.0);
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        return dao.save(e);
    }
    /**
     * @param date
     * @param temperature temperature
     */
    private TrackerEvent createAndSave(final Date date, final double temperature) {
        return dao.save(createEvent(date, temperature));
    }
}
