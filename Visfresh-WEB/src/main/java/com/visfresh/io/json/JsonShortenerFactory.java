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
        aliases.put("locations", "av");
        aliases.put("siblings", "aw");
        aliases.put("alertSummary", "ax");
        aliases.put("alertYetToFire", "ay");
        aliases.put("alertFired", "az");
        aliases.put("arrivalNotificationTime", "aA");
        aliases.put("shutdownTime", "aB");
        aliases.put("arrivalTime", "aC");
        aliases.put("alertsSuppressed", "aD");
        aliases.put("alertsSuppressionTime", "aE");
        aliases.put("firstReadingTime", "aF");
        aliases.put("lastReadingTime", "aG");
        aliases.put("lastReadingTemperature", "aH");
        aliases.put("batteryLevel", "aI");
        aliases.put("noAlertsAfterStartMinutes", "aJ");
        aliases.put("shipmentType", "aK");
        aliases.put("startLocationAlternatives", "aL");
        aliases.put("endLocationAlternatives", "aM");
        aliases.put("interimLocationAlternatives", "aN");
        aliases.put("interimStops", "aO");
        aliases.put("notes", "aP");
        aliases.put("deviceGroups", "aQ");
        aliases.put("deviceColor", "aR");
        aliases.put("isLatestShipment", "aS");
        aliases.put("arrivalReportSent", "aT");
        aliases.put("userAccess", "aU");
        aliases.put("companyAccess", "aV");
        aliases.put("sentAlerts", "aW");
        aliases.put("alertProfile", "aX");
        aliases.put("companyName", "aY");
        aliases.put("locationId", "aZ");
        aliases.put("locationName", "a01");
        aliases.put("address", "a11");
        aliases.put("location", "a21");
        aliases.put("radiusMeters", "a31");
        aliases.put("startFlag", "a41");
        aliases.put("interimFlag", "a51");
        aliases.put("endFlag", "a61");
        aliases.put("lat", "a71");
        aliases.put("lon", "a81");
        aliases.put("noteText", "a91");
        aliases.put("timeOnChart", "aa1");
        aliases.put("noteType", "ab1");
        aliases.put("noteNum", "ac1");
        aliases.put("creationDate", "ad1");
        aliases.put("createdBy", "ae1");
        aliases.put("active", "af1");
        aliases.put("createCreatedByName", "ag1");
        aliases.put("alertProfileId", "ah1");
        aliases.put("alertProfileName", "ai1");
        aliases.put("alertProfileDescription", "aj1");
        aliases.put("watchBatteryLow", "ak1");
        aliases.put("watchEnterBrightEnvironment", "al1");
        aliases.put("watchEnterDarkEnvironment", "am1");
        aliases.put("watchMovementStart", "an1");
        aliases.put("watchMovementStop", "ao1");
        aliases.put("lowerTemperatureLimit", "ap1");
        aliases.put("upperTemperatureLimit", "aq1");
        aliases.put("latitude", "ar1");
        aliases.put("longitude", "as1");
        aliases.put("temperature", "at1");
        aliases.put("time", "au1");
        aliases.put("alerts", "av1");
        aliases.put("type", "aw1");
        aliases.put("id", "ax1");
        aliases.put("date", "ay1");
        aliases.put("trackerEventId", "az1");
        aliases.put("groupId", "aA1");
        aliases.put("name", "aB1");
        aliases.put("description", "aC1");
        aliases.put("notificationScheduleId", "aD1");
        aliases.put("notificationScheduleName", "aE1");
        aliases.put("notificationScheduleDescription", "aF1");
        aliases.put("peopleToNotify", "aG1");
        aliases.put("minutes", "aH1");
        aliases.put("cumulative", "aI1");
        aliases.put("ruleId", "aJ1");
        aliases.put("userId", "aK1");
        aliases.put("email", "aL1");
        aliases.put("stopDate", "aM1");
    }
}
