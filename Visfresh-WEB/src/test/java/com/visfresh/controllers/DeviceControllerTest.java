/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;

import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceConstants;
import com.visfresh.controllers.restclient.DeviceRestClient;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.SystemMessageDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.DeviceModel;
import com.visfresh.entities.Language;
import com.visfresh.entities.ListDeviceItem;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.mock.MockEmailService;
import com.visfresh.mock.MockShipmentShutdownService;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceControllerTest extends AbstractRestServiceTest {
    private DeviceDao dao;
    private DeviceRestClient client;
    //check latest reading:
    final DateFormat format = DateTimeUtils.createDateFormat(
            "yyyy-MM-dd HH:mm", Language.English, UTC);

    /**
     * Default constructor.
     */
    public DeviceControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(DeviceDao.class);

        final User user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
        client = new DeviceRestClient(user);
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login(user));
    }
    @After
    public void tearDown() {
        context.getBean(MockEmailService.class).clear();
    }
    //@RequestMapping(value = "/saveDevice", method = RequestMethod.POST)
    //public @ResponseBody String saveDevice(
    //        final @RequestBody String alert) {
    @Test
    public void testSaveDevice() throws RestServiceException, IOException {
        final Device p = createDevice("0239487043987", true);
        client.saveDevice(p);
        assertNotNull(dao.findOne(p.getImei()));
    }
    @Test
    public void testDeviceStateChanged() throws RestServiceException, IOException {
        final Device p = createDevice("0239487043987", true);

        final boolean activeState = !p.isActive();
        p.setActive(activeState);
        client.saveDevice(p);

        assertEquals(activeState, dao.findOne(p.getImei()).isActive());

        //check email to support sent
        assertEquals(1, context.getBean(MockEmailService.class).getMessages().size());
    }
    @Test
    public void testGetDevice() throws IOException, RestServiceException {
        final Device ap = createDevice("0239487043987", true);

        Shipment s1 = createShipment(true);
        final Device toRemove = s1.getDevice();
        s1.setDevice(ap);
        s1 = context.getBean(ShipmentDao.class).save(s1);
        context.getBean(DeviceDao.class).delete(toRemove);

        //create readings with shipment
        createTrackerEvent(ap, s1, 11.);
        createTrackerEvent(ap, s1, 23.456);

        final Device device = client.getDevice(ap.getId());
        assertNotNull(device);
        assertEquals(ap.getModel(), device.getModel());
    }
