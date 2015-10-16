/**
 *
 */
package com.visfresh.services;

import java.util.Date;
import java.util.List;

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

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RestService {
    Long saveAlertProfile(Company company, final AlertProfile alert);
    List<AlertProfile> getAlertProfiles(Company company);

    Long saveLocationProfile(Company company, final LocationProfile profile);
    List<LocationProfile> getLocationProfiles(Company company);

    Long saveNotificationSchedule(Company company, final NotificationSchedule schedule);
    List<NotificationSchedule> getNotificationSchedules(Company company);

    Long saveShipmentTemplate(Company company, final ShipmentTemplate tpl);
    List<ShipmentTemplate> getShipmentTemplates(Company company);

    void saveDevice(Company company, Device device);
    List<Device> getDevices(Company company);

    List<Shipment> getShipments(Company company);
    Long saveShipment(Company company, Shipment shipment);

    Long createShipmentTemplate(Company company, Shipment shipment, String templateName);

    List<Notification> getNotifications(User user);
    void markNotificationsAsRead(User user, List<Long> ids);

    List<ShipmentData> getShipmentData(Company company, Date startDate,
            Date endDate, String onlyWithAlerts);

    void sendCommandToDevice(DeviceCommand cmd);

    /**
     * @param company company.
     * @param id alert profile ID.
     * @return alert profile.
     */
    AlertProfile getAlertProfile(Company company, Long id);
    /**
     * @param company company.
     * @param id location profile ID.
     * @return location profile.
     */
    LocationProfile getLocationProfile(Company company, Long id);
    /**
     * @param company company.
     * @param id shipment template ID.
     * @return shipment template.
     */
    ShipmentTemplate getShipmentTemplate(Company company, Long id);
    /**
     * @param company company.
     * @param id device ID.
     * @return device.
     */
    Device getDevice(Company company, String id);
    /**
     * @param company company.
     * @param id shipment ID.
     * @return shipment.
     */
    Shipment getShipment(Company company, Long id);
}
