/**
 *
 */
package com.visfresh.services;

import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationService {
    /**
     * @param s person schedule.
     * @param issue notification issue.
     */
    void sendNotification(final PersonSchedule s, final NotificationIssue issue);
}
