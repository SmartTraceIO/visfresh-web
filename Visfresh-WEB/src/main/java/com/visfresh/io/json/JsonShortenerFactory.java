/**
 *
 */
package com.visfresh.io.json;

import java.util.HashMap;
import java.util.Map;

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
        aliases.put("shipmentId", "a");
        aliases.put("companyId", "a1");
        aliases.put("device", "a2");
        aliases.put("deviceName", "a3");
        aliases.put("tripCount", "a4");
        aliases.put("shipmentDescription", "a5");
        aliases.put("palletId", "a6");
        aliases.put("assetNum", "a7");
        aliases.put("assetType", "a8");
        aliases.put("status", "a9");
        aliases.put("alertSuppressionMinutes", "aa");
        aliases.put("alertsNotificationSchedules", "ab");
        aliases.put("commentsForReceiver", "ac");
        aliases.put("arrivalNotificationWithinKm", "ad");
        aliases.put("excludeNotificationsIfNoAlerts", "ae");
        aliases.put("arrivalNotificationSchedules", "af");
        aliases.put("sendArrivalReport", "ag");
        aliases.put("sendArrivalReportOnlyIfAlerts", "ah");
        aliases.put("shutdownDeviceAfterMinutes", "ai");
        aliases.put("noAlertsAfterArrivalMinutes", "aj");
        aliases.put("shutDownAfterStartMinutes", "ak");
        aliases.put("startLocation", "al");
        aliases.put("startTime", "am");
        aliases.put("endLocation", "an");
        aliases.put("eta", "ao");
        aliases.put("currentLocation", "ap");
        aliases.put("currentLocationDescription", "aq");
        aliases.put("percentageComplete", "ar");
        aliases.put("minTemp", "as");
        aliases.put("maxTem", "at");
        aliases.put("timeOfFirstReading", "au");
        aliases.put("siblings", "av");
        aliases.put("alertYetToFire", "aw");
        aliases.put("alertFired", "ax");
        aliases.put("arrivalNotificationTime", "ay");
        aliases.put("shutdownTime", "az");
        aliases.put("arrivalTime", "aA");
        aliases.put("alertsSuppressed", "aB");
        aliases.put("alertsSuppressionTime", "aC");
        aliases.put("firstReadingTime", "aD");
        aliases.put("lastReadingTime", "aE");
        aliases.put("lastReadingTemperature", "aF");
        aliases.put("batteryLevel", "aG");
        aliases.put("noAlertsAfterStartMinutes", "aH");
        aliases.put("shipmentType", "aI");
        aliases.put("startLocationAlternatives", "aJ");
        aliases.put("endLocationAlternatives", "aK");
        aliases.put("interimLocationAlternatives", "aL");
        aliases.put("interimStops", "aM");
        aliases.put("notes", "aN");
        aliases.put("deviceGroups", "aO");
        aliases.put("deviceColor", "aP");
        aliases.put("isLatestShipment", "aQ");
        aliases.put("arrivalReportSent", "aR");
        aliases.put("userAccess", "aS");
        aliases.put("companyAccess", "aT");
        aliases.put("sentAlerts", "aU");
        aliases.put("alertProfile", "aV");
        aliases.put("companyName", "aW");
        aliases.put("locationId", "aX");
        aliases.put("locationName", "aY");
        aliases.put("address", "aZ");
        aliases.put("location", "a01");
        aliases.put("radiusMeters", "a11");
        aliases.put("startFlag", "a21");
        aliases.put("interimFlag", "a31");
        aliases.put("endFlag", "a41");
        aliases.put("lat", "a51");
        aliases.put("lon", "a61");
        aliases.put("noteText", "a71");
        aliases.put("timeOnChart", "a81");
        aliases.put("noteType", "a91");
        aliases.put("noteNum", "aa1");
        aliases.put("creationDate", "ab1");
        aliases.put("createdBy", "ac1");
        aliases.put("active", "ad1");
        aliases.put("createCreatedByName", "ae1");
        aliases.put("alertProfileId", "af1");
        aliases.put("alertProfileName", "ag1");
        aliases.put("alertProfileDescription", "ah1");
        aliases.put("watchBatteryLow", "ai1");
        aliases.put("watchEnterBrightEnvironment", "aj1");
        aliases.put("watchEnterDarkEnvironment", "ak1");
        aliases.put("watchMovementStart", "al1");
        aliases.put("watchMovementStop", "am1");
        aliases.put("lowerTemperatureLimit", "an1");
        aliases.put("upperTemperatureLimit", "ao1");
        aliases.put("groupId", "ap1");
        aliases.put("name", "aq1");
        aliases.put("description", "ar1");
        aliases.put("notificationScheduleId", "as1");
        aliases.put("notificationScheduleName", "at1");
        aliases.put("notificationScheduleDescription", "au1");
        aliases.put("peopleToNotify", "av1");
        aliases.put("id", "aw1");
        aliases.put("date", "ax1");
        aliases.put("trackerEventId", "ay1");
        aliases.put("type", "az1");
        aliases.put("temperature", "aA1");
        aliases.put("minutes", "aB1");
        aliases.put("cumulative", "aC1");
        aliases.put("ruleId", "aD1");
        aliases.put("userId", "aE1");
        aliases.put("email", "aF1");
        aliases.put("latitude", "aG1");
        aliases.put("longitude", "aH1");
        aliases.put("time", "aI1");
        aliases.put("stopDate", "aJ1");
    }
}
