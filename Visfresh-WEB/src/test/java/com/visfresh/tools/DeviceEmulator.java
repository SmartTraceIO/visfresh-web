/**
 *
 */
package com.visfresh.tools;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.Location;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.services.RestServiceException;
import com.visfresh.services.lists.NotificationScheduleListItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceEmulator extends AbstractTool implements Runnable {
    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    private final Random random = new Random();
    private String imei;
    private int battery = 10;
    private int temperature = 7;
    private Location startLocation = new Location(82, 50); //Sidney
    private Location location = new Location(startLocation.getLatitude(), startLocation.getLongitude());
    private Point2D.Double v = new Point2D.Double(0.005, 0.);
    private long startTime;

    /**
     * @param url
     * @param userName
     * @param password
     * @throws IOException
     * @throws RestServiceException
     */
    public DeviceEmulator(final String url, final String userName, final String password)
            throws IOException, RestServiceException {
        super(url, userName, password);
        context.scan(SpringConfig.class.getPackage().getName());
        context.refresh();

        initalize();
    }

    public AlertProfile createAlertProfileIfNeed() throws RestServiceException, IOException {
        final String name = "DevTools Alerts";

        final List<AlertProfile> profiles = this.service.getAlertProfiles(1, 1000);
        for (final AlertProfile alertProfile : profiles) {
            if (name.equals(alertProfile.getName())) {
                return alertProfile;
            }
        }

        final AlertProfile profile = new AlertProfile();

        profile.setCompany(company);
        profile.setName(name);
        profile.setHighTemperature(15);
        profile.setHighTemperatureForMoreThen(5);
        profile.setCriticalHighTemperature(20);
        profile.setCriticalLowTemperatureForMoreThen(1);
        profile.setDescription("Development tool profile");
        profile.setLowTemperature(3);
        profile.setLowTemperatureForMoreThen(10);
        profile.setCriticalLowTemperature(-5);
        profile.setCriticalHighTemperatureForMoreThen(1);
        profile.setWatchBatteryLow(true);
        profile.setWatchEnterBrightEnvironment(true);
        profile.setWatchEnterDarkEnvironment(true);
        profile.setWatchMovementStart(true);
        profile.setWatchMovementStop(true);

        final Long id = service.saveAlertProfile(profile);
        profile.setId(id);

        return profile;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        final int maxPause = 20000;
        final int minPause = 10000;
        startTime = System.currentTimeMillis();

        try {
            while (true) {
                sendSystemDeviceEvent();
                Thread.sleep(minPause + random.nextInt(maxPause - minPause));
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    private void sendSystemDeviceEvent() {
        final JsonObject obj = createEvent();
        sendEvent(obj);
    }

    /**
     * @param obj
     */
    private void sendEvent(final JsonObject obj) {
        final String TABLE = "systemmessages";
        final String TYPE_FIELD = "type";
        final String TYME_FIELD = "time";
        final String RETRYON_FIELD = "retryon";
        final String MESSAGE_FIELD = "message";

        //insert
        final String sql = "insert into " + TABLE + " (" +
                TYME_FIELD
                + "," + TYPE_FIELD
                + "," + RETRYON_FIELD
                + "," + MESSAGE_FIELD
             + ")" + " values("
                + ":"+ TYME_FIELD
                + ", :" + TYPE_FIELD
                + ", :" + RETRYON_FIELD
                + ", :" + MESSAGE_FIELD
                + ")";

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(TYME_FIELD, new Date());
        paramMap.put(TYPE_FIELD, "Tracker");
        paramMap.put(RETRYON_FIELD, new Date());
        paramMap.put(MESSAGE_FIELD, obj.toString());

        final NamedParameterJdbcTemplate jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        jdbc.update(sql, paramMap);
    }

    /**
     * @return
     */
    protected JsonObject createEvent() {
        final JsonObject obj = new JsonObject();
        //update battery
        battery += (random.nextBoolean()? 1 : - 1);
        battery = Math.max(0, battery);
        battery = Math.min(battery, 10);

        //update temperature
        temperature += (random.nextBoolean()? 1 : - 1);
        temperature = Math.max(-10, temperature);
        temperature = Math.min(temperature, 16);

        //update location
        final double dt = (System.currentTimeMillis() - startTime) / 60000L;
        final double lat = location.getLatitude() * v.y * dt;
        final double lon = location.getLongitude();
        location.setLatitude(lat);
        location.setLongitude(lon);

        obj.addProperty("battery", battery);
        obj.addProperty("temperature", temperature);
        obj.addProperty("time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
        obj.addProperty("type", "AUT");
        obj.addProperty("latitude", location.getLatitude());
        obj.addProperty("longitude", location.getLongitude());
        obj.addProperty("imei", imei);
        return obj;
    }

    /**
     * @throws RestServiceException
     * @throws IOException
     */
    protected void initializeShipment() throws RestServiceException, IOException {
        final Device device = createDeviceIfNeed();
        createShipmentIfNeed(device);
        this.imei = device.getImei();
    }

    /**
     * @param device
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    private Shipment createShipmentIfNeed(final Device device) throws RestServiceException, IOException {
        final String description = "DevelopmentShipment";

        final JsonArray shipments = service.getShipments(1, 100000);
        for (final JsonElement e : shipments) {
            final JsonObject obj = e.getAsJsonObject();
            final Long id = obj.get("shipmentId").getAsLong();
            final Shipment shipment = service.getShipment(id);
            if (description.equals(shipment.getShipmentDescription())
                    && shipment.getStatus() != ShipmentStatus.Complete) {
                return shipment;
            }
        }

        //create shipment with default alert
        final Shipment s = new Shipment();

        s.setDevice(device);
        s.setShipmentDescription(description);
        s.setCompany(company);
        s.setAlertProfile(createAlertProfileIfNeed());
        s.getAlertsNotificationSchedules().add(createNotificationScheduleIfNeed());

        final Long id = service.saveShipment(s, null, false);
        s.setId(id);

        return s;
    }

    /**
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    private NotificationSchedule createNotificationScheduleIfNeed() throws RestServiceException, IOException {
        final String name = "Test Schedule";

        final List<NotificationScheduleListItem> schedules = service.getNotificationSchedules(1, 100000);
        for (final NotificationScheduleListItem s : schedules) {
            if (name.equals(s.getNotificationScheduleName())) {
                return service.getNotificationSchedule(s.getId());
            }
        }

        final NotificationSchedule s = new NotificationSchedule();
        s.setCompany(company);
        s.setDescription("Test Notification schedule");
        s.setName(name);

        s.getSchedules().add(createSchedule("Vyacheslav", "Soldatov", "vyacheslav.soldatov@inbox.ru", null, "Java Developer"));
        s.getSchedules().add(createSchedule("James", "Richardson", "james@smarttrace.com.au", "+61414910052", "Manager"));

        final Long id = service.saveNotificationSchedule(s);
        s.setId(id);
        return s;
    }

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @param phone
     * @param position company position
     * @return
     */
    private PersonSchedule createSchedule(final String firstName, final String lastName,
            final String email, final String phone, final String position) {
        final PersonSchedule s = new PersonSchedule();
        s.setCompany("Smart Trace");
        s.setEmailNotification(email);
        s.setFirstName(firstName);
        s.setLastName(lastName);
        s.setFromTime(1);
        s.setToTime(23 * 60 + 55);
        s.setPosition(position);
        s.setPushToMobileApp(true);
        Arrays.fill(s.getWeekDays(), true);
        return s;
    }

    /**
     * @throws IOException
     * @throws RestServiceException
     *
     */
    private Device createDeviceIfNeed() throws RestServiceException, IOException {
        final String id = "111111";

        final List<Device> devices = service.getDevices(1, 100000);
        for (final Device device : devices) {
            if (id.equals(device.getId())) {
                return device;
            }
        }

        final Device d = new Device();
        d.setCompany(company);
        d.setDescription("Develment Tools virtual device");
        d.setImei("111111111111111");
        d.setName("DevTool Device");
        d.setSn("123");

        service.saveDevice(d);
        return d;
    }

    public static void main(final String[] args) throws Exception {
        final DeviceEmulator emulator = new DeviceEmulator("http://localhost:8080/web/vf", "globaladmin", args[0]);
        try {
            emulator.initializeShipment();
            emulator.run();
        } finally {
            emulator.context.destroy();
        }
    }
}
