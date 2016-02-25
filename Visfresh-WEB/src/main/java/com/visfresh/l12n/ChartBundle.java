/**
 *
 */
package com.visfresh.l12n;

import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.mpl.services.NotificationIssueBundle;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ChartBundle extends NotificationIssueBundle {
    private static final String BUNDLE_NAME = "chart";

    /**
     * Default constructor.
     */
    public ChartBundle() {
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
     * @param trackerEvent tracker event.
     * @return description for given alert.
     */
    public String buildDescription(final User user, final NotificationIssue issue,
            final TrackerEvent trackerEvent) {
        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, XmlControl.INSTANCE);
        final String str = bundle.getString(createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(user, issue, trackerEvent));
    }
    /**
     * @param user user.
     * @param event event.
     * @return
     */
    public String buildTrackerEventDescription(final User user, final TrackerEvent event) {
        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, XmlControl.INSTANCE);
        final String str = bundle.getString("TrackerEvent");
        return StringUtils.getMessage(str, createReplacementMap(user, null, event));
    }
}
