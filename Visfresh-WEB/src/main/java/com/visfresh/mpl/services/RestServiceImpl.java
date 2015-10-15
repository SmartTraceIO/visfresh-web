/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
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
    public Long saveAlertProfile(final AlertProfile alert) {
        return alertProfileDao.save(alert).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getAlertProfiles()
     */
    @Override
    public List<AlertProfile> getAlertProfiles() {
        return asList(alertProfileDao.findAll());
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveLocationProfile(com.visfresh.entities.LocationProfile)
     */
    @Override
    public Long saveLocationProfile(final LocationProfile profile) {
        return locationProfileDao.save(profile).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getLocationProfiles()
     */
    @Override
    public List<LocationProfile> getLocationProfiles() {
        return asList(locationProfileDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveNotificationSchedule(com.visfresh.entities.NotificationSchedule)
     */
    @Override
    public Long saveNotificationSchedule(final NotificationSchedule schedule) {
        return notificationScheduleDao.save(schedule).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getNotificationSchedules()
     */
    @Override
    public List<NotificationSchedule> getNotificationSchedules() {
        return asList(notificationScheduleDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveShipmentTemplate(com.visfresh.entities.ShipmentTemplate)
     */
    @Override
    public Long saveShipmentTemplate(final ShipmentTemplate tpl) {
        return shipmentTemplateDao.save(tpl).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipmentTemplates()
     */
    @Override
    public List<ShipmentTemplate> getShipmentTemplates() {
        return asList(shipmentTemplateDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveDevice(com.visfresh.entities.Device)
     */
    @Override
    public void saveDevice(final Device device) {
        deviceDao.save(device);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getDevices()
     */
    @Override
    public List<Device> getDevices() {
        return asList(deviceDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipments()
     */
    @Override
    public List<Shipment> getShipments() {
        return asList(shipmentDao.findAll());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    public Long saveShipment(final Shipment shipment) {
        return shipmentDao.save(shipment).getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#createShipmentTemplate(com.visfresh.entities.Shipment, java.lang.String)
     */
    @Override
    public Long createShipmentTemplate(final Shipment shipment, final String templateName) {
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
    public List<Notification> getNotifications(final Long shipment) {
        return notificationDao.findByShipment(shipment);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#markNotificationsAsRead(com.visfresh.entities.User, java.util.List)
     */
    @Override
    public void markNotificationsAsRead(final User user, final List<Long> ids) {
        notificationDao.deleteByUserAndId(user, ids);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipmentData(java.util.Date, java.util.Date, java.lang.String)
     */
    @Override
    public List<ShipmentData> getShipmentData(final Date startDate, final Date endDate,
            final String onlyWithAlerts) {
        return shipmentDao.getShipmentData(startDate, endDate, onlyWithAlerts);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#sendCommandToDevice(com.visfresh.entities.Device, java.lang.String)
     */
    @Override
    public void sendCommandToDevice(final DeviceCommand cmd) {
        deviceCommandDao.save(cmd);
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
