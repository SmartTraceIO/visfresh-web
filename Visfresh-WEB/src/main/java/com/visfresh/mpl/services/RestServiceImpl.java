/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.UserDao;
import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.services.RestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {DaoImplBase.class})
public class RestServiceImpl implements RestService {
    @Autowired
    private AlertProfileDao alertProfileDao;
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private NotificationScheduleDao notificationScheduleDao;
    @Autowired
    private ShipmentTemplateDao shipmentTemplateDao;
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private NotificationDao notificationDao;
    @Autowired
    private DeviceCommandDao deviceCommandDao;
    @Autowired
    private UserDao userDao;

    /**
     * Default constructor.
     */
    public RestServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveAlertProfile(com.visfresh.entities.AlertProfile)
     */
    @Override
    public Long saveAlertProfile(final Company company, final AlertProfile alert) {
        return alertProfileDao.save(alert).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getAlertProfiles()
     */
    @Override
    public List<AlertProfile> getAlertProfiles(final Company company) {
        return asList(alertProfileDao.findAll());
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveLocationProfile(com.visfresh.entities.LocationProfile)
     */
    @Override
    public Long saveLocationProfile(final Company company, final LocationProfile profile) {
        return locationProfileDao.save(profile).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getLocationProfiles()
     */
    @Override
    public List<LocationProfile> getLocationProfiles(final Company company) {
        return asList(locationProfileDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveNotificationSchedule(com.visfresh.entities.NotificationSchedule)
     */
    @Override
    public Long saveNotificationSchedule(final Company company, final NotificationSchedule schedule) {
        return notificationScheduleDao.save(schedule).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getNotificationSchedules()
     */
    @Override
    public List<NotificationSchedule> getNotificationSchedules(final Company company) {
        return asList(notificationScheduleDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveShipmentTemplate(com.visfresh.entities.ShipmentTemplate)
     */
    @Override
    public Long saveShipmentTemplate(final Company company, final ShipmentTemplate tpl) {
        return shipmentTemplateDao.save(tpl).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipmentTemplates()
     */
    @Override
    public List<ShipmentTemplate> getShipmentTemplates(final Company company) {
        return asList(shipmentTemplateDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveDevice(com.visfresh.entities.Device)
     */
    @Override
    public void saveDevice(final Company company, final Device device) {
        deviceDao.save(device);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getDevices()
     */
    @Override
    public List<Device> getDevices(final Company company) {
        return asList(deviceDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipments()
     */
    @Override
    public List<Shipment> getShipments(final Company company) {
        return asList(shipmentDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    public Long saveShipment(final Company company, final Shipment shipment) {
        return shipmentDao.save(shipment).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#createShipmentTemplate(com.visfresh.entities.Shipment, java.lang.String)
     */
    @Override
    public Long createShipmentTemplate(final Company company, final Shipment shipment, final String templateName) {
        final ShipmentTemplate tpl = new ShipmentTemplate(shipment);
        tpl.setAddDateShipped(true);
        tpl.setDetectLocationForShippedFrom(true);
        tpl.setUseCurrentTimeForDateShipped(true);
        return shipmentTemplateDao.save(tpl).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getNotifications(java.lang.Long)
     */
    @Override
    public List<Notification> getNotifications(final User user) {
        return notificationDao.findForUser(user);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#markNotificationsAsRead(com.visfresh.entities.User, java.util.List)
     */
    @Override
    public void markNotificationsAsRead(final User user, final Set<Long> ids) {
        notificationDao.deleteByUserAndId(user, ids);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipmentData(java.util.Date, java.util.Date, java.lang.String)
     */
    @Override
    public List<ShipmentData> getShipmentData(final Company company, final Date startDate,
            final Date endDate, final boolean onlyWithAlerts) {
        return shipmentDao.getShipmentData(company, startDate, endDate, onlyWithAlerts);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#sendCommandToDevice(com.visfresh.entities.Device, java.lang.String)
     */
    @Override
    public void sendCommandToDevice(final DeviceCommand cmd) {
        deviceCommandDao.save(cmd);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getAlertProfile(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public AlertProfile getAlertProfile(final Company company, final Long id) {
        final AlertProfile p = alertProfileDao.findOne(id);
        return p == null || !p.getCompany().getId().equals(company.getId()) ? null : p;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getDevice(com.visfresh.entities.Company, java.lang.String)
     */
    @Override
    public Device getDevice(final Company company, final String id) {
        final Device d = deviceDao.findOne(id);
        return  d == null || !d.getCompany().getId().equals(company.getId()) ? null : d;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getLocationProfile(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public LocationProfile getLocationProfile(final Company company, final Long id) {
        final LocationProfile p = locationProfileDao.findOne(id);
        return p == null || !p.getCompany().getId().equals(company.getId()) ? null : p;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipment(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public Shipment getShipment(final Company company, final Long id) {
        final Shipment s = shipmentDao.findOne(id);
        return  s == null || !s.getCompany().getId().equals(company.getId()) ? null : s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipmentTemplate(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public ShipmentTemplate getShipmentTemplate(final Company company, final Long id) {
        final ShipmentTemplate s = shipmentTemplateDao.findOne(id);
        return  s == null || !s.getCompany().getId().equals(company.getId()) ? null : s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getNotificationSchedule(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public NotificationSchedule getNotificationSchedule(final Company company, final Long id) {
        final NotificationSchedule s = notificationScheduleDao.findOne(id);
        return  s == null || !s.getCompany().getId().equals(company.getId()) ? null : s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getProfile(com.visfresh.entities.User)
     */
    @Override
    public UserProfile getProfile(final User user) {
        return userDao.getProfile(user);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveUserProfile(com.visfresh.entities.User, com.visfresh.entities.UserProfile)
     */
    @Override
    public void saveUserProfile(final User user, final UserProfile p) {
        // check hacking
        final Long id = user.getCompany().getId();
        for (final Shipment s : p.getShipments()) {
            if (!s.getCompany().getId().equals(id)) {
                return;
            }
        }

        userDao.saveProfile(user, p);
    }
    /**
     * @param iter
     * @return
     */
    private <E> List<E> asList(final Iterable<E> iter) {
        final List<E> list = new LinkedList<E>();
        for (final E e : iter) {
            list.add(e);
        }
        return list;
    }
}
