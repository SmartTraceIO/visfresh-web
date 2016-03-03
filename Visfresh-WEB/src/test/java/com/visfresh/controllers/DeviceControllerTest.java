/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceConstants;
import com.visfresh.controllers.restclient.DeviceRestClient;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.SystemMessageDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.lists.ListDeviceItemDto;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.SerializerUtils;

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
        createTrackerEvent(d1, s1, 11.);
        final TrackerEvent e1 = createTrackerEvent(d1, s1, 23.456);

        final Device d2 = createDevice("4444444444444", true);
        //create readings without shipment
        createTrackerEvent(d2, null, 11.);
        createTrackerEvent(d2, null, 11.);

        assertEquals(4, client.getDevices(null, true, null, null).size());
        assertEquals(1, client.getDevices(null, true, 1, 1).size());
        assertEquals(1, client.getDevices(null, true, 2, 1).size());
        assertEquals(0, client.getDevices(null, true, 5, 1).size());

        //check data for d1
        final ListDeviceItemDto item = client.getDevices(null, true, 3, 1).get(0);
        assertEquals(d1.getDescription(), item.getDescription());
        assertEquals(d1.getId(), item.getImei());
        assertEquals(d1.getImei(), item.getImei());
        assertEquals(d1.getName(), item.getName());
        assertEquals(d1.getSn(), item.getSn());

        //last reading data
        assertEquals(e1.getBattery(), item.getLastReadingBattery().intValue());
        assertEquals(e1.getLatitude(), item.getLastReadingLat(), 0.0001);
        assertEquals(e1.getLongitude(), item.getLastReadingLong(), 0.0001);
        assertEquals(LocalizationUtils.getTemperatureString(23.46, TemperatureUnits.Celsius),
                item.getLastReadingTemperature());
        assertNotNull(item.getLastReadingTimeISO());
        assertEquals(e1.getShipment().getId(), item.getLastShipmentId());
    }
    @Test
    public void testGetDevicesSortByImei() throws RestServiceException, IOException {
        final Device d1 = createDevice("3333333333333", true);
        final Device d2 = createDevice("2222222222222", true);
        final Device d3 = createDevice("1111111111111", true);

        List<ListDeviceItemDto> dto;

        dto = client.getDevices(DeviceConstants.PROPERTY_IMEI, true, null, null);
        assertEquals(d3.getImei(), dto.get(0).getImei());
        assertEquals(d2.getImei(), dto.get(1).getImei());
        assertEquals(d1.getImei(), dto.get(2).getImei());

        dto = client.getDevices(DeviceConstants.PROPERTY_IMEI, false, null, null);
        assertEquals(d1.getImei(), dto.get(0).getImei());
        assertEquals(d2.getImei(), dto.get(1).getImei());
        assertEquals(d3.getImei(), dto.get(2).getImei());
    }
    @Test
    public void testSendCommandToDevice() throws RestServiceException, IOException {
        final Device device = createDevice("089723409857032498", true);
        client.saveDevice(device);

        client.sendCommandToDevice(device, "shutdown");
    }
    @Test
    public void testShutdownDevice() throws RestServiceException, IOException {
        final Device device = createDevice("089723409857032498", true);
        client.shutdownDevice(device.getImei());

        //check result.
        final List<SystemMessage> messages = context.getBean(SystemMessageDao.class).findAll(null, null, null);
        assertEquals(1, messages.size());

        final JsonObject json = SerializerUtils.parseJson(messages.get(0).getMessageInfo()).getAsJsonObject();
        assertEquals("SHUTDOWN#", json.get("command").getAsString());
        assertEquals(device.getImei(), json.get("imei").getAsString());
    }
    /**
     * @param device device.
     * @param shipment shipment.
     */
    private TrackerEvent createTrackerEvent(final Device device, final Shipment shipment, final double t) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(27);
        e.setLatitude(12.34);
        e.setLongitude(56.78);
        e.setDevice(device);
        e.setShipment(shipment);
        e.setTemperature(t);
        e.setTime(new Date());
        e.setType(TrackerEventType.INIT);
        return context.getBean(TrackerEventDao.class).save(e);
    }
}
