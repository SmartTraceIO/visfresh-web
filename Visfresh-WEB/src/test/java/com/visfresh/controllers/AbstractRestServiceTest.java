/**
 *
 */
package com.visfresh.controllers;

import java.util.Date;

import org.junit.runner.RunWith;
import org.springframework.web.context.WebApplicationContext;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(RestServiceRunner.class)
public abstract class AbstractRestServiceTest {
    protected WebApplicationContext context;
    protected RestServiceFacade facade;
    private long lastLong;

    /**
     * Default constructor.
     */
    public AbstractRestServiceTest() {
        super();
    }

    /**
     * @return the context
     */
    public WebApplicationContext getContext() {
        return context;
    }
    /**
     * @param context the context to set
     */
    public void setContext(final WebApplicationContext context) {
        this.context = context;
    }
    /**
     * @return the facade
     */
    public RestServiceFacade getFacade() {
        return facade;
    }
    /**
     * @param facade the facade to set
     */
    public void setFacade(final RestServiceFacade facade) {
        this.facade = facade;
    }

    /**
     * @param save TODO
     * @return any alert profile.
     */
    protected AlertProfile createAlertProfile(final boolean save) {
        final AlertProfile p = new AlertProfile();
        p.setName("AnyAlert");
        p.setDescription("Any description");

        p.setCriticalHighTemperature(10.);
        p.setCriticalHighTemperatureForMoreThen(0);
        p.setCriticalHighTemperature2(9.);
        p.setCriticalHighTemperatureForMoreThen2(1);

        p.setCriticalLowTemperature(-15.);
        p.setCriticalLowTemperatureForMoreThen(0);
        p.setCriticalLowTemperature2(-14.);
        p.setCriticalLowTemperatureForMoreThen2(1);

        p.setHighTemperature(5.);
        p.setHighTemperatureForMoreThen(0);
        p.setHighTemperature2(4.);
        p.setHighTemperatureForMoreThen2(2);

        p.setLowTemperature(-10.);
        p.setLowTemperatureForMoreThen(40);
        p.setLowTemperature2(-8.);
        p.setLowTemperatureForMoreThen2(55);

        p.setWatchBatteryLow(true);
        p.setWatchEnterBrightEnvironment(true);
        p.setWatchEnterDarkEnvironment(true);
        p.setWatchMovementStart(true);
        p.setWatchMovementStop(true);

        if (save) {
            saveAlertProfileDirectly(p);
        }
        return p;
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
    protected NotificationSchedule createNotificationSchedule(final boolean save) {
        final NotificationSchedule s = new NotificationSchedule();
        s.setDescription("JUnit schedule");
        s.setName("Sched");
        s.getSchedules().add(createPersonSchedule());
        s.getSchedules().add(createPersonSchedule());
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
    protected PersonSchedule createPersonSchedule() {
        final PersonSchedule s = new PersonSchedule();

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
    protected Shipment createShipment(final boolean save) {
        final Shipment s = new Shipment();
        s.setAlertProfile(createAlertProfile(save));
        s.getAlertsNotificationSchedules().add(createNotificationSchedule(save));
        s.setAlertSuppressionMinutes(55);
        s.setArrivalNotificationWithinKm(111);
        s.getArrivalNotificationSchedules().add(createNotificationSchedule(save));
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
        t.getAlertsNotificationSchedules().add(createNotificationSchedule(save));
        t.setAlertSuppressionMinutes(55);
        t.setArrivalNotificationWithinKm(11);
        t.getArrivalNotificationSchedules().add(createNotificationSchedule(save));
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
}