//    @Test
//    public void testDeleteDevice() throws RestServiceException, IOException {
//        final Device p = createDevice("0239487043987", true);
//        client.deleteDevice(p);
//        assertNull(dao.findOne(p.getId()));
//    }
    //@RequestMapping(value = "/getDevices", method = RequestMethod.GET)
    //public @ResponseBody String getDevices() {
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

        //create autostart shipment template
        ShipmentTemplate tpl = new ShipmentTemplate();
        tpl.setCompany(getCompanyId());
        tpl.setName("TPL1");
        tpl = getContext().getBean(ShipmentTemplateDao.class).save(tpl);

        AutoStartShipment aut = new AutoStartShipment();
        aut.setCompany(getCompanyId());
        aut.setTemplate(tpl);
        aut.setPriority(10);
        aut = getContext().getBean(AutoStartShipmentDao.class).save(aut);

        d1.setAutostartTemplateId(aut.getId());
        dao.save(d1);

        //create readings without shipment
        createTrackerEvent(d2, null, 11.);
        createTrackerEvent(d2, null, 11.);

        assertEquals(4, client.getDevices(null, true, null, null).size());
        assertEquals(1, client.getDevices(null, true, 1, 1).size());
        assertEquals(1, client.getDevices(null, true, 2, 1).size());
        assertEquals(0, client.getDevices(null, true, 5, 1).size());

        //check data for d1
        final ListDeviceItem item = client.getDevices(null, true, 3, 1).get(0);
        assertEquals(d1.getDescription(), item.getDescription());
        assertEquals(d1.getId(), item.getImei());
        assertEquals(d1.getImei(), item.getImei());
        assertEquals(d1.getModel(), item.getModel());
        assertEquals(d1.getName(), item.getName());
        assertEquals(tpl.getName(), item.getAutostartTemplateName());
        assertEquals(aut.getId(), item.getAutostartTemplateId());

        //last reading data
        assertEquals(e1.getBattery(), item.getBattery().intValue());
        assertEquals(e1.getLatitude(), item.getLatitude(), 0.0001);
        assertEquals(e1.getLongitude(), item.getLongitude(), 0.0001);
        assertEquals(23.46, item.getTemperature(), 0.1);
        assertNotNull(item.getLastReadingTime());
        assertEquals(e1.getShipment().getId(), item.getShipmentId());
    }
    @Test
    public void testGetDevicesSerialNumber() throws RestServiceException, IOException {
        final String imei = "3333333333333";
        final Device d = createDevice(imei, true);
        createShipment(d, true);

        JsonObject device = client.getDevicesJson(null, true, null, null).get(0).getAsJsonObject();
        assertEquals("333333", device.get("sn").getAsString());
        assertEquals("333333(1)", device.get("shipmentNumber").getAsString());

        d.setModel(DeviceModel.STB1);
        dao.save(d);

        device = client.getDevicesJson(null, true, null, null).get(0).getAsJsonObject();
        assertEquals(imei, device.get("sn").getAsString());
        assertEquals(imei + "(1)", device.get("shipmentNumber").getAsString());
    }
    @Test
    public void testGetDevicesSortByImei() throws RestServiceException, IOException {
        final Device d1 = createDevice("3333333333333", true);
        final Device d2 = createDevice("2222222222222", true);
        final Device d3 = createDevice("1111111111111", true);

        List<ListDeviceItem> dto;

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
    public void testGetDevicesSortByModel() throws RestServiceException, IOException {
        final Device d1 = createDevice("2222222222222", true);
        d1.setModel(DeviceModel.TT18);
        final Device d2 = createDevice("3333333333333", true);
        d2.setModel(DeviceModel.SmartTrace);
        dao.save(d1);
        dao.save(d2);

        List<ListDeviceItem> dto;

        dto = client.getDevices(DeviceConstants.PROPERTY_MODEL, true, null, null);
        assertEquals(d2.getImei(), dto.get(0).getImei());
        assertEquals(d1.getImei(), dto.get(1).getImei());

        dto = client.getDevices(DeviceConstants.PROPERTY_MODEL, false, null, null);
        assertEquals(d1.getImei(), dto.get(0).getImei());
        assertEquals(d2.getImei(), dto.get(1).getImei());
    }
    @Test
    public void testSortByShipmentNumber() throws RestServiceException, IOException {
        final Device d1 = createDevice("3333333333333", true);
        final Device d2 = createDevice("2222222222222", true);
        final Device d3 = createDevice("4444444444444", true);

        createShipment(d1, true);
        createShipment(d2, true);
        //d3 without shipment, and therefore without shipment number

        List<ListDeviceItem> dto;

        dto = client.getDevices(DeviceConstants.PROPERTY_SHIPMENT_NUMBER, true, null, null);
        assertEquals(d2.getImei(), dto.get(0).getImei());
        assertEquals(d1.getImei(), dto.get(1).getImei());
        assertEquals(d3.getImei(), dto.get(2).getImei());

        dto = client.getDevices(DeviceConstants.PROPERTY_SHIPMENT_NUMBER, false, null, null);
        assertEquals(d3.getImei(), dto.get(0).getImei());
        assertEquals(d1.getImei(), dto.get(1).getImei());
        assertEquals(d2.getImei(), dto.get(2).getImei());
    }
    @Test
    public void testSortByShipmentStatus() throws RestServiceException, IOException {
        final Device d1 = createDevice("3333333333333", true);
        final Device d2 = createDevice("2222222222222", true);
        final Device d3 = createDevice("1111111111111", true);

        final Shipment s1 = createShipment(d1, true);
        s1.setStatus(ShipmentStatus.Ended);
        final Shipment s2 = createShipment(d2, true);
        s2.setStatus(ShipmentStatus.Default);
        final Shipment s3 = createShipment(d3, true);
        s3.setStatus(ShipmentStatus.Arrived);

        context.getBean(ShipmentDao.class).save(s1);
        context.getBean(ShipmentDao.class).save(s2);
        context.getBean(ShipmentDao.class).save(s3);

        List<ListDeviceItem> dto;

        dto = client.getDevices(DeviceConstants.PROPERTY_SHIPMENT_STATUS, true, null, null);
        assertEquals(d3.getImei(), dto.get(0).getImei());
        assertEquals(d2.getImei(), dto.get(1).getImei());
        assertEquals(d1.getImei(), dto.get(2).getImei());

        dto = client.getDevices(DeviceConstants.PROPERTY_SHIPMENT_STATUS, false, null, null);
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
        final Shipment s = this.createShipment(true);
        s.setStatus(ShipmentStatus.InProgress);
        saveShipmentDirectly(s);

        client.shutdownDevice(s);

        //check result.
        final Date shutdownDate = context.getBean(MockShipmentShutdownService.class).getShutdownDate(s.getId());
        assertNotNull(shutdownDate);

        //check shipment state
        assertEquals(ShipmentStatus.Ended, context.getBean(ShipmentDao.class).findOne(s.getId()).getStatus());
    }
    @Test
    public void testShutdownNotLatestDevice() throws RestServiceException, IOException {
        Shipment s = this.createShipment(true);
        s.setStatus(ShipmentStatus.InProgress);
        saveShipmentDirectly(s);

        final Shipment last = this.createShipment(true);
        last.setDevice(s.getDevice());
        last.setStatus(ShipmentStatus.InProgress);
        saveShipmentDirectly(last);

        client.shutdownDevice(s);

        //check not shutdown message created.
        final List<SystemMessage> messages = context.getBean(SystemMessageDao.class).findAll(null, null, null);
        assertEquals(0, messages.size());

        //but shipment stopped
        s = context.getBean(ShipmentDao.class).findOne(s.getId());
        assertEquals(ShipmentStatus.Ended, s.getStatus());
    }
    @Test
    public void testMoveDevice() throws IOException, RestServiceException, BeansException {
        final Company c1 = createCompany("C1");
        final Company c2 = createCompany("C2");

        //create device
        final Device d = createDevice("0239870932487", false);
        d.setCompany(c1.getCompanyId());
        dao.save(d);

        //create device group for device
        final DeviceGroup dg = new DeviceGroup();
        dg.setCompany(c1.getCompanyId());
        dg.setName("DG-1");

        final DeviceGroupDao dgd = context.getBean(DeviceGroupDao.class);
        dgd.save(dg);

        //add device to group
        dgd.addDevice(dg, d);

        //create device command
        final DeviceCommand cmd = new DeviceCommand();
        cmd.setCommand("start");
        cmd.setDevice(d);
        context.getBean(DeviceCommandDao.class).save(cmd);

        //create shipments
        final Shipment s = createShipment(d, true);

        //create tracker events
        final TrackerEvent e = createTrackerEvent(d, s, 1.);

        //create alerts
        final Alert al = createAlert(d, s);

        //create arrival
        final Arrival ar = createArrival(d, s);

        try {
            client.moveDevice(d, c2);
            throw new AssertionFailedError("Security exception should be thrown");
        } catch (final Exception e1) {
            //correct should not allow this operation for normal user
        }

        final User u = new User();
        u.setRoles(new LinkedList<Role>());
        u.getRoles().add(Role.SmartTraceAdmin);
        u.setCompany(getCompanyId());
        u.setEmail("admin@smartrace.com.au");
        u.setFirstName("JUnit");
        u.setLastName("Admin");
        context.getBean(AuthService.class).saveUser(u, "", false);

        //relogin by smarttrace admin
        final String token = context.getBean(AuthService.class).login(u.getEmail(),"", "junit").getToken();
        client.setAuthToken(token);

        //do move
        final String newImei = client.moveDevice(d, c2);

        //check device really moved
        assertEquals(c2.getId(), dao.findByImei(d.getImei()).getCompanyId());

        //check device with given IMEI
        final Device newDevice = dao.findByImei(newImei);
        assertNotNull(newDevice);

        //check new device has some company

        //check device group has updated device
        assertEquals(dg.getId(), context.getBean(DeviceGroupDao.class).findByDevice(newDevice).get(0).getId());

        //check device commands removed
        assertEquals(0, context.getBean(DeviceCommandDao.class).findAll(null, null, null).size());

        //check shipment moved
        final Shipment shipment = context.getBean(ShipmentDao.class).findOne(s.getId());
        assertEquals(newImei, shipment.getDevice().getImei());
        assertTrue(shipment.hasFinalStatus());

        //check alert moved
        assertEquals(newImei, context.getBean(AlertDao.class).findOne(al.getId()).getDevice().getImei());

        //check arrival moved
        assertEquals(newImei, context.getBean(ArrivalDao.class).findOne(ar.getId()).getDevice().getImei());

        //check tracker event moved
        assertEquals(newImei, context.getBean(TrackerEventDao.class).findOne(e.getId()).getDevice().getImei());
    }
    @Test
    public void testInitDeviceColors() throws IOException, RestServiceException {
        final Device d1 = createDevice("0239870932487", true);
        final Device d2 = createDevice("2934879823477", true);
        final Device d3 = createDevice("2987943878487", true);

        client.initDeviceColors(d1.getCompanyId());

        assertNotNull(dao.findOne(d1.getImei()).getColor());
        assertNotNull(dao.findOne(d2.getImei()).getColor());
        assertNotNull(dao.findOne(d3.getImei()).getColor());
    }
    @Test
    public void testInitDeviceColorsNullCompany() throws IOException, RestServiceException {
        final Device d1 = createDevice("0239870932487", true);
        final Device d2 = createDevice("2934879823477", true);
        final Device d3 = createDevice("2987943878487", true);

        client.initDeviceColors(null);

        assertNotNull(dao.findOne(d1.getImei()).getColor());
        assertNotNull(dao.findOne(d2.getImei()).getColor());
        assertNotNull(dao.findOne(d3.getImei()).getColor());
    }
    @Test
    public void testGetReadings() throws IOException, RestServiceException {
        final Device d1 = createDevice("1234987039487", true);
        final Device d2 = createDevice("9324790898877", true);

        final long dt = 60 * 60 * 1000l; //one hour
        final long t0 = System.currentTimeMillis() - 5 * dt;
        createTrackerEvent(d1, new Date(t0 + 1 * dt));
        createTrackerEvent(d1, new Date(t0 + 2 * dt));
        createTrackerEvent(d2, new Date(t0 + 3 * dt));

        final Shipment s = createShipment(d1, true);
        final TrackerEvent e = createTrackerEvent(d1, new Date(t0 + 4 * dt));
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        String data =  client.getReadings(d1, new Date(t0 + 2 * dt - 10000), new Date(t0 + 3 * dt + 10000));
        assertEquals(2, data.split("\n").length);

        //test without start date
        data =  client.getReadings(d1, null, new Date(t0 + 3 * dt + 10000));
        assertEquals(3, data.split("\n").length);

        //test without end date
        data =  client.getReadings(d1, new Date(t0 + 2 * dt - 10000), null);
        assertEquals(3, data.split("\n").length);

        //test without date ranges
        data =  client.getReadings(d1, null, null);
        assertEquals(4, data.split("\n").length);

        final String[] str = data.split("\n")[3].split(",");
        //id
        assertEquals(Long.toString(e.getId()), str[0]);
        //shipment
        assertEquals("703948(1)", str[1]);
        //time
        assertTrue(getDiferenceMs(e.getTime(), str[2]) < 61000);
        //temperature
        assertEquals(LocalizationUtils.convertToUnitsString(e.getTemperature(), TemperatureUnits.Celsius), str[3]);
        //humidity
        assertEquals(e.getHumidity() + "%", str[4]);
        //battery
        assertEquals(Integer.toString(e.getBattery()), str[5]);
        //latitude
        assertEquals(Double.toString(e.getLatitude()), str[6]);
        //longitude
        assertEquals(Double.toString(e.getLongitude()), str[7]);
        //device
        assertEquals("\"" + e.getDevice().getImei() + "\"", str[8]);
        //createdon
        assertTrue(getDiferenceMs(e.getCreatedOn(), str[9]) <  61000);
        //type
        assertEquals("SwitchedOn", str[10]);
    }
    @Test
    public void testGetReadingsDeviceSn() throws IOException, RestServiceException {
        final Device d1 = createDevice("1234987039487", true);

        final long dt = 60 * 60 * 1000l; //one hour
        final long t0 = System.currentTimeMillis() - 5 * dt;

        final Shipment s = createShipment(d1, true);
        final TrackerEvent e = createTrackerEvent(d1, new Date(t0 + 3 * dt));
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        String data =  client.getReadings(d1, new Date(t0 + 2 * dt - 10000), new Date(t0 + 3 * dt + 10000));
        String[] str = data.split("\n")[1].split(",");

        //shipment
        assertEquals("703948(1)", str[1]);

        //change device model
        d1.setModel(DeviceModel.STB1);
        dao.save(d1);

        data =  client.getReadings(d1, new Date(t0 + 2 * dt - 10000), new Date(t0 + 3 * dt + 10000));
        str = data.split("\n")[1].split(",");

        //shipment
        assertEquals("1234987039487(1)", str[1]);
    }
    @Test
    public void testGetReadingsByShipmentId() throws IOException, RestServiceException {
        final Device d1 = createDevice("1234987039487", true);

        final long dt = 60 * 60 * 1000l; //one hour
        final long t0 = System.currentTimeMillis() - 5 * dt;
        createTrackerEvent(d1, new Date(t0 + 1 * dt));
        createTrackerEvent(d1, new Date(t0 + 2 * dt));

        final Shipment s = createShipment(d1, true);
        createTrackerEvent(s, 10.);
        createTrackerEvent(s, 10.);
        createTrackerEvent(s, 10.);
        createTrackerEvent(s, 10.);

        //check by shipment parameter
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipment", s.getId().toString());

        //should be 5 - header and 4 readings
        assertEquals(5, client.doSendGetRequest(client.getPathWithToken("getReadings"), params)
                .split("\n").length);

        //check by shipment parameter
        params.clear();
        params.put("shipmentId", s.getId().toString());

        //should be 5 - header and 4 readings
        assertEquals(5, client.doSendGetRequest(client.getPathWithToken("getReadings"), params)
                .split("\n").length);
    }
    /**
     * @param date
     * @param dateStr
     * @return
     */
    private long getDiferenceMs(final Date date, final String dateStr) {
        try {
            return Math.abs(date.getTime() - format.parse(dateStr).getTime());
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetReadingsBySnTrip() throws IOException, RestServiceException {
        final Device d1 = createDevice("1234987039487", true);
        final Device d2 = createDevice("9324790898877", true);

        final long dt = 60 * 60 * 1000l; //one hour
        final long t0 = System.currentTimeMillis() - 5 * dt;
        createTrackerEvent(d1, new Date(t0 + 1 * dt));
        createTrackerEvent(d1, new Date(t0 + 2 * dt));
        createTrackerEvent(d2, new Date(t0 + 3 * dt));

        final Shipment s = createShipment(d1, true);
        final TrackerEvent e = createTrackerEvent(d1, new Date(t0 + 4 * dt));
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        final String data =  client.getReadings(s);
        assertEquals(2, data.split("\n").length);

        final String[] str = data.split("\n")[1].split(",");
        //id
        assertEquals(Long.toString(e.getId()), str[0]);
        //shipment
        assertEquals("703948(1)", str[1]);
        //time
        assertTrue(getDiferenceMs(e.getTime(), str[2]) < 61000l);
        //temperature
        assertEquals(LocalizationUtils.convertToUnitsString(e.getTemperature(), TemperatureUnits.Celsius), str[3]);
        //humidity
        assertEquals(e.getHumidity() + "%", str[4]);
        //battery
        assertEquals(Integer.toString(e.getBattery()), str[5]);
        //latitude
        assertEquals(Double.toString(e.getLatitude()), str[6]);
        //longitude
        assertEquals(Double.toString(e.getLongitude()), str[7]);
        //device
        assertEquals("\"" + e.getDevice().getImei() + "\"", str[8]);
        //createdon
        assertTrue(getDiferenceMs(e.getCreatedOn(), str[9]) < 61000l);
        //type
        assertEquals("SwitchedOn", str[10]);
    }
    @Test
    public void testGetReadingsAlerts() throws IOException, RestServiceException {
        final Device d1 = createDevice("1234987039487", true);

        final long dt = 60 * 60 * 1000l; //one hour
        final long t0 = System.currentTimeMillis() - 5 * dt;

        final Shipment s = createShipment(d1, true);
        final TrackerEvent e = createTrackerEvent(d1, new Date(t0 + 4 * dt));
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        //create alert
        createAlert(e, AlertType.Battery);
        createAlert(e, AlertType.Hot);
        createAlert(e, AlertType.Cold);

        final String data =  client.getReadings(s);
        final String[] lines = data.split("\n");
        assertEquals(2, lines.length);

        final String line = lines[1];
        int index = 0;
        for (int i = 0; i < 11; i++) {
            index = line.indexOf(',', index + 1);
        }

        final String events = line.substring(index + 1);
        assertTrue(events.startsWith("\""));
        assertTrue(events.endsWith("\""));

        final String[] split = events.substring(1, events.length() - 1).split(",");
        //check is alert types
        for (final String at : split) {
            final AlertType type = AlertType.valueOf(at);
            assertNotNull(type);
        }
    }
    /**
     * @param e
     */
    private Alert createAlert(final TrackerEvent e, final AlertType type) {
        final Alert a = new Alert();
        a.setDate(new Date());
        a.setType(type);
        a.setDevice(e.getDevice());
        a.setShipment(e.getShipment());
        a.setTrackerEventId(e.getId());
        return context.getBean(AlertDao.class).save(a);
    }
    /**
     * @param d device.
     * @param s shipment.
     * @return alert
     */
    private Alert createAlert(final Device d, final Shipment s) {
        final Alert a = new Alert();
        a.setDate(new Date());
        a.setType(AlertType.Battery);
        a.setDevice(d);
        a.setShipment(s);
        return context.getBean(AlertDao.class).save(a);
    }
    /**
     * @param d device.
     * @param s shipment.
     * @return alert
     */
    private Arrival createArrival(final Device d, final Shipment s) {
        final Arrival a = new Arrival();
        a.setDate(new Date());
        a.setDevice(d);
        a.setShipment(s);
        return context.getBean(ArrivalDao.class).save(a);
    }
    /**
     * @param device device.
     * @param shipment shipment.
     */
    private TrackerEvent createTrackerEvent(final Shipment shipment, final double t) {
        return createTrackerEvent(shipment.getDevice(), shipment, t);
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
    /**
     * @param d1
     * @param date
     * @return tracker event.
     */
    private TrackerEvent createTrackerEvent(final Device d1, final Date date) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(27);
        e.setLatitude(12.34);
        e.setLongitude(56.78);
        e.setDevice(d1);
        e.setShipment(null);
        e.setTemperature(11.);
        e.setHumidity(39);
        e.setTime(date);
        e.setCreatedOn(date);
        e.setType(TrackerEventType.INIT);
        return context.getBean(TrackerEventDao.class).save(e);
    }
}
