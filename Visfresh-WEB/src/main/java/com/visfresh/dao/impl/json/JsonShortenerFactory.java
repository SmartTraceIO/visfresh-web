/**
 *
 */
package com.visfresh.dao.impl.json;

import java.util.HashMap;
import java.util.Map;

import com.visfresh.io.json.DefaultJsonShortener;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JsonShortenerFactory {
    public static final Integer DEFAULT_VERSION = new Integer(1);

    /**
     * Default constructor.
     */
    public JsonShortenerFactory() {
        super();
    }

    /**
     * @param version shortener version.
     * @return shortener.
     */
    public JsonShortener createDefaultShortener(final Integer version) {
        final Map<String, String> aliases = createAliases(version);
        final DefaultJsonShortener s = new DefaultJsonShortener();
        s.getAliases().putAll(aliases);
        return s;
    }
    /**
     * @param version shortener version.
     * @return shortener.
     */
    public JsonShortener createDefaultShortener() {
        return createDefaultShortener(DEFAULT_VERSION);
    }

    /**
     * @param version shortener version.
     * @return aliases map.
     */
    private Map<String, String> createAliases(final Integer version) {
        final Map<String, String> aliases = new HashMap<>();
        if (DEFAULT_VERSION.equals(version)) {
            addForVersion1(aliases);
        }
        return aliases;
    }

    /**
     * @param aliases
     */
    protected void addForVersion1(final Map<String, String> aliases) {
        aliases.put("shipmentId", "0");
        aliases.put("companyId", "1");
        aliases.put("device", "2");
        aliases.put("deviceName", "3");
        aliases.put("tripCount", "4");
        aliases.put("shipmentDescription", "5");
        aliases.put("palletId", "6");
        aliases.put("assetNum", "7");
        aliases.put("assetType", "8");
        aliases.put("status", "9");
        aliases.put("alertSuppressionMinutes", "a");
        aliases.put("alertsNotificationSchedules", "b");
        aliases.put("commentsForReceiver", "c");
        aliases.put("arrivalNotificationWithinKm", "d");
        aliases.put("excludeNotificationsIfNoAlerts", "e");
        aliases.put("arrivalNotificationSchedules", "f");
        aliases.put("sendArrivalReport", "g");
        aliases.put("sendArrivalReportOnlyIfAlerts", "h");
        aliases.put("shutdownDeviceAfterMinutes", "i");
        aliases.put("noAlertsAfterArrivalMinutes", "j");
        aliases.put("shutDownAfterStartMinutes", "k");
        aliases.put("startLocation", "l");
        aliases.put("startTime", "m");
        aliases.put("endLocation", "n");
        aliases.put("eta", "o");
        aliases.put("currentLocation", "p");
        aliases.put("currentLocationDescription", "q");
        aliases.put("percentageComplete", "r");
        aliases.put("minTemp", "s");
        aliases.put("maxTem", "t");
        aliases.put("timeOfFirstReading", "u");//TODO delete
        aliases.put("siblings", "v");
        aliases.put("alertYetToFire", "w");
        aliases.put("alertFired", "x");
        aliases.put("arrival", "y");
        aliases.put("shutdownTime", "z");
        aliases.put("arrivalTime", "A");
        aliases.put("alertsSuppressed", "B");
        aliases.put("alertsSuppressionTime", "C");
        aliases.put("firstReadingTime", "D");
        aliases.put("lastReadingTime", "E");
        aliases.put("lastReadingTemperature", "F");
        aliases.put("batteryLevel", "G");
        aliases.put("noAlertsAfterStartMinutes", "H");
        aliases.put("shipmentType", "I");
        aliases.put("startLocationAlternatives", "J");
        aliases.put("endLocationAlternatives", "K");
        aliases.put("interimLocationAlternatives", "L");
        aliases.put("interimStops", "M");
        aliases.put("notes", "N");
        aliases.put("deviceGroups", "O");
        aliases.put("deviceColor", "P");
        aliases.put("isLatestShipment", "Q");
        aliases.put("arrivalReportSent", "R");
        aliases.put("userAccess", "S");
        aliases.put("companyAccess", "T");
        aliases.put("sentAlerts", "U");
        aliases.put("alertProfile", "V");
        aliases.put("companyName", "W");
        aliases.put("locationId", "X");
        aliases.put("locationName", "Y");
        aliases.put("address", "Z");
        aliases.put("location", "10");
        aliases.put("radiusMeters", "11");
        aliases.put("startFlag", "12");
        aliases.put("interimFlag", "13");
        aliases.put("endFlag", "14");
        aliases.put("lat", "15");
        aliases.put("lon", "16");
        aliases.put("noteText", "17");
        aliases.put("timeOnChart", "18");
        aliases.put("noteType", "19");
        aliases.put("noteNum", "1a");
        aliases.put("creationDate", "1b");
        aliases.put("createdBy", "1c");
        aliases.put("active", "1d");
        aliases.put("createCreatedByName", "1e");
        aliases.put("id", "1f");
        aliases.put("date", "1g");
        aliases.put("trackerEventId", "1h");
        aliases.put("mettersForArrival", "1i");
        aliases.put("notifiedAt", "1j");
        aliases.put("alertProfileId", "1k");
        aliases.put("alertProfileName", "1l");
        aliases.put("alertProfileDescription", "1m");
        aliases.put("watchBatteryLow", "1n");
        aliases.put("watchEnterBrightEnvironment", "1o");
        aliases.put("watchEnterDarkEnvironment", "1p");
        aliases.put("watchMovementStart", "1q");
        aliases.put("watchMovementStop", "1r");
        aliases.put("lowerTemperatureLimit", "1s");
        aliases.put("upperTemperatureLimit", "1t");
        aliases.put("lightOnCorrectiveActions", "1u");
        aliases.put("batteryLowCorrectiveActions", "1v");
        aliases.put("groupId", "1w");
        aliases.put("name", "1x");
        aliases.put("description", "1y");
        aliases.put("notificationScheduleId", "1z");
        aliases.put("notificationScheduleName", "1A");
        aliases.put("notificationScheduleDescription", "1B");
        aliases.put("peopleToNotify", "1C");
        aliases.put("type", "1D");
        aliases.put("temperature", "1E");
        aliases.put("minutes", "1F");
        aliases.put("cumulative", "1G");
        aliases.put("ruleId", "1H");
        aliases.put("userId", "1I");
        aliases.put("email", "1J");
        aliases.put("latitude", "1K");
        aliases.put("longitude", "1L");
        aliases.put("time", "1M");
        aliases.put("stopDate", "1N");
        aliases.put("actions", "1O");
        aliases.put("action", "1P");
        aliases.put("requestVerification", "1Q");
    }
}
