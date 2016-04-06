/**
 *
 */
package com.visfresh.services;

import java.util.TimeZone;

import com.visfresh.entities.Language;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.TemperatureUnits;
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
    /**
     * @param issue notification issue.
     * @param email email address.
     * @param trackerEvent tracker event.
     * @param lang language.
     * @param tz time zone.
     * @param tu temperature unit.
     */
    void sendEmailNotification(NotificationIssue issue, String email,
            TrackerEvent trackerEvent, Language lang, TimeZone tz,
            TemperatureUnits tu);
}
