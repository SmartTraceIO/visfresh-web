/**
 *
 */
package com.visfresh.controllers;

import static com.visfresh.utils.DateTimeUtils.createDateFormat;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.entities.Language;
import com.visfresh.entities.MeasurementUnits;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.TimeZoneService;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Utilities")
@RequestMapping("/rest")
public class UtilitiesController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(UtilitiesController.class);
    @Autowired
    private TimeZoneService timeZoneService;

    /**
     * Default constructor.
     */
    public UtilitiesController() {
        super();
    }
    @RequestMapping(value = "/getTimeZones/{authToken}", method = RequestMethod.GET)
    public JsonObject getTimeZones(@PathVariable final String authToken) {
        try {
            getLoggedInUser(authToken);

            final JsonArray array = new JsonArray();
            //add other available time zones
            final List<TimeZone> timeZones = timeZoneService.getSupportedTimeZones();
            Collections.sort(timeZones, createTimeZoneComparator());

            for (final TimeZone tz : timeZones) {
                final JsonObject obj = createTimeZoneElement(tz);
                array.add(obj);
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to list time zones", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @return
     */
    protected Comparator<TimeZone> createTimeZoneComparator() {
        return new Comparator<TimeZone>() {
            @Override
            public int compare(final TimeZone a, final TimeZone b) {
                //check UTC
                if (a.getID().equals("UTC")) {
                    return -1;
                } else if (b.getID().equals("UTC")) {
                    return 1;
                }

                //not UTC
                int result = 0;
                if (result == 0) {
                    final String sega = getFirstSegment(a.getID());
                    final String segb = getFirstSegment(b.getID());
                    result = sega.compareTo(segb);
                }
                if (result == 0) {
                    result = new Integer(a.getRawOffset()).compareTo(b.getRawOffset());
                }
                if (result == 0) {
                    result = a.getID().compareTo(b.getID());
                }
                return result;
            }
            /**
             * @param id
             * @return
             */
            private String getFirstSegment(final String id) {
                return id.split(Pattern.quote("/"))[0];
            }
        };
    }
    /**
     * @param tz
     * @return
     */
    private JsonObject createTimeZoneElement(final TimeZone tz) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("id", tz.getID());
        obj.addProperty("displayName", tz.getDisplayName());
        obj.addProperty("offset", createOffsetString(tz.getRawOffset()));
        return obj;
    }
    private static String createOffsetString(final int rawOffset) {
        final long hours = TimeUnit.MILLISECONDS.toHours(rawOffset);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(rawOffset)
                - TimeUnit.HOURS.toMinutes(hours);
        // avoid -4:-30 issue
        minutes = Math.abs(minutes);

        String result;
        if (hours >= 0) {
            result = String.format("GMT+%d:%02d", hours, minutes);
        } else {
            result = String.format("GMT%d:%02d", hours, minutes);
        }

        return result;
    }

    @RequestMapping(value = "/getLanguages/{authToken}", method = RequestMethod.GET)
    public JsonObject getLanguages(@PathVariable final String authToken) {
        try {
            getLoggedInUser(authToken);

            final JsonArray array = new JsonArray();
            for (final Language lang : Language.values()) {
                array.add(new JsonPrimitive(lang.toString()));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to list languages", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/getMeasurementUnits/{authToken}", method = RequestMethod.GET)
    public JsonObject getMeasurementUnits(@PathVariable final String authToken) {
        try {
            getLoggedInUser(authToken);

            final JsonArray array = new JsonArray();
            for (final MeasurementUnits units : MeasurementUnits.values()) {
                array.add(new JsonPrimitive(units.toString()));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to list measurement units", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/getRoles/{authToken}", method = RequestMethod.GET)
    public JsonObject getRoles(@PathVariable final String authToken) {
        try {
            getLoggedInUser(authToken);
//            final TODO

            final JsonArray array = new JsonArray();
            for (final Role roles : Role.values()) {
                if (roles != Role.GlobalAdmin) {
                    array.add(new JsonPrimitive(roles.toString()));
                }
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to list roles", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/getUserTime/{authToken}", method = RequestMethod.GET)
    public JsonObject getUserTime(@PathVariable final String authToken) {
        try {
            final User user = getLoggedInUser(authToken);
            final TimeZone tz = user.getTimeZone();
            final Date date = new Date();

            final JsonObject json = new JsonObject();
            json.addProperty("dateTimeIso", createDateFormat(user, "yyyy-MM-dd'T'HH:mm").format(date));
            // formattedDateTimeIso: "9-Dec-2015 1:16PM (UTC)"
            json.addProperty("formattedDateTimeIso",
                    createDateFormat(user, "d-MMM-yyyy h:mmaa '('zzz')'").format(date));
            // dateTimeString: "24-Nov-15 9:42am"
            json.addProperty("dateTimeString", createDateFormat(user, "d-MMM-yyyy h:mmaa").format(date));
            // dateString: "24-Nov-15"
            json.addProperty("dateString", createDateFormat(user, "d-MMM-yyyy").format(date));
            // timeString: "24-Nov-15"
            json.addProperty("timeString", createDateFormat(user, "h:mmaa").format(date));
            // v24: "16:33"
            json.addProperty("timeString24", createDateFormat(user, "HH:mm").format(date));
            json.addProperty("timeZoneId", tz.getID());
            json.addProperty("timeZoneString", tz.getDisplayName());
            return createSuccessResponse(json);
        } catch (final Exception e) {
            log.error("Failed to list roles", e);
            return createErrorResponse(e);
        }
    }
}
