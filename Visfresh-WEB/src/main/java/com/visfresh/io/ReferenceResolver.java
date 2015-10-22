/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ReferenceResolver {
    LocationProfile getLocationProfile(Long id);
    AlertProfile getAlertProfile(Long id);
    NotificationSchedule getNotificationSchedule(Long id);
    Device getDevice(String id);
    Shipment getShipment(Long id);
    Company getCompany(Long id);
}
