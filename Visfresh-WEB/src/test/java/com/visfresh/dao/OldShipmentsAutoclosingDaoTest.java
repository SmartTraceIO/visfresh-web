/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class OldShipmentsAutoclosingDaoTest extends BaseDaoTest<OldShipmentsAutoclosingDao> {
    private Device device;

    /**
     * Default constructor.
     */
    public OldShipmentsAutoclosingDaoTest() {
        super(OldShipmentsAutoclosingDao.class);
    }

    @Before
    public void setUp() {
        device = createDevice("23984293087034");
    }

    /**
     * @param imei device IMEI.
     * @return device.
     */
    private Device createDevice(final String imei) {
        final Device d = new Device();
        d.setName("Test Device: " + imei);
        d.setImei(imei);
        d.setCompany(sharedCompany.getCompanyId());
        d.setDescription("Test Device: " + imei);
        return getContext().getBean(DeviceDao.class).save(d);
    }
    /**
     * @param status shipment status.
     * @return the shipment.
     */
    private Shipment createShipment(final ShipmentStatus status) {
        return createShipment(device, status);
    }

    /**
     * @param d device.
     * @param status shipment status.
     * @return shipment.
     */
    protected Shipment createShipment(final Device d, final ShipmentStatus status) {
        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompanyId());
        s.setStatus(status);
        return getContext().getBean(ShipmentDao.class).save(s);
    }

    @Test
    public void testFindNotClosedShipments() {
        createShipment(ShipmentStatus.Arrived);
        createShipment(ShipmentStatus.Ended);
        createShipment(ShipmentStatus.Default);
        createShipment(ShipmentStatus.InProgress);

        //test select with active device
        assertEquals(0, dao.findNotClosedShipmentsWithInactiveDevices(100).size());

        //set device inactive
        device.setActive(false);
        context.getBean(DeviceDao.class).save(device);
        assertEquals(2, dao.findNotClosedShipmentsWithInactiveDevices(100).size());

        //test limit
        assertEquals(1, dao.findNotClosedShipmentsWithInactiveDevices(1).size());
    }
    @Test
    public void testCloseShipments() {
        final Shipment s1 = createShipment(ShipmentStatus.Default);
        final Shipment s2 = createShipment(ShipmentStatus.InProgress);
        final Shipment s3 = createShipment(ShipmentStatus.Default);
        final Shipment s4 = createShipment(ShipmentStatus.InProgress);

        //close two first shipments
        final List<Long> ids = new LinkedList<>();
        ids.add(s1.getId());
        ids.add(s2.getId());

        dao.closeShipments(ids);

        //check result
        final ShipmentDao shipmentDao = context.getBean(ShipmentDao.class);
        assertEquals(ShipmentStatus.Ended, shipmentDao.findOne(s1.getId()).getStatus());
        assertEquals(ShipmentStatus.Ended, shipmentDao.findOne(s2.getId()).getStatus());
        assertEquals(ShipmentStatus.Default, shipmentDao.findOne(s3.getId()).getStatus());
        assertEquals(ShipmentStatus.InProgress, shipmentDao.findOne(s4.getId()).getStatus());
    }
    @Test
    public void testFindActiveShipmentsForDevice() {
        final Device d1 = createDevice("90328457093857");
        final Device d2 = createDevice("23487987297884");

        createShipment(d1, ShipmentStatus.Default);
        createShipment(d1, ShipmentStatus.InProgress);
        createShipment(d1, ShipmentStatus.Ended);
        createShipment(d1, ShipmentStatus.Arrived);

        createShipment(d2, ShipmentStatus.Default);
        createShipment(d2, ShipmentStatus.Ended);

        final List<String> ids = new LinkedList<>();
        ids.add(d1.getImei());
        Map<String, List<Long>> deviceShipments = dao.findActiveShipmentsFor(ids);
        assertEquals(1, deviceShipments.size());
        assertEquals(2, deviceShipments.get(d1.getImei()).size());

        ids.clear();
        ids.add(d2.getImei());
        deviceShipments = dao.findActiveShipmentsFor(ids);
        assertEquals(1, deviceShipments.size());
        assertEquals(1, deviceShipments.get(d2.getImei()).size());
    }
    @Test
    public void testFindDevicesWithoutReadingsAfter() {
        final Date date = new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000l);

        //device with reading after date
        final Device d1 = createDevice("90328457093857");
        //device without readings after date, but with readings before
        final Device d2 = createDevice("23487987297884");
        //device without any readings
        final Device d3 = createDevice("23989798798778");

        final Shipment s1 = createShipment(d1, ShipmentStatus.Default);
        final Shipment s2 = createShipment(d2, ShipmentStatus.Default);
        createShipment(d3, ShipmentStatus.Default);

        createReading(s1, new Date(date.getTime() - 100000l));
        createReading(s1, new Date(date.getTime() + 100000l));

        createReading(s2, new Date(date.getTime() - 100000l));

        final Set<String> devices = new HashSet<>(dao.findDevicesWithoutReadingsAfter(date));
        //3 devices = 2 given devices + 1 default device of test
        assertEquals(3, devices.size());
        assertTrue(devices.contains(device.getImei()));
        assertTrue(devices.contains(d2.getImei()));
        assertTrue(devices.contains(d3.getImei()));
    }

    /**
     * @param shipment shipment.
     * @param date event time.
     * @return tracker event.
     */
    private TrackerEvent createReading(final Shipment shipment, final Date date) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(12);
        e.setCreatedOn(new Date());
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTemperature(15.);
        e.setTime(date);
        e.setType(TrackerEventType.AUT);

        return context.getBean(TrackerEventDao.class).save(e);
    }
}
