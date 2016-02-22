/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.DeviceRestClient;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.lists.ListDeviceItem;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceControllerTest extends AbstractRestServiceTest {
    private DeviceDao dao;
    private DeviceRestClient client = new DeviceRestClient(UTC);

    /**
     * Default constructor.
     */
    public DeviceControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(DeviceDao.class);
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
    }
    //@RequestMapping(value = "/saveDevice/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveDevice(@PathVariable final String authToken,
    //        final @RequestBody String alert) {
    @Test
    public void testSaveDevice() throws RestServiceException, IOException {
        final Device p = createDevice("0239487043987", false);
        client.saveDevice(p);
        assertNotNull(dao.findOne(p.getImei()));
    }
    @Test
    public void testGetDevice() throws IOException, RestServiceException {
        final Device ap = createDevice("0239487043987", true);
        assertNotNull(client.getDevice(ap.getId()));
    }
    @Test
    public void testDeleteDevice() throws RestServiceException, IOException {
        final Device p = createDevice("0239487043987", true);
        client.deleteDevice(p);
        assertNull(dao.findOne(p.getId()));
    }
    //@RequestMapping(value = "/getDevices/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getDevices(@PathVariable final String authToken) {
    @Test
    public void testGetDevices() throws RestServiceException, IOException {
        createDevice("1111111111111", true);
        createDevice("2222222222222", true);
        final Device d1 = createDevice("3333333333333", true);

        Shipment s1 = createShipment(true);
        final Device toRemove = s1.getDevice();
        s1.setDevice(d1);
        s1 = context.getBean(ShipmentDao.class).save(s1);
        context.getBean(DeviceDao.class).delete(toRemove);

        //create readings with shipment
        createTrackerEvent(d1, s1);
        final TrackerEvent e1 = createTrackerEvent(d1, s1);

        final Device d2 = createDevice("4444444444444", true);
        //create readings without shipment
        createTrackerEvent(d2, null);
        createTrackerEvent(d2, null);

        assertEquals(4, client.getDevices(null, null).size());
        assertEquals(1, client.getDevices(1, 1).size());
        assertEquals(1, client.getDevices(2, 1).size());
        assertEquals(0, client.getDevices(5, 1).size());

        //check data for d1
        final ListDeviceItem item = client.getDevices(3, 1).get(0);
        assertEquals(d1.getDescription(), item.getDescription());
        assertEquals(d1.getId(), item.getImei());
        assertEquals(d1.getImei(), item.getImei());
        assertEquals(d1.getName(), item.getName());
        assertEquals(d1.getSn(), item.getSn());

        //last reading data
        assertEquals(e1.getBattery(), item.getLastReadingBattery().intValue());
        assertEquals(e1.getLatitude(), item.getLastReadingLat(), 0.0001);
        assertEquals(e1.getLongitude(), item.getLastReadingLong(), 0.0001);
        assertEquals(e1.getTemperature(), item.getLastReadingTemperature(), 0.0001);
        assertNotNull(item.getLastReadingTimeISO());
        assertEquals(e1.getShipment().getId(), item.getLastShipmentId());
    }
    @Test
    public void testSendCommandToDevice() throws RestServiceException, IOException {
        final Device device = createDevice("089723409857032498", true);
        client.saveDevice(device);

        client.sendCommandToDevice(device, "shutdown");
    }
    /**
     * @param device device.
     * @param shipment shipment.
     */
    private TrackerEvent createTrackerEvent(final Device device, final Shipment shipment) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(27);
        e.setDevice(device);
        e.setShipment(shipment);
        e.setTemperature(11);
        e.setTime(new Date());
        e.setType(TrackerEventType.INIT);
        return context.getBean(TrackerEventDao.class).save(e);
    }
}
