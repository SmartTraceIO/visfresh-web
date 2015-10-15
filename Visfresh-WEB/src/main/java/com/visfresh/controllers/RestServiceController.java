/**
 *
 */
package com.visfresh.controllers;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.io.JSonFactory;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthToken;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.RestService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("rest")
@RequestMapping("/rest")
@ComponentScan(basePackageClasses = {AuthService.class})
public class RestServiceController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RestServiceController.class);
    /**
     * Authentication service.
     */
    @Autowired
    private AuthService authService;
    /**
     * REST service.
     */
    @Autowired
    private RestService restService;
    /**
     * JSON converter.
     */
    @Autowired
    private JSonFactory jsonFactory;

    /**
     * Default constructor.
     */
    public RestServiceController() {
        super();
    }

    //authentication
    /**
     * @param login login
     * @param password password.
     * @return authorization token.
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody String login(@RequestBody final String loginRequest) {
        String login = null;

        try {
            final JsonObject json = getJSonObject(loginRequest);
            login = json.get("login").getAsString();
            final String password = json.get("password").getAsString();

            final AuthToken token = authService.login(login, password);
            return createSuccessResponse(jsonFactory.toJson(token));
        } catch (final Exception e) {
            log.error("Faile to log in " + login, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param session this method can be used if the user was authorized out of the controller
     * and wants to obtain an security token for REST service.
     * @return authorization token.
     */
    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    public @ResponseBody String getAuthToken(final HttpSession session) {
        try {
            final AuthToken token = authService.attachToExistingSession(session);
            return createSuccessResponse(jsonFactory.toJson(token));
        } catch (final Exception e) {
            log.error("Failed to get auth token. Possible not user has logged in", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     */
    @RequestMapping(value = "/logout/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String logout(@PathVariable final String authToken) {
        authService.logout(authToken);
        return createSuccessResponse(null);
    }
    /**
     * @param authToken old authentication token.
     * @return refreshed authorization token.
     */
    @RequestMapping(value = "/refreshToken/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String refreshToken(@PathVariable final String authToken) {
        try {
            final AuthToken token = authService.refreshToken(authToken);
            return createSuccessResponse(jsonFactory.toJson(token));
        } catch (final Exception e) {
            log.error("Failed to refresh token " + authToken, e);
            return createErrorResponse(e);
        }
    }

    //REST methods
    /**
     * @param authToken authentication token.
     * @param username name of user for request info.
     * @return user info
     */
    @RequestMapping(value = "/getUser/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getUser(@PathVariable final String authToken,
            final @RequestParam String username) {
        try {
            final User user = getLoggedInUser(authToken);
            if (!user.getLogin().equals(username)) {
                checkAdmin(user);
            }

            final User u = authService.getUser(username);
            return createSuccessResponse(u == null ? null : jsonFactory.toJson(u));
        } catch (final Exception e) {
            log.error("Failed to get user info", e);
            return createErrorResponse(e);
        }
    }

    /**
     * @param authToken authentication token.
     * @param alert alert profile.
     * @return ID of saved alert profile.
     */
    @RequestMapping(value = "/saveAlertProfile/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String saveAlertProfile(@PathVariable final String authToken,
            final @RequestBody String alert) {
        try {
            checkAdmin(authToken);
            final Long id = restService.saveAlertProfile(
                    jsonFactory.parseAlertProfile(getJSonObject(alert)));
            return createIdResponse(id);
        } catch (final Exception e) {
            log.error("Failed to save alert profile", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @return list of alert profiles.
     */
    @RequestMapping(value = "/getAlertProfiles/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getAlertProfiles(@PathVariable final String authToken) {
        try {
            //check logged in.
            getLoggedInUser(authToken);

            final List<AlertProfile> alerts = restService.getAlertProfiles();
            final JsonArray array = new JsonArray();
            for (final AlertProfile a : alerts) {
                array.add(jsonFactory.toJson(a));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get alert profiles", e);
            return createErrorResponse(e);
        }
    }

    /**
     * @param authToken authentication token.
     * @param profile location profile.
     * @return ID of saved location profile.
     */
    @RequestMapping(value = "/saveLocationProfile/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String saveLocationProfile(@PathVariable final String authToken,
            final @RequestBody String profile) {
        try {
            checkAdmin(authToken);

            final Long id = restService.saveLocationProfile(
                    jsonFactory.parseLocationProfile(getJSonObject(profile)));
            return createIdResponse(id);
        } catch (final Exception e) {
            log.error("Failed to save location profile.", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @return list of location profiles.
     */
    @RequestMapping(value = "/getLocationProfiles/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getLocationProfiles(@PathVariable final String authToken) {
        try {
            //check logged in.
            getLoggedInUser(authToken);

            final List<LocationProfile> locations = restService.getLocationProfiles();
            final JsonArray array = new JsonArray();
            for (final LocationProfile location : locations) {
                array.add(jsonFactory.toJson(location));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get location profiles", e);
            return createErrorResponse(e);
        }
    }

    /**
     * @param authToken authentication token.
     * @param schedule notification schedule.
     * @return ID of saved notification schedule.
     */
    @RequestMapping(value = "/saveNotificationSchedule/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String saveNotificationSchedule(@PathVariable final String authToken,
            final @RequestBody String schedule) {
        try {
            checkAdmin(authToken);

            final Long id = restService.saveNotificationSchedule(
                    jsonFactory.parseNotificationSchedule(getJSonObject(schedule)));
            return createIdResponse(id);
        } catch (final Exception e) {
            log.error("Failed to save notification schedule", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @return list of notification shedules.
     */
    @RequestMapping(value = "/getNotificationSchedules/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getNotificationSchedules(@PathVariable final String authToken) {
        try {
            //check logged in.
            getLoggedInUser(authToken);

            final List<NotificationSchedule> schedules = restService.getNotificationSchedules();
            final JsonArray array = new JsonArray();
            for (final NotificationSchedule schedule : schedules) {
                array.add(jsonFactory.toJson(schedule));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get notification schedules", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param tpl shipment template.
     * @return ID of saved shipment template.
     */
    @RequestMapping(value = "/saveShipmentTemplate/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String saveShipmentTemplate(@PathVariable final String authToken,
            final @RequestBody String tpl) {
        try {
            checkAdmin(authToken);

            final Long id = restService.saveShipmentTemplate(
                    jsonFactory.parseShipmentTemplate(getJSonObject(tpl)));
            return createIdResponse(id);
        } catch (final Exception e) {
            log.error("Failed to save shipment template", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @return list of shipment templates.
     */
    @RequestMapping(value = "/getShipmentTemplates/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipmentTemplates(@PathVariable final String authToken) {
        try {
            //check logged in.
            getLoggedInUser(authToken);

            final List<ShipmentTemplate> templates = restService.getShipmentTemplates();
            final JsonArray array = new JsonArray();
            for (final ShipmentTemplate tpl : templates) {
                array.add(jsonFactory.toJson(tpl));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get shipment templates", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param device device.
     * @return ID of saved device.
     */
    @RequestMapping(value = "/saveDevice/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String saveDevice(@PathVariable final String authToken,
            final @RequestBody String device) {
        try {
            checkAdmin(authToken);

            restService.saveDevice(jsonFactory.parseDevice(getJSonObject(device)));
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @return list of devices.
     */
    @RequestMapping(value = "/getDevices/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getDevices(@PathVariable final String authToken) {
        try {
            //check logged in.
            getLoggedInUser(authToken);

            final List<Device> devices = restService.getDevices();
            final JsonArray array = new JsonArray();
            for (final Device t : devices) {
                array.add(jsonFactory.toJson(t));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param shipment shipment.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/saveShipment/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String saveShipment(@PathVariable final String authToken,
            final @RequestBody String shipment) {
        try {
            checkAdmin(authToken);

            final SaveShipmentRequest req = jsonFactory.parseSaveShipmentRequest(getJSonObject(shipment));
            final Long id = restService.saveShipment(req.getShipment());

            final SaveShipmentResponse resp = new SaveShipmentResponse();
            resp.setShipmentId(id);

            if (req.isSaveAsNewTemplate()) {
                final Long tplId = restService.createShipmentTemplate(req.getShipment(), req.getTemplateName());
                resp.setTemplateId(tplId);
            }
            return createSuccessResponse(jsonFactory.toJson(resp));
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @return list of shipments.
     */
    @RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipments(@PathVariable final String authToken) {
        try {
            //check logged in.
            getLoggedInUser(authToken);

            final List<Shipment> shipments = restService.getShipments();
            final JsonArray array = new JsonArray();
            for (final Shipment t : shipments) {
                array.add(jsonFactory.toJson(t));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param shipment shipment ID.
     * @return list of shipments.
     */
    @RequestMapping(value = "/getNotifications/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getNotifications(@PathVariable final String authToken,
            @RequestParam final Long shipment) {
        try {
            //check logged in.
            getLoggedInUser(authToken);

            final List<Notification> shipments = restService.getNotifications(shipment);
            final JsonArray array = new JsonArray();
            for (final Notification t : shipments) {
                array.add(jsonFactory.toJson(t));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/markNotificationsAsRead/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String markNotificationsAsRead(@PathVariable final String authToken,
            @RequestBody final String notificationIds) {
        try {
            //check logged in.
            final User user = authService.getUserForToken(authToken);
            getLoggedInUser(authToken);

            final JsonArray array = getJSon(notificationIds).getAsJsonArray();
            final List<Long> ids = new LinkedList<Long>();

            final int size = array.size();
            for (int i = 0; i < size; i++) {
                ids.add(array.get(i).getAsLong());
            }

            restService.markNotificationsAsRead(user, ids);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/getShipmentData/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipmentData(@PathVariable final String authToken,
            @RequestParam final String fromDate,
            @RequestParam final String toDate,
            @RequestParam final String onlyWithAlerts) {

        try {
            //check logged in.
            getLoggedInUser(authToken);

            final Date startDate = parseDate(fromDate);
            final Date endDate = parseDate(toDate);

            final List<ShipmentData> data = restService.getShipmentData(
                    startDate, endDate, onlyWithAlerts);

            final JsonArray array = new JsonArray();
            for (final ShipmentData d : data) {
                array.add(jsonFactory.toJson(d));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param req shipment.
     * @return status.
     */
    @RequestMapping(value = "/sendCommandToDevice/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String sendCommandToDevice(@PathVariable final String authToken,
            final @RequestBody String req) {
        try {
            checkAdmin(authToken);

            final DeviceCommand cmd = jsonFactory.parseDeviceCommand(getJSonObject(req));
            restService.sendCommandToDevice(cmd);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param id the entity ID.
     * @return JSON response.
     */
    protected String createIdResponse(final Long id) {
        return createSuccessResponse(jsonFactory.idToJson(id));
    }
    /**
     * @param response.
     * @return
     */
    private String createSuccessResponse(final JsonElement response) {
        final JsonObject obj = new JsonObject();
        //add status
        obj.add("status", createStatus(0, "Success"));
        //add response
        obj.add("response", response == null ? JsonNull.INSTANCE : response);
        return obj.toString();
    }
    /**
     * @param e
     * @return
     */
    protected String createErrorResponse(final Exception e) {
        final JsonObject obj = new JsonObject();
        //add status
        obj.add("status", createErrorStatus(e));
        //add response
        return obj.toString();
    }
    /**
     * @param errorCode error code.
     * @param e error.
     * @return encoded to JSON object error.
     */
    private JsonObject createErrorStatus(final Throwable e) {
        int code = -1;
        if (e instanceof AuthenticationException) {
            code = ErrorCodes.AUTHENTICATION_ERROR;
        } else if (e instanceof RestServiceException) {
            code = ((RestServiceException) e).getErrorCode();
        }
        return jsonFactory.createErrorStatus(code, e);
    }
    /**
     * @param code status code.
     * @param message status description.
     * @return
     */
    private JsonObject createStatus(final int code, final String message) {
        final JsonObject status = new JsonObject();
        status.addProperty("code", code);
        status.addProperty("message", message);
        return status;
    }

    /**
     * @param authToken authentication token.
     * @return user if logged in.
     * @throws AuthenticationException
     */
    private User getLoggedInUser(final String authToken) throws AuthenticationException {
        final User user = authService.getUserForToken(authToken);
        if (user == null) {
            throw new AuthenticationException("Not logged in");
        }
        return user;
    }
    /**
     * @param user user.
     */
    private void checkAdmin(final User user) throws RestServiceException {
        if (!user.getRoles().contains(Role.admin)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR, "User has not admin privileges");
        }
    }
    /**
     * @param authToken
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    private void checkAdmin(final String authToken) throws AuthenticationException, RestServiceException {
        final User user = getLoggedInUser(authToken);
        checkAdmin(user);
    }
    /**
     * @param text the resource name.
     * @throws RestServiceException
     * @throws IOException
     */
    private JsonObject getJSonObject(final String text) throws RestServiceException {
        return getJSon(text).getAsJsonObject();
    }
    /**
     * @param text the resource name.
     * @throws RestServiceException
     * @throws IOException
     */
    private JsonElement getJSon(final String text) throws RestServiceException {
        try {
            final Reader in = new StringReader(text);
            return new JsonParser().parse(in);
        } catch (final Exception e) {
            throw new RestServiceException(ErrorCodes.INVALID_JSON, "Invalid JSON format");
        }
    }
    /**
     * @param dateStr date string.
     * @return date.
     */
    private Date parseDate(final String dateStr) {
        return dateStr == null || dateStr.length() == 0 ? null : JSonFactory.parseDate(dateStr);
    }
}
