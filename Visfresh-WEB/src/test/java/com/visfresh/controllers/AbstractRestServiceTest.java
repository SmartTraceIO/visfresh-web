/**
 *
 */
package com.visfresh.controllers;

import java.net.URL;
import java.util.Date;
import java.util.TimeZone;

import org.junit.runner.RunWith;
import org.springframework.context.support.AbstractApplicationContext;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(RestServiceRunner.class)
public abstract class AbstractRestServiceTest {
    protected static TimeZone UTC = SerializerUtils.UTС;
    protected AbstractApplicationContext context;
    private long lastLong;
    private URL serviceUrl;

    /**
     * Default constructor.
     */
    public AbstractRestServiceTest() {
        super();
    }

    /**
     * @return the context
     */
    public AbstractApplicationContext getContext() {
        return context;
    }
    /**
     * @param context the context to set
     */
    public void setContext(final AbstractApplicationContext context) {
        this.context = context;
    }

    public User createUser1() {
        final User u = new User();
        u.setCompany(getCompany());
        u.setEmail("asuvorov@mail.ru");
        u.setFirstName("Alexander");
        u.setLastName("Suvorov");
        u.setLogin("asuvorov");
        u.setPhone("11111111117");
        u.setTemperatureUnits(TemperatureUnits.Celsius);
        u.setTimeZone(TimeZone.getTimeZone("UTC"));
        getContext().getBean(UserDao.class).save(u);
        return u;
    }
    public User createUser2() {
        final User u = new User();
        u.setCompany(getCompany());
        u.setEmail("mkutuzov@mail.ru");
        u.setFirstName("Mikhael");
        u.setLastName("Kutuzov");
        u.setLogin("mkutuzov");
        u.setPhone("11111111118");
        u.setTemperatureUnits(TemperatureUnits.Fahrenheit);
        u.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        getContext().getBean(UserDao.class).save(u);
        return u;
    }
    /**
     * @param save TODO
     * @return any alert profile.
     */
    protected AlertProfile createAlertProfile(final boolean save) {
        final AlertProfile ap = new AlertProfile();
        ap.setName("AnyAlert");
        ap.setDescription("Any description");

        final int normalTemperature = 3;
        AlertRule criticalHot = new AlertRule(AlertType.CriticalHot);
        criticalHot.setTemperature(normalTemperature + 15);
        criticalHot.setTimeOutMinutes(0);
        criticalHot.setCumulativeFlag(true);
        ap.getAlertRules().add(criticalHot);

        criticalHot = new AlertRule(AlertType.CriticalHot);
        criticalHot.setTemperature(normalTemperature + 14);
        criticalHot.setCumulativeFlag(true);
        criticalHot.setTimeOutMinutes(1);
        ap.getAlertRules().add(criticalHot);

        AlertRule criticalLow = new AlertRule(AlertType.CriticalCold);
        criticalLow.setTemperature(normalTemperature -15.);
        criticalLow.setTimeOutMinutes(0);
        criticalLow.setCumulativeFlag(true);
        ap.getAlertRules().add(criticalLow);

        criticalLow = new AlertRule(AlertType.CriticalCold);
        criticalLow.setTemperature(normalTemperature -14.);
        criticalLow.setTimeOutMinutes(1);
        criticalLow.setCumulativeFlag(true);
        ap.getAlertRules().add(criticalLow);

        AlertRule hot = new AlertRule(AlertType.Hot);
        hot.setTemperature(normalTemperature + 3);
        hot.setTimeOutMinutes(0);
        hot.setCumulativeFlag(true);
        ap.getAlertRules().add(hot);

        hot = new AlertRule(AlertType.Hot);
        hot.setTemperature(normalTemperature + 4.);
        hot.setTimeOutMinutes(2);
        hot.setCumulativeFlag(true);
        ap.getAlertRules().add(hot);

        AlertRule low = new AlertRule(AlertType.Cold);
        low.setTemperature(normalTemperature -10.);
        low.setTimeOutMinutes(40);
        low.setCumulativeFlag(true);
        ap.getAlertRules().add(low);

        low = new AlertRule(AlertType.Cold);
        low.setTemperature(normalTemperature-8.);
        low.setTimeOutMinutes(55);
        low.setCumulativeFlag(true);
        ap.getAlertRules().add(low);

        ap.setWatchBatteryLow(true);
        ap.setWatchEnterBrightEnvironment(true);
        ap.setWatchEnterDarkEnvironment(true);
        ap.setWatchMovementStart(true);
        ap.setWatchMovementStop(true);

        if (save) {
            saveAlertProfileDirectly(ap);
        }
        return ap;
    }
    /**
     * @return any location profile.
     */
    protected LocationProfile createLocationProfile(final boolean save) {
        final LocationProfile p = new LocationProfile();

        p.setCompanyName("Sun Microsystems");
        p.setCompany(getCompany());
        p.setInterim(true);
        p.setName("Loc-" + (++lastLong));
        p.setNotes("Any notes");
        p.setAddress("Bankstown Warehouse");
        p.setRadius(1000);
        p.setStart(true);
        p.setStop(true);
        p.getLocation().setLatitude(100.500);
        p.getLocation().setLongitude(100.501);
        if (save) {
            saveLocationDirectly(p);
        }
        return p;
    }
    /**
     * @return
     */
    protected NotificationSchedule createNotificationSchedule(final User user, final boolean save) {
        final NotificationSchedule s = new NotificationSchedule();
        s.setDescription("JUnit schedule");
        s.setName("Sched");
        s.getSchedules().add(createPersonSchedule(user));
        s.getSchedules().add(createPersonSchedule(user));
        if (save) {
            saveNotificationScheduleDirectly(s);
        }
        return s;
    }

