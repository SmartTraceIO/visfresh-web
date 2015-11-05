/**
 *
 */
package com.visfresh.services;

import java.util.List;
import java.util.Set;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.ShipmentStateDto;
import com.visfresh.io.UpdateUserDetailsRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RestService {
    Long saveAlertProfile(Company company, final AlertProfile alert);
    List<AlertProfile> getAlertProfiles(Company company);

    Long saveLocation(Company company, final LocationProfile profile);
    List<LocationProfile> getLocation(Company company);

    Long saveNotificationSchedule(Company company, final NotificationSchedule schedule);
    List<NotificationSchedule> getNotificationSchedules(Company company);

    Long saveShipmentTemplate(Company company, final ShipmentTemplate tpl);
    List<ShipmentTemplate> getShipmentTemplates(Company company);

    void saveDevice(Company company, Device device);
    List<Device> getDevices(Company company);

    List<ShipmentStateDto> getShipments(Company company);
    Long saveShipment(Company company, Shipment shipment);

    Long createShipmentTemplate(Company company, Shipment shipment, String templateName);

    List<Notification> getNotifications(User user);
    void markNotificationsAsRead(User user, Set<Long> ids);
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
    /**
     * @param company company.
     * @param id notification schedule ID.
     * @return notification schedule.
     */
    NotificationSchedule getNotificationSchedule(Company company, Long id);
    /**
     * @param user user.
     * @return user profile.
     */
    UserProfile getProfile(User user);
    /**
     * @param user user.
     * @param p profile.
     */
    void saveUserProfile(User user, UserProfile p);
    /**
     * @param id company ID.
     * @return company by given ID.
     */
    Company getCompany(Long id);
    /**
     * @return
     */
    List<Company> getCompanies();
    /**
     * @param company company
     * @param alertProfileId
     */
    void deleteAlertProfile(Company company, Long alertProfileId);
    /**
     * @param company company
     * @param locationId
     */
    void deleteLocation(Company company, Long locationId);
    /**
     * @param company
     * @param notificationScheduleId
     */
    void deleteNotificationSchedule(Company company,
            Long notificationScheduleId);
    /**
     * @param company
     * @param shipmentId
     */
    void deleteShipment(Company company, Long shipmentId);
    /**
     * @param company
     * @param shipmentTemplateId
     */
    void deleteShipmentTemplate(Company company, Long shipmentTemplateId);
    /**
     * @param company company.
     * @param imei device IMEI.
     */
    void deleteDevice(Company company, String imei);
    /**
     * @param req request.
     */
    void updateUserDetails(UpdateUserDetailsRequest req);
}
