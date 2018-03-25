/**
 *
 */
package com.visfresh.l12n;

import java.util.ResourceBundle;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Language;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.io.TrackerEventDto;
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
    public String buildDescription(final NotificationIssue issue,
            final TrackerEvent trackerEvent, final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, XmlControl.INSTANCE);
        final String str = bundle.getString(createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, trackerEvent, lang, tz, tu));
    }
    /**
     * @param user user.
     * @param event event.
     * @return
     */
    public String buildTrackerEventDescription(final TrackerEvent event,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, XmlControl.INSTANCE);
        final String str = bundle.getString("TrackerEvent");
        return StringUtils.getMessage(str, createReplacementMap(null, event, lang, tz, tu));
    }

    /**
     * @param alert alert.
     * @param dto tracker event DTO.
     * @param language language.
     * @param timeZone time zone.
     * @param temperatureUnits temperature units.
     * @return
     */
    public String buildDescription(final Alert alert, final TrackerEventDto dto,
            final Language language, final TimeZone timeZone,
            final TemperatureUnits temperatureUnits) {
        final TrackerEvent e = new TrackerEvent();
        e.setId(dto.getId());
        e.setBeaconId(dto.getBeaconId());
        e.setBattery(dto.getBattery());
        e.setDevice(alert.getDevice());
        e.setLatitude(dto.getLatitude());
        e.setLongitude(dto.getLongitude());
        e.setShipment(alert.getShipment());
        e.setTemperature(dto.getTemperature());
        e.setTime(dto.getTime());
        e.setType(dto.getType());

        return buildDescription(alert, e, language, timeZone, temperatureUnits);
    }
}
