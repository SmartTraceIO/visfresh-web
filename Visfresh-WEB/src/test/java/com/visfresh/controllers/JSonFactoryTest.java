/**
 *
 */
package com.visfresh.controllers;

import java.util.Date;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.DeviceData;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Role;
import com.visfresh.entities.SchedulePersonHowWhen;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.JSonSerializer;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JSonFactoryTest extends TestCase {
    /**
     * Factory to test.
     */
    private JSonSerializer factory;
    private MockReferenceResolver resolver;
    private long lastLong;
    private Gson gson;

    /**
     * default constructor.
     */
    public JSonFactoryTest() {
        super();
    }
    /**
     * @param name test case name.
     */
    public JSonFactoryTest(final String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        final GsonBuilder b = new GsonBuilder();
        b.setPrettyPrinting();
        this.gson = b.create();

        resolver = new MockReferenceResolver();
        factory = new JSonSerializer();
        factory.setReferenceResolver(resolver);
    }

    public void testAlertProfile() {
        final double criticalHighTemperature = 5.77;
        final double criticalLowTemperature = -15.88;
        final String description = "Any description";
        final double highTemperature = 1.22;
        final int highTemperatureForMoreThen = 55; // min
        final Long id = 7L;
        final double lowTemperature = -10.33;
        final int lowTemperatureForMoreThen = 55; //min
        final String name = "AnyName";
        final boolean watchBatteryLow = true;
        final boolean watchEnterBrightEnvironment = true;
        final boolean watchEnterDarkEnvironment = false;
        final boolean watchShock = false;
        final int criticalHighTemperatureForMoreThen = 91;
        final int criticalLowTemperatureForMoreThen = 71;

        AlertProfile p = new AlertProfile();
        p.setCriticalHighTemperature(criticalHighTemperature);
        p.setCriticalLowTemperature(criticalLowTemperature);
        p.setDescription(description);
        p.setHighTemperature(highTemperature);
        p.setHighTemperatureForMoreThen(highTemperatureForMoreThen);
        p.setId(id);
        p.setLowTemperature(lowTemperature);
        p.setLowTemperatureForMoreThen(lowTemperatureForMoreThen);
        p.setName(name);
        p.setWatchBatteryLow(watchBatteryLow);
        p.setWatchEnterBrightEnvironment(watchEnterBrightEnvironment);
        p.setWatchEnterDarkEnvironment(watchEnterDarkEnvironment);
        p.setWatchShock(watchShock);
        p.setCriticalHighTemperatureForMoreThen(criticalHighTemperatureForMoreThen);
        p.setCriticalLowTemperatureForMoreThen(criticalLowTemperatureForMoreThen);

        final JsonObject json = factory.toJson(p);
        p = factory.parseAlertProfile(json);

        assertEquals(criticalHighTemperature, p.getCriticalHighTemperature());
        assertEquals(criticalLowTemperature, p.getCriticalLowTemperature());
        assertEquals(description, p.getDescription());
        assertEquals(highTemperature, p.getHighTemperature());
        assertEquals(highTemperatureForMoreThen, p.getHighTemperatureForMoreThen());
        assertEquals(id, p.getId());
        assertEquals(lowTemperature, p.getLowTemperature());
        assertEquals(lowTemperatureForMoreThen, p.getLowTemperatureForMoreThen());
        assertEquals(name, p.getName());
        assertEquals(watchBatteryLow, p.isWatchBatteryLow());
        assertEquals(watchEnterBrightEnvironment, p.isWatchEnterBrightEnvironment());
        assertEquals(watchEnterDarkEnvironment, p.isWatchEnterDarkEnvironment());
        assertEquals(watchShock, p.isWatchShock());
        assertEquals(criticalHighTemperatureForMoreThen, p.getCriticalHighTemperatureForMoreThen());
        assertEquals(criticalLowTemperatureForMoreThen, p.getCriticalLowTemperatureForMoreThen());
    }
    public void testSchedulePersonHowWhen() {
        SchedulePersonHowWhen s = new SchedulePersonHowWhen();

        final String company = "Sun";
        final String emailNotification = "anybody@sun.com";
        final String firstName = "Alexander";
        final int forMinute = 17;
        final int fromMinute = 1;
        final Long id = 77l;
        final String lastName = "Suvorov";
        final String position = "General";
        final boolean pushToMobileApp = true;
        final String smsNotification = "1111111117";

        s.setCompany(company);
        s.setEmailNotification(emailNotification);
        s.setFirstName(firstName);
        s.setToTime(forMinute);
        s.setFromTime(fromMinute);
        s.setId(id);
        s.setLastName(lastName);
        s.setPosition(position);
        s.setPushToMobileApp(pushToMobileApp);
        s.setSmsNotification(smsNotification);
        s.getWeekDays()[0] = true;
        s.getWeekDays()[3] = true;

        final JsonObject obj = factory.toJson(s);
        s = factory.parseSchedulePersonHowWhen(obj);

        assertEquals(company, s.getCompany());
        assertEquals(emailNotification, s.getEmailNotification());
        assertEquals(firstName, s.getFirstName());
        assertEquals(forMinute, s.getToTime());
        assertEquals(fromMinute, s.getFromTime());
        assertEquals(id, s.getId());
        assertEquals(lastName, s.getLastName());
        assertEquals(position, s.getPosition());
        assertEquals(pushToMobileApp, s.isPushToMobileApp());
        assertEquals(smsNotification, s.getSmsNotification());
        assertTrue(s.getWeekDays()[0]);
        assertFalse(s.getWeekDays()[1]);
        assertFalse(s.getWeekDays()[2]);
        assertTrue(s.getWeekDays()[3]);
        assertFalse(s.getWeekDays()[4]);
        assertFalse(s.getWeekDays()[5]);
        assertFalse(s.getWeekDays()[6]);
    }
    public void testNotificationSchedule() {
        final String description = "JUnit schedule";
        final Long id = 77l;
        final String name = "Sched";

        NotificationSchedule s = new NotificationSchedule();

        s.setDescription(description);
        s.setId(id);
        s.setName(name);
        s.getSchedules().add(createSchedulePersonHowWhen());
        s.getSchedules().add(createSchedulePersonHowWhen());

        final JsonObject obj = factory.toJson(s);
        s = factory.parseNotificationSchedule(obj);

        assertEquals(description, s.getDescription());
        assertEquals(id, s.getId());
        assertEquals(name, s.getName());
        assertEquals(2, s.getSchedules().size());
    }
    public void testLocationProfile() {
        final String company = "Sun Microsystems";
        final Long id = 77l;
        final boolean interim = true;
        final String name = "JUnit-Location";
        final String notes = "Any notes";
        final String address = "Odessa, Deribasovskaya 1, apt.1";
        final int radius = 1000;
        final boolean start = true;
        final boolean stop = true;
        final double x = 100.500;
        final double y = 100.501;

        LocationProfile p = new LocationProfile();

        p.setCompanyDescription(company);
        p.setId(id);
        p.setInterim(interim);
        p.setName(name);
        p.setNotes(notes);
        p.setAddress(address);
        p.setRadius(radius);
        p.setStart(start);
        p.setStop(stop);
        p.getLocation().setLatitude(x);
        p.getLocation().setLongitude(y);

        final JsonObject obj = factory.toJson(p);
        p = factory.parseLocationProfile(obj);

        assertEquals(company, p.getCompanyDescription());
        assertEquals(id, p.getId());
        assertEquals(interim, p.isInterim());
        assertEquals(name, p.getName());
        assertEquals(notes, p.getNotes());
        assertEquals(address, p.getAddress());
        assertEquals(radius, p.getRadius());
        assertEquals(start, p.isStart());
        assertEquals(stop, p.isStop());
        assertEquals(x, p.getLocation().getLatitude());
        assertEquals(y, p.getLocation().getLongitude());
    }
    public void testShipmentTemplate() {
        final boolean addDateShipped = true;
        final AlertProfile alertProfile = createAlertProfile();
        final NotificationSchedule alertsNotificationSchedule = createNotificationSchedule();
        final int alertSuppressionDuringCoolDown = 55;
        final int arrivalNotification = 111;
        final NotificationSchedule arrivalNotificationSchedule = createNotificationSchedule();
        final boolean excludeNotificationsIfNoAlertsFired = true;
        final Long id = 77l;
        final String name = "JUnit-tpl";
        final String shipmentDescription = "Any Description";
        final LocationProfile shippedFrom = createLocationProfile();
        final LocationProfile shippedTo = createLocationProfile();
        final int shutdownDeviceTimeOut = 155;
        final boolean useCurrentTimeForDateShipped = true;
        final boolean useLocationNearestToDevice = true;

        ShipmentTemplate t = new ShipmentTemplate();
        t.setAddDateShipped(addDateShipped);
        t.setAlertProfile(alertProfile);
        t.getAlertsNotificationSchedules().add(alertsNotificationSchedule);
        t.setAlertSuppressionDuringCoolDown(alertSuppressionDuringCoolDown);
        t.setArrivalNotificationWithIn(arrivalNotification);
        t.getArrivalNotificationSchedules().add(arrivalNotificationSchedule);
        t.setexcludeNotificationsIfNoAlertsFired(excludeNotificationsIfNoAlertsFired);
        t.setId(id);
        t.setName(name);
        t.setShipmentDescription(shipmentDescription);
        t.setShippedFrom(shippedFrom);
        t.setShippedTo(shippedTo);
        t.setShutdownDeviceTimeOut(shutdownDeviceTimeOut);
        t.setUseCurrentTimeForDateShipped(useCurrentTimeForDateShipped);
        t.setDetectLocationForShippedFrom(useLocationNearestToDevice);

        final JsonObject obj = factory.toJson(t);

        t = factory.parseShipmentTemplate(obj);

        assertEquals(addDateShipped, t.isAddDateShipped());
        assertNotNull(t.getAlertProfile());
        assertNotNull(t.getAlertsNotificationSchedules());
        assertEquals(alertSuppressionDuringCoolDown, t.getAlertSuppressionDuringCoolDown());
        assertEquals(arrivalNotification, t.getArrivalNotificationWithIn());
        assertNotNull(t.getArrivalNotificationSchedules());
        assertEquals(excludeNotificationsIfNoAlertsFired, t.isexcludeNotificationsIfNoAlertsFired());
        assertEquals(id, t.getId());
        assertEquals(name, t.getName());
        assertEquals(shipmentDescription, t.getShipmentDescription());
        assertNotNull(t.getShippedFrom());
        assertNotNull(t.getShippedTo());
        assertEquals(shutdownDeviceTimeOut, t.getShutdownDeviceTimeOut());
        assertEquals(useCurrentTimeForDateShipped, t.isUseCurrentTimeForDateShipped());
        assertEquals(useLocationNearestToDevice, t.isDetectLocationForShippedFrom());
    }
    public void testDevice() {
        final String description = "Device description";
        final String imei = "018923475076";
        final String id = "018923475076.123";
        final String name = "Device Name";
        final String sn = "938479";

        Device t = new Device();
        t.setDescription(description);
        t.setId(id);
        t.setImei(imei);
        t.setName(name);
        t.setSn(sn);

        final JsonObject json = factory.toJson(t);
        t= factory.parseDevice(json);

        assertEquals(description, t.getDescription());
        assertEquals(id, t.getId());
        assertEquals(imei, t.getImei());
        assertEquals(name, t.getName());
        assertEquals(sn, t.getSn());
    }
    public void testShipment() {
        final AlertProfile alertProfile = createAlertProfile();
        final NotificationSchedule alertsNotificationSchedule = createNotificationSchedule();
        final int alertSuppressionDuringCoolDown = 55;
        final int arrivalNotification = 111;
        final NotificationSchedule arrivalNotificationSchedule = createNotificationSchedule();
        final boolean excludeNotificationsIfNoAlertsFired = true;
        final Long id = 77l;
        final String name = "JUnit-tpl";
        final String shipmentDescription = "Any Description";
        final LocationProfile shippedFrom = createLocationProfile();
        final LocationProfile shippedTo = createLocationProfile();
        final int shutdownDeviceTimeOut = 155;
        final Device t1 = createDevice("234908720394857");
        final Device t2 = createDevice("329847983724987");
        final String palletId = "palettid";
        final Date shipmentDescriptionDate = new Date(System.currentTimeMillis() - 1000000000l);
        final String customFields = "customFields";
        final ShipmentStatus status = ShipmentStatus.Complete;

        Shipment s = new Shipment();
        s.setAlertProfile(alertProfile);
        s.getAlertsNotificationSchedules().add(alertsNotificationSchedule);
        s.setAlertSuppressionDuringCoolDown(alertSuppressionDuringCoolDown);
        s.setArrivalNotificationWithIn(arrivalNotification);
        s.getArrivalNotificationSchedules().add(arrivalNotificationSchedule);
        s.setexcludeNotificationsIfNoAlertsFired(excludeNotificationsIfNoAlertsFired);
        s.setId(id);
        s.setName(name);
        s.setShipmentDescription(shipmentDescription);
        s.setShippedFrom(shippedFrom);
        s.setShippedTo(shippedTo);
        s.setShutdownDeviceTimeOut(shutdownDeviceTimeOut);
        s.getDevices().add(t1);
        s.getDevices().add(t2);
        s.setPalletId(palletId);
        s.setShipmentDescriptionDate(shipmentDescriptionDate);
        s.setCustomFields(customFields);
        s.setStatus(status);

        final JsonObject obj = factory.toJson(s);
        s = factory.parseShipment(obj);

        assertNotNull(s.getAlertProfile());
        assertNotNull(s.getAlertsNotificationSchedules());
        assertEquals(alertSuppressionDuringCoolDown, s.getAlertSuppressionDuringCoolDown());
        assertEquals(arrivalNotification, s.getArrivalNotificationWithIn());
        assertNotNull(s.getArrivalNotificationSchedules());
        assertEquals(excludeNotificationsIfNoAlertsFired, s.isexcludeNotificationsIfNoAlertsFired());
        assertEquals(id, s.getId());
        assertEquals(name, s.getName());
        assertEquals(shipmentDescription, s.getShipmentDescription());
        assertNotNull(s.getShippedFrom());
        assertNotNull(s.getShippedTo());
        assertEquals(shutdownDeviceTimeOut, s.getShutdownDeviceTimeOut());
        assertEquals(2, s.getDevices().size());
        assertEquals(palletId, s.getPalletId());
        assertEquals(shipmentDescriptionDate, s.getShipmentDescriptionDate());
        assertEquals(customFields, s.getCustomFields());
        assertEquals(status, s.getStatus());
    }
    public void testSaveShipmentResponse() {
        final Long shipmentId = 1L;
        final Long templateId = 2l;

        SaveShipmentResponse r = new SaveShipmentResponse();
        r.setShipmentId(shipmentId);
        r.setTemplateId(templateId);

        final JsonObject obj = factory.toJson(r);
        r = factory.parseSaveShipmentResponse(obj);

        assertEquals(shipmentId, r.getShipmentId());
        assertEquals(templateId, r.getTemplateId());
    }
    public void testSaveShipmentRequest() {
        final boolean saveAsNewTemplate = true;
        final Shipment shipment = createShipment();
        final String templateName = "JUnit Shipment Template";

        SaveShipmentRequest req = new SaveShipmentRequest();
        req.setSaveAsNewTemplate(saveAsNewTemplate);
        req.setShipment(shipment);
        req.setTemplateName(templateName);

        final JsonObject obj = factory.toJson(req);
        req = factory.parseSaveShipmentRequest(obj);

        assertEquals(saveAsNewTemplate, req.isSaveAsNewTemplate());
        assertEquals(templateName, req.getTemplateName());
        assertNotNull(req.getShipment());
    }
    public void testEnterDarkEnvironmentAlertNotification() {
        final Date alertDate = new Date(System.currentTimeMillis() - 100000000l);
        final String alertDescription = "Alert description";
        final Long alertId = 77l;
        final String alertName = "Any name";
        final Device device = createDevice("20394870987324");
        final AlertType alertType = AlertType.EnterDarkEnvironment;

        Alert alert = new Alert();
        alert.setDate(alertDate);
        alert.setDescription(alertDescription);
        alert.setId(alertId);
        alert.setName(alertName);
        alert.setDevice(device);
        alert.setType(alertType);

        final Long notificationId = 78L;
        final NotificationType notificationType = NotificationType.Alert;

        Notification n= new Notification();
        n.setId(notificationId);
        n.setIssue(alert);
        n.setType(notificationType);

        final JsonObject json = factory.toJson(n);
        n = factory.parseNotification(json);

        //check notification
        assertEquals(notificationType, n.getType());
        assertEquals(notificationId, n.getId());

        //check issue
        alert = (Alert) n.getIssue();

        assertEquals(alertDate, alert.getDate());
        assertEquals(alertDescription, alert.getDescription());
        assertEquals(alertId, alert.getId());
        assertEquals(alertName, alert.getName());
        assertNotNull(alert.getDevice());
        assertEquals(alertType, alert.getType());
    }

    public void testArrivalNotification() {
        final Date alertDate = new Date(System.currentTimeMillis() - 100000000l);
        final String alertDescription = "Alert description";
        final Long alertId = 77l;
        final String alertName = "Any name";
        final Device device = createDevice("20394870987324");
        final AlertType alertType = AlertType.HighTemperature;
        final double temperature = 10.12;
        final int minutes = 75;

        TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(alertDate);
        alert.setDescription(alertDescription);
        alert.setId(alertId);
        alert.setName(alertName);
        alert.setDevice(device);
        alert.setType(alertType);
        alert.setTemperature(temperature);
        alert.setMinutes(minutes);

        final Long notificationId = 78L;
        final NotificationType notificationType = NotificationType.Alert;

        Notification n= new Notification();
        n.setId(notificationId);
        n.setIssue(alert);
        n.setType(notificationType);

        final JsonObject json = factory.toJson(n);
        n = factory.parseNotification(json);

        //check notification
        assertEquals(notificationType, n.getType());
        assertEquals(notificationId, n.getId());

        //check issue
        alert = (TemperatureAlert) n.getIssue();

        assertEquals(alertDate, alert.getDate());
        assertEquals(alertDescription, alert.getDescription());
        assertEquals(alertId, alert.getId());
        assertEquals(alertName, alert.getName());
        assertNotNull(alert.getDevice());
        assertEquals(alertType, alert.getType());
        assertEquals(temperature, alert.getTemperature());
        assertEquals(minutes, alert.getMinutes());
    }
    public void testTrackerEvent() {
        final int battery = 12;
        final Long id = 7l;
        final double temperature = 77.77;
        final Date time = new Date(System.currentTimeMillis() - 1000000000L);
        final TrackerEventType type = TrackerEventType.RSP;

        TrackerEvent e = new TrackerEvent();
        e.setBattery(battery);
        e.setId(id);
        e.setTemperature(temperature);
        e.setTime(time);
        e.setType(type);

        final JsonObject obj= factory.toJson(e);
        e = factory.parseTrackerEvent(obj);

        assertEquals(battery, e.getBattery());
        assertEquals(id, e.getId());
        assertEquals(temperature, e.getTemperature());
        assertEquals(time, e.getTime());
        assertEquals(type, e.getType());
    }
    public void testDeviceData() {
        DeviceData d = new DeviceData();

        final Device device = createDevice("93218709879");
        d.setDevice(device);
        d.getAlerts().add(createAlert(device, AlertType.BatteryLow));
        d.getAlerts().add(createAlert(device, AlertType.EnterDarkEnvironment));
        d.getEvents().add(createEvent(TrackerEventType.AUT));
        d.getEvents().add(createEvent(TrackerEventType.DRK));

        final JsonObject obj = factory.toJson(d);
        d = factory.parseDeviceData(obj);

        assertEquals(2, d.getAlerts().size());
        assertEquals(2, d.getEvents().size());
        assertNotNull(d.getDevice());
    }
    public void testShipmentData() {
        final Shipment shipment = createShipment();
        final DeviceData d1 = new DeviceData();
        d1.setDevice(createDevice("109823981237049"));
        final DeviceData d2 = new DeviceData();
        d2.setDevice(createDevice("2309875948987987"));

        ShipmentData s = new ShipmentData();
        s.setShipment(shipment);
        s.getDeviceData().add(d1);
        s.getDeviceData().add(d2);

        final JsonObject obj = factory.toJson(s);
        s = factory.parseShipmentData(obj);

        assertNotNull(s.getShipment());
        assertEquals(2, s.getDeviceData().size());
    }
    public void testUser() {
        final String login = "login";
        final String fullName = "Full Name";

        User u = new User();
        u.setLogin(login);
        u.setFullName(fullName);
        u.getRoles().add(Role.Dispatcher);
        u.getRoles().add(Role.ReportViewer);

        final JsonObject obj = factory.toJson(u);
        u = factory.parseUser(obj);

        assertEquals(login, u.getLogin());
        assertEquals(fullName, u.getFullName());
        assertEquals(2, u.getRoles().size());
    }
    public void testDeviceCommand() {
        final Device device = createDevice("2380947093287");
        final String command = "shutdown";

        DeviceCommand cmd = new DeviceCommand();
        cmd.setDevice(device);
        cmd.setCommand(command);

        final JsonObject obj = factory.toJson(cmd);
        cmd = factory.parseDeviceCommand(obj);

        assertEquals(command, cmd.getCommand());
        assertNotNull(cmd.getDevice());
    }

    /**
     * @return tracker event.
     */
    private TrackerEvent createEvent(final TrackerEventType type) {
        final TrackerEvent e = new TrackerEvent();
        e.setId(generateId());
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
        alert.setId(generateId());
        alert.setName("Any name");
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
        resolver.add(t);
        return t;
    }
    /**
     * @return
     */
    private NotificationSchedule createNotificationSchedule() {
        final NotificationSchedule s = new NotificationSchedule();
        s.setDescription("JUnit schedule");
        s.setId(generateId());
        s.setName("Sched");
        s.getSchedules().add(createSchedulePersonHowWhen());
        s.getSchedules().add(createSchedulePersonHowWhen());
        resolver.add(s);
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
        p.setId(generateId());
        p.setLowTemperature(-10);
        p.setLowTemperatureForMoreThen(55);
        p.setName("AnyAlert");
        p.setWatchBatteryLow(true);
        p.setWatchEnterBrightEnvironment(true);
        p.setWatchEnterDarkEnvironment(true);
        p.setWatchShock(true);
        resolver.add(p);
        return p;
    }
    /**
     * @return any location profile.
     */
    private LocationProfile createLocationProfile() {
        final LocationProfile p = new LocationProfile();

        p.setCompanyDescription("Sun Microsystems");
        p.setId(generateId());
        p.setInterim(true);
        p.setName("JUnit-Location");
        p.setNotes("Any notes");
        p.setAddress("Odessa, Deribasovskaya 1, apt.1");
        p.setRadius(1000);
        p.setStart(true);
        p.setStop(true);
        p.getLocation().setLatitude(100.500);
        p.getLocation().setLongitude(100.501);
        resolver.add(p);
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
        s.setId(generateId());
        s.setName("JUnit-tpl");
        s.setShipmentDescription("Any Description");
        s.setShippedFrom(createLocationProfile());
        s.setShippedTo(createLocationProfile());
        s.setShutdownDeviceTimeOut(155);
        s.getDevices().add(createDevice("234908720394857"));
        s.getDevices().add(createDevice("329847983724987"));
        s.setPalletId("palettid");
        s.setShipmentDescriptionDate(new Date(System.currentTimeMillis() - 1000000000l));
        s.setCustomFields("customFields");
        resolver.add(s);
        return s;
    }
    /**
     * @return
     */
    private Long generateId() {
        return 1000000L + (++lastLong);
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
        s.setId(generateId());
        s.setLastName("Suvorov");
        s.setPosition("Generalisimus");
        s.setPushToMobileApp(true);
        s.setSmsNotification("1111111117");
        s.getWeekDays()[0] = true;
        s.getWeekDays()[3] = true;

        return s;
    }
    @SuppressWarnings("unused")
    private void printJSon(final JsonElement e) {
        final String str = gson.toJson(e);
        System.out.println(str);
    }
}
