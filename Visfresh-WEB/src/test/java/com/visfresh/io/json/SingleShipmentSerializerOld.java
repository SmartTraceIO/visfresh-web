/**
 *
 */
package com.visfresh.io.json;

import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.impl.json.SingleShipmentBeanSerializer;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.io.NoteDto;
import com.visfresh.io.SingleShipmentInterimStop;
import com.visfresh.io.shipment.AlertDto;
import com.visfresh.io.shipment.AlertProfileDto;
import com.visfresh.io.shipment.SingleShipmentAlert;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentDto;
import com.visfresh.io.shipment.SingleShipmentLocation;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentSerializerOld extends SingleShipmentBeanSerializer {
    protected TemperatureUnits tempUnits;
    private final NoteSerializer noteSerializer;

    public SingleShipmentSerializerOld(final Language lang, final TimeZone tz, final TemperatureUnits units) {
        super(tz, lang, units);
        this.tempUnits = units;
        noteSerializer = new NoteSerializer(tz);
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
        json.addProperty("isBeacon", dto.isBeacon());
        json.addProperty(ShipmentConstants.DEVICE_SN, dto.getDeviceSN()); /*+*/
        json.addProperty(ShipmentConstants.DEVICE_COLOR, dto.getDeviceColor());
        if (isNotSibling) {
            json.addProperty(ShipmentConstants.DEVICE_NAME, dto.getDeviceName());
        }
        json.addProperty("tripCount", dto.getTripCount()); /*+*/
        if (dto.getNearestTracker() != null) {
            final JsonObject nt = new JsonObject();
            json.add("nearestTracker", nt);

            nt.addProperty("device", dto.getNearestTracker());
            nt.addProperty("sn", Device.getSerialNumber(dto.getNearestTracker()));
            nt.addProperty("color", dto.getNearestTrackerColor());
        }

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
            json.addProperty("firstReadingTimeISO", dto.getFirstReadingTimeIso());
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
            json.add("notes", notesToJson(dto.getNotes()));
        }

        //add device groups
        json.add("deviceGroups", deviceGroupsToJson(dto.getDeviceGroups()));

        //company access
        json.add("userAccess", userAcessToJson(dto.getUserAccess()));

        //company access
        json.add("companyAccess", companyAccessToJson(dto.getCompanyAccess()));

        //sent alerts
        json.add("alertsWithCorrectiveActions", sentAlertsToJson(dto.getAlertsWithCorrectiveActions()));

        return json;
    }
    /**
     * @param alert
     * @return
     */
    @Override
    protected JsonObject toJson(final AlertProfileDto alert) {
        if (alert == null) {
            return null;
        }

        final JsonObject obj = super.toJson(alert);
        obj.addProperty(AlertProfileConstants.LOWER_TEMPERATURE_LIMIT,
                LocalizationUtils.convertToUnits(alert.getLowerTemperatureLimit(), tempUnits));
        obj.addProperty(AlertProfileConstants.UPPER_TEMPERATURE_LIMIT,
                LocalizationUtils.convertToUnits(alert.getUpperTemperatureLimit(), tempUnits));

        return obj;
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
        json.addProperty("humidity", l.getHumidity());
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
        json.addProperty("stopDate", formatPretty(stop.getStopDate()));
        json.addProperty("stopDateISO", formatIso(stop.getStopDate()));
        json.add("location", toJson(stop.getLocation()));
        return json;
    }

    /**
     * @param sentAlerts
     * @return
     */
    protected JsonArray sentAlertsToJson(final List<AlertDto> sentAlerts) {
        final JsonArray array = new JsonArray();
        for (final AlertDto a : sentAlerts) {
            array.add(toJson(a));
        }
        return array;
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
     * @param s
     * @return
     */
    protected JsonArray notesToJson(final List<NoteDto> notes) {
        final JsonArray array = new JsonArray();
        for (final NoteDto note : notes) {
            array.add(noteSerializer.toJson(note));
        }
        return array;
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
     * @param s single shipment data.
     * @return
     */
    @Override
    public JsonObject exportToViewData(final SingleShipmentData s) {
        throw new InternalError("Should implement");
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
}
