/**
 *
 */
package com.visfresh.services;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
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
    Long saveAlertProfile(final AlertProfile alert);
    List<AlertProfile> getAlertProfiles();

    Long saveLocationProfile(final LocationProfile profile);
    List<LocationProfile> getLocationProfiles();

    Long saveNotificationSchedule(final NotificationSchedule schedule);
    List<NotificationSchedule> getNotificationSchedules();

    Long saveShipmentTemplate(final ShipmentTemplate tpl);
    List<ShipmentTemplate> getShipmentTemplates();

    void saveDevice(Device device);
    List<Device> getDevices();

    List<Shipment> getShipments();
    Long saveShipment(Shipment shipment);

    Long createShipmentTemplate(Shipment shipment, String templateName);

    List<Notification> getNotifications(Long shipment);
    void markNotificationsAsRead(User user, List<Long> ids);

    List<ShipmentData> getShipmentData(Date startDate, Date endDate,
            String onlyWithAlerts);

    void sendCommandToDevice(Device device, String command);
}
