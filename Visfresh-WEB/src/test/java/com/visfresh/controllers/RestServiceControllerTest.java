/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceData;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.SchedulePersonHowWhen;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.init.mock.MockConfig;
import com.visfresh.mock.MockRestService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestServiceControllerTest {
    /**
     * WEB application context.
     */
    private static WebApplicationContext context;
    private static String url;

    private MockRestService service;
    private RestServiceFacade facade;
    private long lastLong;
    private static Server server;

    /**
     * Default constructor.
     */
    public RestServiceControllerTest() {
        super();
    }

    @BeforeClass
    public static void initStatics() {
        try {
            //configure servlet
            final ServletHolder holder = new ServletHolder();

            holder.setHeldClass(DispatcherServlet.class);
            holder.setName("SpringDispatcher");
            holder.setInitParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());
            holder.setInitParameter("contextConfigLocation", MockConfig.class.getPackage().getName());

            //add servlet mapping
            final ServletMapping mapping = new ServletMapping();
            mapping.setPathSpec("/vf/*");
            mapping.setServletName(holder.getName());

            //set handler to servlet.
            final ServletHandler handler = new ServletHandler();
            handler.addServlet(holder);
            handler.addServletMapping(mapping);

            //context handler
            final ServletContextHandler contextHandler = new ServletContextHandler();
            contextHandler.setContextPath( "/web" );
            contextHandler.setHandler(handler);

            //search free port
            final int port;
            final ServerSocket so = new ServerSocket(0);
            try {
                port = so.getLocalPort();
            } finally {
                so.close();
            }

            final QueuedThreadPool pool = new QueuedThreadPool();
            pool.setDaemon(true); // it allows the server to be stopped after tests finished
            server = new Server(pool);

            final ServerConnector connector=new ServerConnector(server);
            connector.setPort(port);
            server.setConnectors(new Connector[]{connector});

            server.setHandler(contextHandler);
            server.start();

            //get WEB application context.
            context = ((FrameworkServlet) holder.getServlet()).getWebApplicationContext();
            RestServiceControllerTest.url = "http://localhost:" + port + "/web/vf";
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        service = context.getBean(MockRestService.class);

        facade = new RestServiceFacade();
        facade.setServiceUrl(new URL(url));

        final String authToken = facade.login("anylogin", "anypassword");
        facade.setAuthToken(authToken);
    }

    //@RequestMapping(value = "/login", method = RequestMethod.POST)
    //public @ResponseBody String login(@RequestBody final String loginRequest) {
    @Test
    public void testLogin() throws RestServiceException, IOException {
        final String token = facade.login("aldsklksadf", "lkasdlfkj");
        assertNotNull(token);
    }
    //@RequestMapping(value = "/getToken", method = RequestMethod.GET)
    //public @ResponseBody String getAuthToken(final HttpSession session) {
    public void _testGetToken() throws IOException, RestServiceException {
        final String token = facade.getToken();
        assertNotNull(token);
    }
    //@RequestMapping(value = "/logout/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String logout(@PathVariable final String authToken) {
    @Test
    public void testLogout() throws RestServiceException, IOException {
        facade.logout(facade.getAuthToken());
    }
    //@RequestMapping(value = "/refreshToken/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String refreshToken(@PathVariable final String authToken) {
    @Test
    public void testRefreshToken() throws IOException, RestServiceException {
        final String token = facade.refreshToken();
        facade.setAuthToken(token);
        assertNotNull(token);
    }
    //@RequestMapping(value = "/getUser/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getUser(@PathVariable final String authToken,
    //        final @RequestParam String username) {
    @Test
    public void testGetUser() throws IOException, RestServiceException {
        final User user = facade.getUser("anylogin");
        assertNotNull(user);
    }
    //@RequestMapping(value = "/saveAlertProfile/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveAlertProfile(@PathVariable final String authToken,
    //        final @RequestBody String alert) {
    @Test
    public void testSaveAlertProfile() throws RestServiceException, IOException {
        final AlertProfile p = createAlertProfile();
        final Long id = facade.saveAlertProfile(p);
        assertNotNull(id);
    }
    //@RequestMapping(value = "/getAlertProfiles/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getAlertProfiles(@PathVariable final String authToken) {
    @Test
    public void testGetAlertProfiles() throws RestServiceException, IOException {
        final AlertProfile p = createAlertProfile();
        facade.saveAlertProfile(p);

        final List<AlertProfile> alertProfiles = facade.getAlertProfiles();
        assertEquals(1, alertProfiles.size());
    }
    //@RequestMapping(value = "/saveLocationProfile/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveLocationProfile(@PathVariable final String authToken,
    //        final @RequestBody String profile) {
    @Test
    public void testSaveLocationProfile() throws RestServiceException, IOException {
        final LocationProfile l = createLocationProfile();
        final Long id = facade.saveLocationProfile(l);
        assertNotNull(id);
    }
    //@RequestMapping(value = "/getLocationProfiles/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getLocationProfiles(@PathVariable final String authToken) {
    @Test
    public void testGetLocationProfiles() throws RestServiceException, IOException {
        final LocationProfile l = createLocationProfile();
        facade.saveLocationProfile(l);

        final List<LocationProfile> locationProfiles = facade.getLocationProfiles();
        assertEquals(1, locationProfiles.size());
    }
    //@RequestMapping(value = "/saveNotificationSchedule/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveNotificationSchedule(@PathVariable final String authToken,
    //        final @RequestBody String schedule) {
    @Test
    public void testSaveNotificationSchedule() throws RestServiceException, IOException {
        final NotificationSchedule s = createNotificationSchedule();
        final Long id = facade.saveNotificationSchedule(s);

        assertNotNull(id);
    }
    //@RequestMapping(value = "/getNotificationSchedules/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getNotificationSchedules(@PathVariable final String authToken) {
    @Test
    public void testGetNotificationSchedules() throws RestServiceException, IOException {
        final NotificationSchedule s = createNotificationSchedule();
        facade.saveNotificationSchedule(s);

        final List<NotificationSchedule> notificationSchedules = facade.getNotificationSchedules();
        assertEquals(1, notificationSchedules.size());
    }
    //@RequestMapping(value = "/saveShipmentTemplate/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveShipmentTemplate(@PathVariable final String authToken,
    //        final @RequestBody String tpl) {
    @Test
    public void testSaveShipmentTemplate() throws RestServiceException, IOException {
        final ShipmentTemplate t = createShipmentTemplate();

        //save depended entities.
        Long id = facade.saveAlertProfile(t.getAlertProfile());
        t.getAlertProfile().setId(id);

        id = facade.saveNotificationSchedule(t.getAlertsNotificationSchedules().get(0));
        t.getAlertsNotificationSchedules().get(0).setId(id);

        id = facade.saveNotificationSchedule(t.getArrivalNotificationSchedules().get(0));
        t.getArrivalNotificationSchedules().get(0).setId(id);

        id = facade.saveLocationProfile(t.getShippedFrom());
        t.getShippedFrom().setId(id);

        id = facade.saveLocationProfile(t.getShippedTo());
        t.getShippedTo().setId(id);

        id = facade.saveShipmentTemplate(t);
        assertNotNull(id);
    }
    //@RequestMapping(value = "/getShipmentTemplates/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipmentTemplates(@PathVariable final String authToken) {
    @Test
    public void testGetShipmentTemplates() throws RestServiceException, IOException {
        final ShipmentTemplate t = createShipmentTemplate();

        //save depended entities.
        Long id = facade.saveAlertProfile(t.getAlertProfile());
        t.getAlertProfile().setId(id);
        id = facade.saveNotificationSchedule(t.getAlertsNotificationSchedules().get(0));
        t.getAlertsNotificationSchedules().get(0).setId(id);
        id = facade.saveNotificationSchedule(t.getArrivalNotificationSchedules().get(0));
        t.getArrivalNotificationSchedules().get(0).setId(id);
        id = facade.saveLocationProfile(t.getShippedFrom());
        t.getShippedFrom().setId(id);
        id = facade.saveLocationProfile(t.getShippedTo());
        t.getShippedTo().setId(id);

        facade.saveShipmentTemplate(t);

        final List<ShipmentTemplate> shipmentTemplates = facade.getShipmentTemplates();
        assertEquals(1, shipmentTemplates.size());
    }
    //@RequestMapping(value = "/saveDevice/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveDevice(@PathVariable final String authToken,
    //        final @RequestBody String device) {
    @Test
    public void testSaveDevice() throws RestServiceException, IOException {
        final Device d = createDevice("1209898347987");
        facade.saveDevice(d);
    }
    //@RequestMapping(value = "/getDevices/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getDevices(@PathVariable final String authToken) {
    @Test
    public void testGetDevices() throws RestServiceException, IOException {
        final Device d = createDevice("1209898347987");
        facade.saveDevice(d);

        final List<Device> devices = facade.getDevices();
        assertEquals(1, devices.size());
    }
    //@RequestMapping(value = "/saveShipment/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveShipment(@PathVariable final String authToken,
    //        final @RequestBody String shipment) {
    @Test
    public void testSaveShipment() throws RestServiceException, IOException {
        final Shipment s = createShipment();

        //save depended entities.
        Long id = facade.saveAlertProfile(s.getAlertProfile());
        s.getAlertProfile().setId(id);
        id = facade.saveNotificationSchedule(s.getAlertsNotificationSchedules().get(0));
        s.getAlertsNotificationSchedules().get(0).setId(id);
        id = facade.saveNotificationSchedule(s.getArrivalNotificationSchedules().get(0));
        s.getArrivalNotificationSchedules().get(0).setId(id);
        id = facade.saveLocationProfile(s.getShippedFrom());
        s.getShippedFrom().setId(id);
        id = facade.saveLocationProfile(s.getShippedTo());
        s.getShippedTo().setId(id);
        for (final Device d : s.getDevices()) {
            facade.saveDevice(d);
        }

        id = facade.saveShipment(s, "NewTemplate.tpl", true);
        assertNotNull(id);
    }
    //@RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipments(@PathVariable final String authToken) {
    @Test
    public void testGetShipments() throws RestServiceException, IOException {
        saveShipment();

        final List<Shipment> shipments = facade.getShipments();
        assertEquals(1, shipments.size());
    }
    //@RequestMapping(value = "/getNotifications/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getNotifications(@PathVariable final String authToken,
    //        @RequestParam final Long shipment) {
    @Test
    public void testGetNotifications() throws IOException, RestServiceException {
        final Shipment s = saveShipment();
        //get server device
        final Device d = service.devices.get(s.getDevices().get(0).getId());

        //create temperature alert notification
        final TemperatureAlert tempAlert = new TemperatureAlert();
        tempAlert.setDate(new Date());
        tempAlert.setId(service.ids.incrementAndGet());
        tempAlert.setDescription("Temp Alert");
        tempAlert.setType(AlertType.HighTemperature);
        tempAlert.setTemperature(5);
        tempAlert.setMinutes(55);
        tempAlert.setName("TempAlert-1");
        tempAlert.setDevice(d);

        Notification n = new Notification();
        n.setId(service.ids.incrementAndGet());
        n.setIssue(tempAlert);
        n.setType(NotificationType.Alert);

        service.alerts.put(tempAlert.getId(), tempAlert);
        service.notifications.put(n.getId(), n);

        //create ordinar alert
        final Alert batteryAlert = new Alert();
        batteryAlert.setDate(new Date());
        batteryAlert.setId(service.ids.incrementAndGet());
        batteryAlert.setDescription("Battery Low alert");
        batteryAlert.setType(AlertType.BatteryLow);
        batteryAlert.setName("Battery-1");
        batteryAlert.setDevice(d);

        n = new Notification();
        n.setId(service.ids.incrementAndGet());
        n.setIssue(batteryAlert);
        n.setType(NotificationType.Alert);

        service.alerts.put(batteryAlert.getId(), batteryAlert);
        service.notifications.put(n.getId(), n);

        //arrival notification
        final Arrival a = new Arrival();
        a.setDevice(d);
        a.setId(service.ids.incrementAndGet());
        a.setDate(new Date());
        a.setNumberOfMettersOfArrival(1500);


        n = new Notification();
        n.setId(service.ids.incrementAndGet());
        n.setIssue(a);
        n.setType(NotificationType.Arrival);

        service.arrivals.put(a.getId(), a);
        service.notifications.put(n.getId(), n);

        //get notifications
        final List<Notification> notifications = facade.getNotifications(s);
        assertEquals(3, notifications.size());
    }
    //@RequestMapping(value = "/markNotificationsAsRead/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String markNotificationsAsRead(@PathVariable final String authToken,
    //        @RequestBody final String notificationIds) {
    @Test
    public void testMarkNotificationsAsRead() throws IOException, RestServiceException {
        final Shipment s = saveShipment();
        //get server device
        final Device d = service.devices.get(s.getDevices().get(0).getId());

        //create temperature alert notification
        final TemperatureAlert tempAlert = new TemperatureAlert();
        tempAlert.setDate(new Date());
        tempAlert.setId(service.ids.incrementAndGet());
        tempAlert.setDescription("Temp Alert");
        tempAlert.setType(AlertType.HighTemperature);
        tempAlert.setTemperature(5);
        tempAlert.setMinutes(55);
        tempAlert.setName("TempAlert-1");
        tempAlert.setDevice(d);

        final Notification n = new Notification();
        n.setId(service.ids.incrementAndGet());
        n.setIssue(tempAlert);
        n.setType(NotificationType.Alert);

        service.alerts.put(tempAlert.getId(), tempAlert);
        service.notifications.put(n.getId(), n);

        //create ordinar alert
        final Alert batteryAlert = new Alert();
        batteryAlert.setDate(new Date());
        batteryAlert.setId(service.ids.incrementAndGet());
        batteryAlert.setDescription("Battery Low alert");
        batteryAlert.setType(AlertType.BatteryLow);
        batteryAlert.setName("Battery-1");
        batteryAlert.setDevice(d);

        final Notification n1 = new Notification();
        n1.setId(service.ids.incrementAndGet());
        n1.setIssue(batteryAlert);
        n1.setType(NotificationType.Alert);

        service.alerts.put(batteryAlert.getId(), batteryAlert);
        service.notifications.put(n1.getId(), n1);

        //arrival notification
        final Arrival a = new Arrival();
        a.setDevice(d);
        a.setId(service.ids.incrementAndGet());
        a.setDate(new Date());
        a.setNumberOfMettersOfArrival(1500);


        final Notification n2 = new Notification();
        n2.setId(service.ids.incrementAndGet());
        n2.setIssue(a);
        n2.setType(NotificationType.Arrival);

        service.arrivals.put(a.getId(), a);
        service.notifications.put(n2.getId(), n2);

        //get notifications
        final List<Notification> toReaden = new LinkedList<Notification>();
        toReaden.add(n1);
        toReaden.add(n2);

        facade.markNotificationsAsRead(toReaden);

        final List<Notification> notifications = facade.getNotifications(s);
        assertEquals(1, notifications.size());

    }
    //@RequestMapping(value = "/getShipmentData/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipmentData(@PathVariable final String authToken,
    //        @RequestParam final String fromDate,
    //        @RequestParam final String toDate,
    //        @RequestParam final String onlyWithAlerts
    @Test
    public void testGetShipmentData() throws RestServiceException, IOException {
        final Shipment s = saveShipment();
        //get server device
        final Device d = service.devices.get(s.getDevices().get(0).getId());

        //add tracker event.
        final TrackerEvent te = createEvent(TrackerEventType.AUT);
        te.setId(service.ids.incrementAndGet());

        final List<TrackerEvent> tes = new LinkedList<TrackerEvent>();
        tes.add(te);
        service.trackerEvents.put(d.getId(), tes);

        //add alert
        final Alert a = createAlert(d, AlertType.BatteryLow);
        a.setId(service.ids.incrementAndGet());
        service.alerts.put(a.getId(), a);

        final List<ShipmentData> data = facade.getShipmentData(new Date(System.currentTimeMillis() - 100000000L),
                new Date(System.currentTimeMillis() + 10000000l), false);
        assertEquals(1, data.size());

        final ShipmentData sd = data.get(0);
        assertEquals(1, sd.getDeviceData().size());

        final DeviceData deviceData = sd.getDeviceData().get(0);
        assertEquals(1, deviceData.getAlerts().size());
        assertEquals(1, deviceData.getEvents().size());
    }
    @Test
    public void testSendCommandToDevice() throws RestServiceException, IOException {
        final Device device = createDevice("089723409857032498");
        facade.saveDevice(device);

        facade.sendCommandToDevice(device, "shutdown");
    }

    /**
     * @return tracker event.
     */
    private TrackerEvent createEvent(final TrackerEventType type) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(1234);
        e.setTemperature(56);
        e.setTime(new Date());
        e.setType(type);
        return e;
    }
    /**
     * @return alert.
     */
    private Alert createAlert(final Device device, final AlertType type) {
        final Alert alert = new Alert();
        alert.setDate(new Date(System.currentTimeMillis() - 100000000l));
        alert.setDescription("Alert description");
        alert.setName("Alert-" + type);
        alert.setDevice(device);
        alert.setType(type);
        return alert;
    }
    /**
     * @param imei IMEI.
     * @return
     */
    private Device createDevice(final String imei) {
        final Device t = new Device();
        t.setDescription("Device description");
        t.setImei(imei);
        t.setId(imei);
        t.setName("Device Name");
        t.setSn("1");
        return t;
    }
    /**
     * @return
     */
    private NotificationSchedule createNotificationSchedule() {
        final NotificationSchedule s = new NotificationSchedule();
        s.setDescription("JUnit schedule");
        s.setName("Sched");
        s.getSchedules().add(createSchedulePersonHowWhen());
        s.getSchedules().add(createSchedulePersonHowWhen());
        return s;
    }
    /**
     * @return any alert profile.
     */
    private AlertProfile createAlertProfile() {
        final AlertProfile p = new AlertProfile();
        p.setCriticalHighTemperature(5);
        p.setCriticalLowTemperature(-15);
        p.setDescription("Any description");
        p.setHighTemperature(1);
        p.setHighTemperatureForMoreThen(55);
        p.setLowTemperature(-10);
        p.setLowTemperatureForMoreThen(55);
        p.setName("AnyAlert");
        p.setWatchBatteryLow(true);
        p.setWatchEnterBrightEnvironment(true);
        p.setWatchEnterDarkEnvironment(true);
        p.setWatchShock(true);
        return p;
    }
    /**
     * @return any location profile.
     */
    private LocationProfile createLocationProfile() {
        final LocationProfile p = new LocationProfile();

        p.setCompany("Sun Microsystems");
        p.setInterim(true);
        p.setName("Loc-" + (++lastLong));
        p.setNotes("Any notes");
        p.setAddress("Odessa, Deribasovskaya 1, apt.1");
        p.setRadius(1000);
        p.setStart(true);
        p.setStop(true);
        p.getLocation().setLatitude(100.500);
        p.getLocation().setLongitude(100.501);
        return p;
    }
    private Shipment createShipment() {
        final Shipment s = new Shipment();
        s.setAlertProfile(createAlertProfile());
        s.getAlertsNotificationSchedules().add(createNotificationSchedule());
        s.setAlertSuppressionDuringCoolDown(55);
        s.setArrivalNotificationWithIn(111);
        s.getArrivalNotificationSchedules().add(createNotificationSchedule());
        s.setexcludeNotificationsIfNoAlertsFired(true);
        s.setName("Shipment-" + (++lastLong));
        s.setShipmentDescription("Any Description");
        s.setShippedFrom(createLocationProfile());
        s.setShippedTo(createLocationProfile());
        s.setShutdownDeviceTimeOut(155);
        s.getDevices().add(createDevice("234908720394857"));
        s.getDevices().add(createDevice("329847983724987"));
        s.setPalletId("palettid");
        s.setShipmentDescriptionDate(new Date(System.currentTimeMillis() - 1000000000l));
        s.setCustomFields("customFields");
        return s;
    }
    /**
     * @return any schedule/person/how/when
     */
    private SchedulePersonHowWhen createSchedulePersonHowWhen() {
        final SchedulePersonHowWhen s = new SchedulePersonHowWhen();

        s.setCompany("Sun");
        s.setEmailNotification("asuvorov@sun.com");
        s.setFirstName("Alexander");
        s.setToTime(17);
        s.setFromTime(1);
        s.setLastName("Suvorov");
        s.setPosition("Generalisimus");
        s.setPushToMobileApp(true);
        s.setSmsNotification("1111111117");
        s.getWeekDays()[0] = true;
        s.getWeekDays()[3] = true;

        return s;
    }
    private ShipmentTemplate createShipmentTemplate() {
        final ShipmentTemplate t = new ShipmentTemplate();
        t.setAddDateShipped(true);
        t.setAlertProfile(createAlertProfile());
        t.getAlertsNotificationSchedules().add(createNotificationSchedule());
        t.setAlertSuppressionDuringCoolDown(55);
        t.setArrivalNotificationWithIn(11);
        t.getArrivalNotificationSchedules().add(createNotificationSchedule());
        t.setexcludeNotificationsIfNoAlertsFired(true);
        t.setName("JUnit-tpl");
        t.setShipmentDescription("Any Description");
        t.setShippedFrom(createLocationProfile());
        t.setShippedTo(createLocationProfile());
        t.setShutdownDeviceTimeOut(155);
        t.setUseCurrentTimeForDateShipped(true);
        t.setDetectLocationForShippedFrom(true);
        return t;
    }
    /**
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    private Shipment saveShipment() throws RestServiceException, IOException {
        final Shipment s = createShipment();
        //save depended entities.
        Long id = facade.saveAlertProfile(s.getAlertProfile());
        s.getAlertProfile().setId(id);
        id = facade.saveNotificationSchedule(s.getAlertsNotificationSchedules().get(0));
        s.getAlertsNotificationSchedules().get(0).setId(id);
        id = facade.saveNotificationSchedule(s.getArrivalNotificationSchedules().get(0));
        s.getArrivalNotificationSchedules().get(0).setId(id);
        id = facade.saveLocationProfile(s.getShippedFrom());
        s.getShippedFrom().setId(id);
        id = facade.saveLocationProfile(s.getShippedTo());
        s.getShippedTo().setId(id);

        for (final Device d : s.getDevices()) {
            facade.saveDevice(d);
        }

        id = facade.saveShipment(s, "NewTemplate.tpl", true);
        s.setId(id);
        return s;
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        if (service != null) {
            service.clear();
        }
    }
    @AfterClass
    public static void destroyStatics() {
        if (server != null) {
            try {
                server.stop();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