    /**
     * @param s
     */
    protected Long saveNotificationScheduleDirectly(final NotificationSchedule s) {
        final NotificationScheduleDao dao = context.getBean(NotificationScheduleDao.class);
        s.setCompany(getCompany());
        dao.save(s);
        return s.getId();
    }
    protected Long saveAlertProfileDirectly(final AlertProfile p) {
        final AlertProfileDao dao = context.getBean(AlertProfileDao.class);
        p.setCompany(getCompany());
        dao.save(p);
        return p.getId();
    }
    /**
     * @param company
     * @param p
     */
    protected Long saveLocationDirectly(final LocationProfile p) {
        final LocationProfileDao dao = context.getBean(LocationProfileDao.class);
        p.setCompany(getCompany());
        dao.save(p);
        return p.getId();
    }
    /**
     * @param s
     */
    protected Long saveShipmentDirectly(final Shipment s) {
        final ShipmentDao dao = context.getBean(ShipmentDao.class);
        s.setCompany(getCompany());
        dao.save(s);
        return s.getId();
    }
    /**
     * @param t
     */
    protected Long saveShipmentTemplateDirectly(final ShipmentTemplate t) {
        final ShipmentTemplateDao dao = context.getBean(ShipmentTemplateDao.class);
        t.setCompany(getCompany());
        dao.save(t);
        return t.getId();
    }

    /**
     * @return any schedule/person/how/when
     */
    protected PersonSchedule createPersonSchedule(final User user) {
        final PersonSchedule s = new PersonSchedule();

        s.setToTime(1200);
        s.setFromTime(800);
        s.setSendApp(true);
        s.getWeekDays()[0] = true;
        s.getWeekDays()[3] = true;
        s.setUser(user);

        return s;
    }
    protected Shipment createShipment(final boolean save) {
        final Shipment s = new Shipment();
        s.setAlertProfile(createAlertProfile(save));
        s.getAlertsNotificationSchedules().add(createNotificationSchedule(createUser1(), save));
        s.setAlertSuppressionMinutes(55);
        s.setArrivalNotificationWithinKm(111);
        s.getArrivalNotificationSchedules().add(createNotificationSchedule(createUser2(), save));
        s.setExcludeNotificationsIfNoAlerts(true);
        s.setShipmentDescription("Any Description");
        s.setShippedFrom(createLocationProfile(true));
        s.setShippedTo(createLocationProfile(true));
        s.setShutdownDeviceTimeOut(155);
        s.setDevice(createDevice("234908720394857", save));
        s.setPalletId("palettid");
        s.setAssetNum("10515");
        s.setShipmentDate(new Date(System.currentTimeMillis() - 1000000000l));
        s.getCustomFields().put("field1", "value1");
        s.setAssetType("SeaContainer");
        s.setPoNum(893793487);
        s.setTripCount(88);
        s.setCompany(getCompany());
        s.setStatus(ShipmentStatus.InProgress);
        s.setCommentsForReceiver("Comments for receiver");
        if (save) {
            saveShipmentDirectly(s);
        }
        return s;
    }
    protected ShipmentTemplate createShipmentTemplate(final boolean save) {
        final ShipmentTemplate t = new ShipmentTemplate();
        t.setAddDateShipped(true);
        t.setAlertProfile(createAlertProfile(save));
        t.getAlertsNotificationSchedules().add(createNotificationSchedule(createUser1(), save));
        t.setAlertSuppressionMinutes(55);
        t.setArrivalNotificationWithinKm(11);
        t.getArrivalNotificationSchedules().add(createNotificationSchedule(createUser2(), save));
        t.setExcludeNotificationsIfNoAlerts(true);
        t.setName("JUnit-tpl");
        t.setShipmentDescription("Any Description");
        t.setShippedFrom(createLocationProfile(save));
        t.setShippedTo(createLocationProfile(save));
        t.setShutdownDeviceTimeOut(155);
        t.setUseCurrentTimeForDateShipped(true);
        t.setDetectLocationForShippedFrom(true);
        t.setCommentsForReceiver("Comments for receiver");

        if (save) {
            saveShipmentTemplateDirectly(t);
        }
        return t;
    }
    /**
     * @param imei IMEI.
     * @return
     */
    protected Device createDevice(final String imei, final boolean save) {
        final Device t = new Device();
        t.setDescription("Device description");
        t.setImei(imei);
        t.setName("Device Name");
        t.setSn(imei.substring(imei.length() - 6));
        if (save) {
            t.setCompany(getCompany());
            context.getBean(DeviceDao.class).save(t);
        }
        return t;
    }

    /**
     * @return
     */
    protected Company getCompany() {
        for (final Company c : context.getBean(CompanyDao.class).findAll(null, null, null)) {
            if (RestServiceRunner.SHARED_COMPANY_NAME.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }
    protected String login() {
        try {
            final User user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
            return context.getBean(AuthService.class).login(user.getLogin(),"").getToken();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @param url URL.
     */
    public void setServiceUrl(final URL url) {
        serviceUrl = url;
    }
    /**
     * @return the serviceUrl
     */
    public URL getServiceUrl() {
        return serviceUrl;
    }
}
