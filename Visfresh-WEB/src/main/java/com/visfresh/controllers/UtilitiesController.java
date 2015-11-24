/**
 *
 */
package com.visfresh.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            for (final String tzId : TimeZone.getAvailableIDs()) {
                final TimeZone tz = TimeZone.getTimeZone(tzId);

                final JsonObject obj = new JsonObject();
                array.add(obj);

                obj.addProperty("id", tz.getID());
                obj.addProperty("displayName", tz.getDisplayName());
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to list time zones", e);
            return createErrorResponse(e);
        }
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

            final JsonArray array = new JsonArray();
            for (final Role units : Role.values()) {
                array.add(new JsonPrimitive(units.toString()));
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
            json.addProperty("dateTimeIso", createDateFormat("yyyy-MM-dd'T'HH:mm", tz).format(date));
//          formattedDateTimeIso: "04 Dec 2015 4:33pm"
            json.addProperty("formattedDateTimeIso",
                    createDateFormat("d MMM yyyy hh:mmaa", tz).format(date));
//          dateTimeString: "04 Dec 2015"
            json.addProperty("dateTimeString", createDateFormat("d MMM yyyy", tz).format(date));
//          dateString: "04:33am"
            json.addProperty("dateString", createDateFormat("hh:mmaa", tz).format(date));
//          dateString24: "16:33"
            json.addProperty("dateString24", createDateFormat("HH:mm", tz).format(date));
            json.addProperty("timeZoneId", tz.getID());
            json.addProperty("timeZoneString", tz.getDisplayName());

            return createSuccessResponse(json);
        } catch (final Exception e) {
            log.error("Failed to list roles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param format
     * @param tz
     * @return
     */
    private DateFormat createDateFormat(final String format, final TimeZone tz) {
        final DateFormat fmt = new SimpleDateFormat(format, Locale.ENGLISH);
        fmt.setTimeZone(tz);
        return fmt;
    }
}
