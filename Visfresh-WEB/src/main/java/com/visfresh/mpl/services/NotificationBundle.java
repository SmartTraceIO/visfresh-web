/**
 *
 */
package com.visfresh.mpl.services;

import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.l12n.XmlControl;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationBundle extends NotificationIssueBundle {
    /**
     * Default constructor.
     */
    public NotificationBundle() {
        super();
    }

    /**
     *  supported place holders:
     *    ${date} alert issue date include day and year
     *    ${time} the time in scope of day.
     *    ${type} alert type
     *    ${device} device IMEI
     *    ${devicesn} device serial number
     *    ${tripCount} trip count for given device of shipment.
     *
     *  for temperature alerts:
     *    ${temperature}
     *    ${period}
     * @param user target user.
     * @param issue alert
     * @param trackerEvent tracker event
     * @return description for given alert.
     */
    public String getEmailMessage(final User user, final NotificationIssue issue,
            final TrackerEvent trackerEvent) {
        final String str = getBundle().getString("Email." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(user, issue, trackerEvent));
    }
    /**
     *  supported place holders:
     *    ${date} alert issue date include day and year
     *    ${time} the time in scope of day.
     *    ${type} alert type
     *    ${device} device IMEI
     *    ${devicesn} device serial number
     *    ${tripCount} trip count for given device of shipment.
     *
     *  for temperature alerts:
     *    ${temperature}
     *    ${period}
     * @param user target user.
     * @param issue alert
     * @param trackerEvent tracker event.
     * @return description for given alert.
     */
    public String getSmsMessage(final User user, final NotificationIssue issue,
            final TrackerEvent trackerEvent) {
        final String str = getBundle().getString("SMS." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(user, issue, trackerEvent));
    }
    /**
     * @param user user.
     * @param issue notification issue.
     * @param trackerEvent tracker event.
     * @return message.
     */
    public String getAppMessage(final User user, final NotificationIssue issue,
            final TrackerEvent trackerEvent) {
        final String str = getBundle().getString("App." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(user, issue, trackerEvent));
    }
    /**
     * @param user the user.
     * @param issue notification issue.
     * @param trackerEvent tracker event, can be NULL
     * @return
     */
    public String getEmailSubject(final User user, final NotificationIssue issue, final TrackerEvent trackerEvent) {
        final String str = getBundle().getString("Email.Subject." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(user, issue, trackerEvent));
    }
    /**
     * @return resource bundle.
     */
    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle("notifications", XmlControl.INSTANCE);
    }
}
