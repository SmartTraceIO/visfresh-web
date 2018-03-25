/**
 *
 */
package com.visfresh.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Color;
import com.visfresh.entities.Company;
import com.visfresh.entities.CorrectiveActionList;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.json.fastxml.JsonSerializerFactory;
import com.visfresh.io.shipment.AlertProfileBean;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.CorrectiveActionListBean;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentLocationBean;
import com.visfresh.io.shipment.TemperatureRuleBean;
import com.visfresh.lists.ListNotificationScheduleItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentUtils {
    /**
     * @param in input stream.
     * @return single shipment data from json stream.
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static SingleShipmentData parseSingleShipmentDataJson(final InputStream in) throws JsonParseException, JsonMappingException, IOException {
        return JsonSerializerFactory.FACTORY.createSingleShipmentDataParser().readValue(in, SingleShipmentData.class);
    }
    /**
     * @param url resource.
     * @return single shipment data from json stream.
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static SingleShipmentData parseSingleShipmentDataJson(final URL url) throws JsonParseException, JsonMappingException, IOException {
        final InputStream in = url.openStream();
        try {
            return parseSingleShipmentDataJson(in);
        } finally {
            in.close();
        }
    }
    public static Shipment createShipment(final SingleShipmentBean bean) {
        final Shipment s = new Shipment();
        final Company company = createCompany(bean.getCompanyId());
        s.setCompany(company.getCompanyId());

        s.setAlertProfile(createAlertProfile(bean.getAlertProfile(), bean, company));

        s.setAlertSuppressionMinutes(bean.getAlertSuppressionMinutes());
        s.setArrivalDate(bean.getArrivalTime());
        s.setArrivalNotificationWithinKm(bean.getArrivalNotificationWithinKm());
        s.setAssetNum(bean.getAssetNum());
        s.setAssetType(bean.getAssetType());
        //s.setAutostart(bean.is);
        s.setCommentsForReceiver(bean.getCommentsForReceiver());
        //s.setCreatedBy(bean.getCr);
        s.setDevice(createDevice(bean.getDevice(), bean.getDeviceColor(), bean.getDeviceName(), bean.getDeviceGroups(), company));
        //s.setDeviceShutdownTime(bean.getShutdownDeviceAfterMinutes());
        s.setEta(bean.getEta());
        s.setExcludeNotificationsIfNoAlerts(bean.isExcludeNotificationsIfNoAlerts());
        s.setId(bean.getShipmentId());
        s.setLastEventDate(bean.getLastReadingTime());
        s.setNoAlertsAfterArrivalMinutes(bean.getNoAlertsAfterArrivalMinutes());
        s.setNoAlertsAfterStartMinutes(bean.getNoAlertsAfterStartMinutes());
        s.setPalletId(bean.getPalletId());
        //s.setPoNum(bean.getP);
        s.setSendArrivalReport(bean.isSendArrivalReport());
        s.setSendArrivalReportOnlyIfAlerts(bean.isSendArrivalReportOnlyIfAlerts());
        s.setShipmentDate(bean.getStartTime());
        s.setShipmentDescription(bean.getShipmentDescription());
        s.setShippedFrom(createLocationProfile(bean.getStartLocation(), company));
        s.setShippedTo(createLocationProfile(bean.getEndLocation(), company));
        s.setShutDownAfterStartMinutes(bean.getShutDownAfterStartMinutes());
        s.setShutdownDeviceAfterMinutes(bean.getShutdownDeviceAfterMinutes());
        s.setSiblingCount(bean.getSiblings().size());
        s.setStartDate(bean.getStartTime());
        s.setStatus(bean.getStatus());
        s.setTripCount(bean.getTripCount());
        s.getAlertsNotificationSchedules().addAll(createNotificationSchedules(bean.getAlertsNotificationSchedules(), company));
        s.getArrivalNotificationSchedules().addAll(createNotificationSchedules(bean.getArrivalNotificationSchedules(), company));
        s.getCompanyAccess().addAll(createCompanyAccesses(bean.getCompanyAccess()));
        //s.getCustomFields().putAll(bean.getCu);
        s.getSiblings().addAll(bean.getSiblings());
        s.getUserAccess().addAll(createUserAccesses(bean.getUserAccess(), company));

        return s;
    }
    /**
     * @param userAccess
     * @return
     */
    private static List<User> createUserAccesses(final List<ShipmentUserDto> userAccess, final Company company) {
        final List<User> users = new LinkedList<>();
        for (final ShipmentUserDto dto : userAccess) {
            users.add(createUser(dto, company));
        }
        return users;
    }
    /**
     * @param dto
     * @param company
     * @return
     */
    private static User createUser(final ShipmentUserDto dto, final Company company) {
        final User u = new User();
        dto.setId(dto.getId());
        dto.setEmail(dto.getEmail());
        return u;
    }
    /**
     * @param beans
     * @return
     */
    private static List<Company> createCompanyAccesses(final List<ShipmentCompanyDto> beans) {
        final List<Company> companies = new LinkedList<>();
        for (final ShipmentCompanyDto c : beans) {
            final Company company = new Company(c.getId());
            company.setName(c.getName());
        }
        return companies;
    }
    /**
     * @param beans
     * @param company
     * @return
     */
    private static List<NotificationSchedule> createNotificationSchedules(
            final List<ListNotificationScheduleItem> beans, final Company company) {
        final List<NotificationSchedule> schedules = new LinkedList<>();
        for (final ListNotificationScheduleItem bean : beans) {
            schedules.add(createNotificationSchedulee(bean, company));
        }
        return schedules;
    }
    /**
     * @param bean bean.
     * @param company company.
     * @return
     */
    private static NotificationSchedule createNotificationSchedulee(final ListNotificationScheduleItem bean,
            final Company company) {
        final NotificationSchedule s = new NotificationSchedule();
        s.setCompany(company.getCompanyId());
        s.setDescription(bean.getNotificationScheduleDescription());
        s.setId(bean.getId());
        s.setName(bean.getNotificationScheduleName());
        return s;
    }
    /**
     * @param bean
     * @param company company
     * @return
     */
    private static LocationProfile createLocationProfile(final LocationProfileBean bean, final Company company) {
        if (bean == null) {
            return null;
        }

        final LocationProfile p = new LocationProfile();
        p.setAddress(bean.getAddress());
        p.setCompany(company.getCompanyId());
        p.setId(bean.getId());
        p.setInterim(bean.isInterim());
        p.setName(bean.getName());
        p.setNotes(bean.getNotes());
        p.setRadius(bean.getRadius());
        p.setStart(bean.isStart());
        p.setStop(bean.isStop());
        return p;
    }
    /**
     * @param imei
     * @param color
     * @param name
     * @param groups
     * @return
     */
    private static Device createDevice(final String imei, final String color, final String name,
            final List<DeviceGroupDto> groups, final Company company) {
        final Device d = new Device();
        d.setActive(true);
        if (color != null) {
            d.setColor(Color.valueOf(color));
        }
        d.setCompany(company.getCompanyId());
        d.setImei(imei);
        d.setName(name);
        return d;
    }
    /**
     * @param id
     * @return
     */
    private static Company createCompany(final Long id) {
        final Company c = new Company(id);
        return c;
    }
    /**
     * @param bean
     * @param shipment
     * @return
     */
    private static AlertProfile createAlertProfile(final AlertProfileBean bean, final SingleShipmentBean shipment, final Company company) {
        final AlertProfile ap = new AlertProfile();
        ap.setBatteryLowCorrectiveActions(createCorrectiveActionList(bean.getBatteryLowCorrectiveActions(), company));
        ap.setCompany(company.getCompanyId());
        ap.setDescription(bean.getDescription());
        ap.setId(bean.getId());
        ap.setLightOnCorrectiveActions(createCorrectiveActionList(bean.getLightOnCorrectiveActions(), company));
        ap.setLowerTemperatureLimit(bean.getLowerTemperatureLimit());
        ap.setName(bean.getName());
        ap.setUpperTemperatureLimit(bean.getUpperTemperatureLimit());
        ap.setWatchBatteryLow(bean.isWatchBatteryLow());
        ap.setWatchEnterBrightEnvironment(bean.isWatchEnterBrightEnvironment());
        ap.setWatchEnterDarkEnvironment(bean.isWatchEnterDarkEnvironment());
        ap.setWatchMovementStart(bean.isWatchMovementStart());
        ap.setWatchMovementStop(bean.isWatchMovementStop());

        final List<AlertRuleBean> rules = new LinkedList<>(shipment.getAlertFired());
        rules.addAll(shipment.getAlertYetToFire());
        ap.getAlertRules().addAll(createAlertRules(rules, company));

        return ap;
    }
    /**
     * @param beans
     * @return
     */
    private static List<TemperatureRule> createAlertRules(final List<AlertRuleBean> beans, final Company company) {
        final List<TemperatureRule> rules = new LinkedList<>();
        for (final AlertRuleBean bean: beans) {
            if (bean instanceof TemperatureRuleBean) {
                rules.add(createTemperatureAlertRule((TemperatureRuleBean) bean, company));
            }
        }
        return rules;
    }
    /**
     * @param bean
     * @return
     */
    private static TemperatureRule createTemperatureAlertRule(final TemperatureRuleBean bean, final Company company) {
        final TemperatureRule r = new TemperatureRule();
        r.setCorrectiveActions(createCorrectiveActionList(bean.getCorrectiveActions(), company));
        r.setCumulativeFlag(bean.hasCumulativeFlag());
        r.setId(bean.getId());
        r.setMaxRateMinutes(bean.getMaxRateMinutes());
        r.setTemperature(bean.getTemperature());
        r.setTimeOutMinutes(bean.getTimeOutMinutes());
        r.setType(bean.getType());
        return r;
    }
    /**
     * @param bean
     * @return
     */
    private static CorrectiveActionList createCorrectiveActionList(final CorrectiveActionListBean bean, final Company company) {
        if (bean == null) {
            return null;
        }

        final CorrectiveActionList al = new CorrectiveActionList();
        al.setCompany(company.getCompanyId());
        al.setDescription(bean.getDescription());
        al.setId(bean.getId());
        al.setName(bean.getName());

        al.getActions().addAll(bean.getActions());
        return al;
    }
    public static List<TrackerEvent> getTrackerEvents(final List<SingleShipmentLocationBean> locs, final Shipment s) {
        final List<TrackerEvent> events = new LinkedList<>();
        for (final SingleShipmentLocationBean bean : locs) {
            events.add(createTrackerEvent(bean, s));
        }
        return events;
    }
    public static List<TrackerEvent> getTrackerEvents(final SingleShipmentData data) {
        return getTrackerEvents(data.getLocations(), createShipment(data.getBean()));
    }
    /**
     * @param bean tracker event bean.
     * @param s shipment.
     * @return tracker event.
     */
    private static TrackerEvent createTrackerEvent(final SingleShipmentLocationBean bean, final Shipment s) {
        final TrackerEvent e = new TrackerEvent();
        //e.setBattery(bean.get);
        e.setCreatedOn(bean.getTime());
        e.setDevice(s.getDevice());
        e.setId(bean.getId());
        e.setBeaconId(bean.getBeaconId());
        e.setLatitude(bean.getLatitude());
        e.setLongitude(bean.getLongitude());
        e.setShipment(s);
        e.setTemperature(bean.getTemperature());
        e.setTime(bean.getTime());
        e.setType(bean.getType());
        return e;
    }
}
