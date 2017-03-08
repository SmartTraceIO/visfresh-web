/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

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
        d.setCompany(sharedCompany);
        d.setDescription("Test Device: " + imei);
        return getContext().getBean(DeviceDao.class).save(d);
    }
    /**
     * @param status shipment status.
     * @return the shipment.
     */
    private Shipment createShipment(final ShipmentStatus status) {
        final Shipment s = new Shipment();
        s.setDevice(device);
        s.setCompany(device.getCompany());
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
}
