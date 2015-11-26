/**
 *
 */
package com.visfresh.mock;

import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface MockNotificationServiceListener {
    /**
     * @param s person schedule.
     * @param issue notification issue.
     */
    void sendingNotification(PersonSchedule s, NotificationIssue issue);
}
