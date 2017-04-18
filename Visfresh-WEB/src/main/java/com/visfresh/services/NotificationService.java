/**
 *
 */
package com.visfresh.services;

import java.util.List;
import java.util.TimeZone;

import com.visfresh.entities.Language;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationService {
    /**
     * @param s person schedule.
     * @param issue notification issue.
     * @param trackerEvent tracker event.
     */
    void sendNotification(final PersonSchedule s, final NotificationIssue issue, TrackerEvent trackerEvent);
    /**
     * @param issue notification issue.
     * @param user email address.
     * @param trackerEvent tracker event.
     * @param lang language.
     * @param tz time zone.
     * @param tu temperature unit.
     */
    void sendEmailNotification(NotificationIssue issue, User user,
            TrackerEvent trackerEvent, Language lang, TimeZone tz,
            TemperatureUnits tu);
    /**
     * @param shipment shipment.
     * @param user user.
     * @param usersReceivedReports TODO
     */
    void sendShipmentReport(Shipment shipment, User user, List<User> usersReceivedReports);
}
