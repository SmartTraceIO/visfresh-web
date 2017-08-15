/**
 *
 */
package com.visfresh.io.json;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Location;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.NoteDto;
import com.visfresh.io.SingleShipmentInterimStop;
import com.visfresh.io.shipment.AlertDto;
import com.visfresh.io.shipment.AlertProfileDto;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.LocationProfileDto;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentAlert;
import com.visfresh.io.shipment.SingleShipmentDto;
import com.visfresh.io.shipment.SingleShipmentLocation;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentSerializer extends AbstractJsonSerializer {
    private TemperatureUnits tempUnits;
    private final NoteSerializer noteSerializer;
    private final LocationSerializer locationSerializer;

    /**
     * @param tz
     */
    public SingleShipmentSerializer(final User user) {
        super(user.getTimeZone());
        locationSerializer = new LocationSerializer(user.getTimeZone());
        noteSerializer = new NoteSerializer(user.getTimeZone());
        this.tempUnits = user.getTemperatureUnits();
    }
    /**
     * @param dto
     * @return
     */
    public JsonObject exportToViewData(final SingleShipmentDto dto) {
        return toJson(dto, true);
    }

    /**
     * @param dto
     * @param isNotSibling
     * @return
     */
    protected JsonObject toJson(final SingleShipmentDto dto,
            final boolean isNotSibling) {
        if (dto == null) {
            return  null;
        }

        final JsonObject json = new JsonObject();

        json.addProperty("shipmentId", dto.getShipmentId()); /*+*/
        json.addProperty(ShipmentConstants.DEVICE_SN, dto.getDeviceSN()); /*+*/
        json.addProperty(ShipmentConstants.DEVICE_COLOR, dto.getDeviceColor());
        if (isNotSibling) {
            json.addProperty(ShipmentConstants.DEVICE_NAME, dto.getDeviceName());
        }
        json.addProperty("tripCount", dto.getTripCount()); /*+*/

        if (isNotSibling) {
            json.addProperty("shipmentDescription", dto.getShipmentDescription());
            json.addProperty("palletId", dto.getPalletId());
            json.addProperty("assetNum", dto.getAssetNum());
            json.addProperty("assetType", dto.getAssetType());
            json.addProperty("status", dto.getStatus().name());
        }
        json.addProperty("isLatestShipment", dto.isLatestShipment());
        json.addProperty("trackerPositionFrontPercent", dto.getTrackerPositionFrontPercent()); /*+*/
        json.addProperty("trackerPositionLeftPercent", dto.getTrackerPositionLeftPercent()); /*+*/

        if (isNotSibling) {
            json.addProperty("alertProfileId", dto.getAlertProfileId());
            json.addProperty("alertProfileName", dto.getAlertProfileName());
            json.add("alertProfile", toJson(dto.getAlertProfile()));
            json.addProperty("alertSuppressionMinutes", dto.getAlertSuppressionMinutes());

            json.addProperty("alertPeopleToNotify", createPeopleToNotifyString(
                    dto.getAlertsNotificationSchedules()));

            //alertsNotificationSchedules
            final JsonArray array = new JsonArray();
            for (final ListNotificationScheduleItem item: dto.getAlertsNotificationSchedules()) {
                array.add(toJson(item));
            }
            json.add("alertsNotificationSchedules", array);
        }

        //alert summary
        json.add(ShipmentConstants.ALERT_SUMMARY, createAlertSummaryArray(dto.getAlertSummary())); /*+*/
        if (isNotSibling) {
            json.addProperty("alertYetToFire", dto.getAlertYetToFire());
            json.addProperty("alertFired", dto.getAlertFired());

            //"arrivalNotificationTimeISO": "2014-08-12 12:10",
            // NEW - ISO for actual time arrival notification sent out
            json.addProperty("arrivalNotificationTimeISO", dto.getArrivalNotificationTimeIso());
            json.addProperty("arrivalNotificationTime", dto.getArrivalNotificationTime());

            json.addProperty("arrivalNotificationWithinKm", dto.getArrivalNotificationWithinKm());
            json.addProperty("excludeNotificationsIfNoAlerts", dto.isExcludeNotificationsIfNoAlerts());

            json.addProperty("arrivalPeopleToNotify", createPeopleToNotifyString(
                    dto.getArrivalNotificationSchedules()));

            final JsonArray array = new JsonArray();
            for (final ListNotificationScheduleItem item: dto.getArrivalNotificationSchedules()) {
                array.add(toJson(item));
            }

            json.addProperty("commentsForReceiver", dto.getCommentsForReceiver());

            json.add("arrivalNotificationSchedules", array);
            json.addProperty("shutdownDeviceAfterMinutes", dto.getShutdownDeviceAfterMinutes());

            json.addProperty("noAlertsAfterArrivalMinutes", dto.getNoAlertsAfterArrivalMinutes());
            json.addProperty("shutDownAfterStartMinutes", dto.getShutDownAfterStartMinutes());

            json.addProperty("shutdownTimeISO", dto.getShutdownTimeIso());
            json.addProperty("shutdownTime", dto.getShutdownTime());

            json.addProperty("startLocation", dto.getStartLocation());
            json.addProperty("startTimeISO", dto.getStartTimeISO());
            json.addProperty("startTime", dto.getStartTime());
            json.add("startLocationForMap", toJson(dto.getStartLocationForMap()));

            json.addProperty("endLocation", dto.getEndLocation());
            json.addProperty("etaISO", dto.getEtaIso());
            json.addProperty("eta", dto.getEta());

            json.addProperty("arrivalTimeISO", dto.getArrivalTimeIso());
            json.addProperty("arrivalTime", dto.getArrivalTime());
            json.add("endLocationForMap", toJson(dto.getEndLocationForMap()));

            json.addProperty("lastReadingLocation", dto.getCurrentLocation());
            json.addProperty(ShipmentConstants.LAST_READING_TIME_ISO, dto.getLastReadingTimeIso());
            json.addProperty(ShipmentConstants.LAST_READING_TIME, dto.getLastReadingTime());
            json.addProperty(ShipmentConstants.LAST_READING_TEMPERATURE,
                    convertTemperature(dto.getLastReadingTemperature()));
            json.addProperty("batteryLevel", dto.getBatteryLevel());

            json.add("lastReadingForMap", toJson(dto.getCurrentLocationForMap()));

            json.addProperty("minTemp", convertTemperature(dto.getMinTemp()));
            json.addProperty("maxTemp", convertTemperature(dto.getMaxTemp()));
            json.addProperty("firstReadingTimeISO", dto.getTimeOfFirstReading());
            json.addProperty("firstReadingTime", dto.getFirstReadingTime());

            json.addProperty("alertsSuppressed", dto.isAlertsSuppressed());
            json.addProperty("alertsSuppressionTime", dto.getAlertsSuppressionTime());
            json.addProperty("alertsSuppressionTimeIso", dto.getAlertsSuppressionTimeIso());

            json.addProperty(ShipmentConstants.SEND_ARRIVAL_REPORT, dto.isSendArrivalReport());
            json.addProperty(ShipmentConstants.ARRIVAL_REPORT_ONLY_IF_ALERTS, dto.isSendArrivalReportOnlyIfAlerts());
            json.addProperty("arrivalReportSent", dto.isArrivalReportSent());

            final JsonArray locations = new JsonArray();
            for (final SingleShipmentLocation l : dto.getLocations()) {
                locations.add(toJson(l));
            }
            json.add("locations", locations);

            final JsonArray siblings = new JsonArray();
            for (final SingleShipmentDto sibling: dto.getSiblings()) {
                siblings.add(toJson(sibling, false));
            }

            json.add("siblings", siblings);

            //alternatives
            json.add("startLocationAlternatives", locationsToJson(dto.getStartLocationAlternatives()));
            json.add(ShipmentConstants.END_LOCATION_ALTERNATIVES, locationsToJson(dto.getEndLocationAlternatives()));
            json.add(ShipmentConstants.INTERIM_LOCATIONS, locationsToJson(dto.getInterimLocationAlternatives()));

            //interim stops
            json.add(ShipmentConstants.INTERIM_STOPS, interimStopsToJson(dto.getInterimStops()));

            //add notes
            final JsonArray notes = new JsonArray();
            for (final NoteDto n : dto.getNotes()) {
                notes.add(noteSerializer.toJson(n));
            }
            json.add("notes", notes);
        }

        //add device groups
        final JsonArray deviceGroups = new JsonArray();
        json.add("deviceGroups", deviceGroups);
        for (final DeviceGroupDto grp : dto.getDeviceGroups()) {
            deviceGroups.add(toJson(grp));
        }

        //company access
        final JsonArray userAccess = new JsonArray();
        json.add("userAccess", userAccess);
        for (final ShipmentUserDto u : dto.getUserAccess()) {
            final JsonObject cobj = new JsonObject();
            cobj.addProperty("userId", u.getId());
            cobj.addProperty("email", u.getEmail());
            userAccess.add(cobj);
        }

        //company access
        final JsonArray companyAccess = new JsonArray();
        json.add("companyAccess", companyAccess);
        for (final ShipmentCompanyDto c : dto.getCompanyAccess()) {
            final JsonObject cobj = new JsonObject();
            cobj.addProperty("companyId", c.getId());
            cobj.addProperty("companyName", c.getName());
            companyAccess.add(cobj);
        }

        //sent alerts
        final JsonArray alertsWithCorrectiveActions = new JsonArray();
        for (final AlertDto a : dto.getSentAlerts()) {
            alertsWithCorrectiveActions.add(toJson(a));
        }
        json.add("alertsWithCorrectiveActions", alertsWithCorrectiveActions);

        return json;
    }
    /**
     * @param alert
     * @return
     */
    private JsonObject toJson(final AlertProfileDto alert) {
        if (alert == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        //alertProfileId, alertProfileName, alertProfileDescription, highTemperature, criticalHighTemperature, lowTemperature, criticalHighTemperature, watchEnterBrightEnvironment, watchEnterDarkEnvironment, watchMovementStart
        obj.addProperty(AlertProfileConstants.ALERT_PROFILE_ID, alert.getId());
        obj.addProperty(AlertProfileConstants.ALERT_PROFILE_NAME, alert.getName());
        obj.addProperty(AlertProfileConstants.ALERT_PROFILE_DESCRIPTION, alert.getDescription());

        obj.addProperty(AlertProfileConstants.WATCH_BATTERY_LOW,
                alert.isWatchBatteryLow());
        obj.addProperty(AlertProfileConstants.WATCH_ENTER_BRIGHT_ENVIRONMENT,
                alert.isWatchEnterBrightEnvironment());
        obj.addProperty(AlertProfileConstants.WATCH_ENTER_DARK_ENVIRONMENT,
                alert.isWatchEnterDarkEnvironment());
        obj.addProperty(AlertProfileConstants.WATCH_MOVEMENT_START,
                alert.isWatchMovementStart());
        obj.addProperty(AlertProfileConstants.WATCH_MOVEMENT_STOP,
                alert.isWatchMovementStop());
        obj.addProperty(AlertProfileConstants.LOWER_TEMPERATURE_LIMIT,
                LocalizationUtils.convertToUnits(alert.getLowerTemperatureLimit(), tempUnits));
        obj.addProperty(AlertProfileConstants.UPPER_TEMPERATURE_LIMIT,
                LocalizationUtils.convertToUnits(alert.getUpperTemperatureLimit(), tempUnits));

        return obj;
    }

    /**
     * @param item list notification schedule item.
     * @return
     */
    private JsonObject toJson(final ListNotificationScheduleItem item) {
        if (item == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("notificationScheduleId", item.getNotificationScheduleId());
        json.addProperty("notificationScheduleName", item.getNotificationScheduleName());
        return json;
    }

    /**
     * @param l location.
     * @return
     */
    private JsonObject toJson(final SingleShipmentLocation l) {
        if (l == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("lat", l.getLatitude());
        json.addProperty("long", l.getLongitude());
        json.addProperty("temperature", convertTemperature(l.getTemperature()));
        json.addProperty("timeISO", l.getTimeIso());
        json.addProperty("time", l.getTime());
        json.addProperty("type", l.getType());

        final JsonArray alerts = new JsonArray();
        for (final SingleShipmentAlert alert : l.getAlerts()) {
            alerts.add(toJson(alert));
        }
        json.add("alerts", alerts);

        return json;
    }

    /**
     * @param alert
     * @return
     */
    private JsonObject toJson(final SingleShipmentAlert alert) {
        if (alert == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("title", alert.getTitle());

        //add lines
        int i = 1;
        for (final String line : alert.getLines()) {
            json.addProperty("Line" + i, line);
            i++;
        }
        json.addProperty("type", alert.getType());
        return json;
    }
    /**
     * @param summary
     * @return
     */
    private JsonArray createAlertSummaryArray(final Set<AlertType> summary) {
        final JsonArray array = new JsonArray();
        final List<AlertType> list = new LinkedList<>(summary);
        Collections.sort(list);

        for (final AlertType t : list) {
            array.add(new JsonPrimitive(t.name()));
        }
        return array;
    }
    /**
     * @param locs locations.
     * @return JSON array of locations.
     */
    private JsonArray locationsToJson(final List<LocationProfileDto> locs) {
        final JsonArray array = new JsonArray();
        for (final LocationProfileDto loc : locs) {
            array.add(toJson(loc));
        }
        return array;
    }
    /**
     * @param interimStops
     * @return
     */
    private JsonArray interimStopsToJson(final List<SingleShipmentInterimStop> interimStops) {
        final JsonArray array = new JsonArray();
        for (final SingleShipmentInterimStop stop : interimStops) {
            array.add(toJson(stop));
        }
        return array;
    }
    /**
     * @param stop
     * @return
     */
    private JsonObject toJson(final SingleShipmentInterimStop stop) {
        if (stop == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("id", stop.getId());
        json.addProperty("latitude", stop.getLatitude());
        json.addProperty("longitude", stop.getLongitude());
        json.addProperty("time", stop.getTime());
        json.addProperty("stopDate", stop.getStopDate());
        json.addProperty("stopDateISO", stop.getStopDateIso());
        json.add("location", toJson(stop.getLocation()));
        return json;
    }
    /**
     * @param grp
     * @return
     */
    private JsonObject toJson(final DeviceGroupDto grp) {
        if (grp == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("groupId", grp.getId());
        json.addProperty("name", grp.getName());
        json.addProperty("description", grp.getDescription());

        return json;
    }
    /**
     * @param a alert DTO.
     * @return JSON object.
     */
    private JsonObject toJson(final AlertDto a) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", a.getId());
        json.addProperty("description", a.getDescription());
        json.addProperty("time", a.getTime());
        json.addProperty("timeISO", a.getTimeISO());
        json.addProperty("correctiveActionListId", a.getCorrectiveActionListId());
        json.addProperty("type", a.getType().toString());
        return json;
    }
    /**
     * @param location
     * @return
     */
    private JsonElement toJson(final Location location) {
        if (location == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("latitude", location.getLatitude());
        json.addProperty("longitude", location.getLongitude());

        return json;
    }
    /**
     * @param sched
     * @return
     */
    private String createPeopleToNotifyString(final List<ListNotificationScheduleItem> sched) {
        final List<String> list = new LinkedList<>();
        for (final ListNotificationScheduleItem s : sched) {
            list.add(s.getPeopleToNotify());
        }
        return StringUtils.combine(list, ", ");
    }
    /**
     * @param t
     * @return
     */
    protected double convertTemperature(final double t) {
        double value = tempUnits == TemperatureUnits.Fahrenheit ? t * 1.8 + 32 : t;
        //cut extra decimal signs.
        value = Math.round(value * 100) / 100.;
        return value;
    }
    /**
     * @param location
     * @return
     */
    protected JsonElement toJson(final LocationProfileDto location) {
        return locationSerializer.toJson(location);
    }
}
