/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;

import com.visfresh.constants.DeviceConstants;
import com.visfresh.controllers.restclient.DeviceRestClient;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.SystemMessageDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.lists.DeviceDto;
import com.visfresh.mock.MockEmailService;
import com.visfresh.mock.MockShipmentShutdownService;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.LocalizationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceControllerTest extends AbstractRestServiceTest {
    private DeviceDao dao;
    private DeviceRestClient client = new DeviceRestClient(UTC);
    private long id;

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
    @After
    public void tearDown() {
        context.getBean(MockEmailService.class).getMessages().clear();
    }
    //@RequestMapping(value = "/saveDevice/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveDevice(@PathVariable final String authToken,
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

        assertNotNull(client.getDevice(ap.getId()));
    }
//    @Test
//    public void testDeleteDevice() throws RestServiceException, IOException {
//        final Device p = createDevice("0239487043987", true);
//        client.deleteDevice(p);
//        assertNull(dao.findOne(p.getId()));
//    }
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

        //create autostart shipment template
        ShipmentTemplate tpl = new ShipmentTemplate();
        tpl.setCompany(getCompany());
        tpl.setName("TPL1");
        tpl = getContext().getBean(ShipmentTemplateDao.class).save(tpl);

        AutoStartShipment aut = new AutoStartShipment();
        aut.setCompany(getCompany());
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
        final DeviceDto item = client.getDevices(null, true, 3, 1).get(0);
        assertEquals(d1.getDescription(), item.getDescription());
        assertEquals(d1.getId(), item.getImei());
        assertEquals(d1.getImei(), item.getImei());
        assertEquals(d1.getName(), item.getName());
        assertEquals(d1.getSn(), item.getSn());
        assertEquals(tpl.getName(), item.getAutostartTemplateName());
        assertEquals(aut.getId(), item.getAutostartTemplateId());

        //last reading data
        assertEquals(e1.getBattery(), item.getLastReadingBattery().intValue());
        assertEquals(e1.getLatitude(), item.getLastReadingLat(), 0.0001);
        assertEquals(e1.getLongitude(), item.getLastReadingLong(), 0.0001);
        assertEquals(LocalizationUtils.getTemperatureString(23.46, TemperatureUnits.Celsius),
                item.getLastReadingTemperature());
        assertNotNull(item.getLastReadingTimeISO());
        assertNotNull(item.getLastReadingTime());
        assertEquals(e1.getShipment().getId(), item.getLastShipmentId());
    }
    @Test
    public void testGetDevicesSortByImei() throws RestServiceException, IOException {
        final Device d1 = createDevice("3333333333333", true);
        final Device d2 = createDevice("2222222222222", true);
        final Device d3 = createDevice("1111111111111", true);

        List<DeviceDto> dto;

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
    public void testSortByShipmentNumber() throws RestServiceException, IOException {
        final Device d1 = createDevice("3333333333333", true);
        final Device d2 = createDevice("2222222222222", true);
        final Device d3 = createDevice("4444444444444", true);

        createShipment(d1, true);
        createShipment(d2, true);
        //d3 without shipment, and therefore without shipment number

        List<DeviceDto> dto;

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

        List<DeviceDto> dto;

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
    public void testMoveDevice() throws IOException, RestServiceException, BeansException, AuthenticationException {
        final Company c1 = createCompany("C1");
        final Company c2 = createCompany("C2");

        //create device
        final Device d = createDevice("0239870932487", false);
        d.setCompany(c1);
        dao.save(d);

        //create device group for device
        final DeviceGroup dg = new DeviceGroup();
        dg.setCompany(c1);
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
        u.setCompany(getCompany());
        u.setEmail("admin@smartrace.com.au");
        u.setFirstName("JUnit");
        u.setLastName("Admin");
        context.getBean(AuthService.class).saveUser(u, "", false);

        //relogin by smarttrace admin
        final String token = context.getBean(AuthService.class).login(u.getEmail(),"").getToken();
        client.setAuthToken(token);

        //do move
        final String newImei = client.moveDevice(d, c2);

        //check device really moved
        assertEquals(c2.getId(), dao.findByImei(d.getImei()).getCompany().getId());

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

        client.initDeviceColors(d1.getCompany());

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
    public void testReadingsToCsv() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final DateFormat fmt= new SimpleDateFormat("yyyy-MM-dd'T'mm");

        //create event list
        final List<ShortTrackerEvent> events = new LinkedList<>();
        final ShortTrackerEvent e = createShortTrackerEvent(10.10, 11.11, 7l);
        events.add(e);
        events.add(createShortTrackerEvent(9.10, 10.11, null));
        //create event without lat/lon
        final ShortTrackerEvent e1 = createShortTrackerEvent(10.10, 11.11, 7l);
        e1.setLatitude(null);
        e1.setLongitude(null);
        events.add(e1);

        DeviceController.readingsToCsv(events, out, fmt);

        final String[] text = new String(out.toByteArray()).split("\r?\n");
        assertEquals(4, text.length);

        //check first reading
        final String[] str = text[1].split(",");

        assertEquals(10, str.length);
        //| id          | bigint(20)   | NO   | PRI | NULL    | auto_increment |
        assertEquals(Long.toString(e.getId()), str[0]);
        //| type        | varchar(20)  | NO   |     | NULL    |                |
        assertEquals(e.getType().name(), str[1]);
        //| time        | timestamp    | YES  |     | NULL    |                |
        assertEquals(fmt.format(e.getTime()), str[2]);
        //| battery     | int(11)      | NO   |     | NULL    |                |
        assertEquals(Integer.toString(e.getBattery()), str[3]);
        //| temperature | double       | NO   |     | NULL    |                |
        assertEquals(Double.toString(e.getTemperature()), str[4]);
        //| latitude    | double       | YES  |     | NULL    |                |
        assertEquals(Double.toString(e.getLatitude()), str[5]);
        //| longitude   | double       | YES  |     | NULL    |                |
        assertEquals(Double.toString(e.getLongitude()), str[6]);
        //| device      | varchar(127) | NO   | MUL | NULL    |                |
        assertEquals(e.getDeviceImei(), str[7]);
        //| shipment    | bigint(20)   | YES  | MUL | NULL    |                |
        assertEquals(e.getShipmentId() == null ? null : e.getShipmentId().toString(), str[8]);
        //| createdon   | timestamp    | YES  |     | NULL    |                |
        assertEquals(fmt.format(e.getCreatedOn()), str[9]);
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
        createTrackerEvent(d1, new Date(t0 + 4 * dt));

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
     * @param name company name.
     * @return company.
     */
    private Company createCompany(final String name) {
        final Company c = new Company();
        c.setName(name);
        return context.getBean(CompanyDao.class).save(c);
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
        e.setTime(date);
        e.setCreatedOn(date);
        e.setType(TrackerEventType.INIT);
        return context.getBean(TrackerEventDao.class).save(e);
    }

    /**
     * @param lat latitude.
     * @param lon longitude.
     * @param shipmentId shipment ID.
     * @return short tracker event.
     */
    private ShortTrackerEvent createShortTrackerEvent(final double lat, final double lon, final Long shipmentId) {
        final ShortTrackerEvent e = new ShortTrackerEvent();
        e.setId(id++);
        e.setBattery(3500);
        e.setLatitude(lat);
        e.setLongitude(lon);
        e.setDeviceImei("923847082374087");
        e.setShipmentId(shipmentId);
        e.setTemperature(11);
        e.setTime(new Date());
        e.setType(TrackerEventType.INIT);
        e.setCreatedOn(new Date());
        return e;
    }
}
