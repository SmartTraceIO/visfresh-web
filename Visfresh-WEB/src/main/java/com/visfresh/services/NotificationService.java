/**
 *
 */
package com.visfresh.services;

import java.util.List;

import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationService {
    /**
     * @param schedules person schedule.
     * @param issue notification issue.
     * @param trackerEvent tracker event.
     */
    void sendNotification(final List<PersonSchedule> schedules, final NotificationIssue issue, TrackerEvent trackerEvent);
    /**
     * @param issue notification issue.
     * @param users email address.
     * @param trackerEvent tracker event.
     */
    void sendEmailNotification(NotificationIssue issue, List<User> users,
            TrackerEvent trackerEvent);
    /**
     * @param shipment shipment.
     * @param usersReceivedReports report receivers.
     */
    void sendShipmentReport(Shipment shipment, List<User> usersReceivedReports);
    /**
     * @param shipment the shipment session.
     * @return true if arrival report has sent for given shipment.
     */
    boolean isArrivalReportSent(Shipment shipment);
}
