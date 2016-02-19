/**
 *
 */
package com.visfresh.services;

import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationService {
    /**
     * @param s person schedule.
     * @param issue notification issue.
     * @param trackerEvent TODO
     */
    void sendNotification(final PersonSchedule s, final NotificationIssue issue, TrackerEvent trackerEvent);
}
