/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ReferenceResolver extends DeviceResolver {
    LocationProfile getLocationProfile(Long id);
    AlertProfile getAlertProfile(Long id);
    NotificationSchedule getNotificationSchedule(Long id);
}
