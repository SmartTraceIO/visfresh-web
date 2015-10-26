/**
 *
 */
package com.visfresh.controllers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.io.JSonSerializer;
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
    private JSonSerializer serializer;
    /**
     * Access controller.
     */
    @Autowired
    private AccessController security;

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
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public @ResponseBody String login(final @RequestParam String login, final @RequestParam String password) {
        try {
            final AuthToken token = authService.login(login, password);
            return createSuccessResponse(getSerializer().toJson(token));
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
            return createSuccessResponse(getSerializer().toJson(token));
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
            final User user = getLoggedInUser(authToken);
            final AuthToken token = authService.refreshToken(user);
            return createSuccessResponse(getSerializer().toJson(token));
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
            security.checkCanGetUserInfo(user, username);

            final User u = authService.getUser(username);
            return createSuccessResponse(u == null ? null : getSerializer().toJson(u));
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
            final User user = getLoggedInUser(authToken);
            final AlertProfile p = getSerializer().parseAlertProfile(getJSonObject(alert));

            security.checkCanSaveAlertProfile(user);
            final Long id = restService.saveAlertProfile(user.getCompany(), p);
            return createIdResponse(id);
        } catch (final Exception e) {
            log.error("Failed to save alert profile", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param id alert profile ID.
     * @return alert profile.
     */
    @RequestMapping(value = "/getAlertProfile/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getAlertProfile(@PathVariable final String authToken,
            @RequestParam final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetAlertProfiles(user);

            final AlertProfile alert = restService.getAlertProfile(user.getCompany(), id);
            return createSuccessResponse(getSerializer().toJson(alert));
        } catch (final Exception e) {
            log.error("Failed to get alert profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of alert profiles.
     */
    @RequestMapping(value = "/getAlertProfiles/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getAlertProfiles(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetAlertProfiles(user);
            final JSonSerializer ser = getSerializer();

            final List<AlertProfile> alerts = getPage(
                    restService.getAlertProfiles(user.getCompany()), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final AlertProfile a : alerts) {
                array.add(ser.toJson(a));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get alert profiles", e);
            return createErrorResponse(e);
        }
    }

    /**
     * @param list
     * @param pageIndex
     * @param pageSize
     * @return
     */
    private <E extends EntityWithId<ID>, ID extends Serializable & Comparable<ID>> List<E>
            getPage(final List<E> list, final int pageIndex, final int pageSize) {
        final int fromIndex = (pageIndex - 1) * pageSize;
        if (fromIndex >= list.size()) {
            return new LinkedList<E>();
        }

        //sort first of all
        Collections.sort(list, new Comparator<E>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final E o1, final E o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        final int toIndex = Math.min(fromIndex + pageSize, list.size());
        return list.subList(fromIndex, toIndex);
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
            final User user = getLoggedInUser(authToken);
            final LocationProfile lp = getSerializer().parseLocationProfile(getJSonObject(profile));

            security.checkCanSaveLocationProfile(user);

            final Long id = restService.saveLocationProfile(user.getCompany(), lp);
            return createIdResponse(id);
        } catch (final Exception e) {
            log.error("Failed to save location profile.", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of location profiles.
     */
    @RequestMapping(value = "/getLocationProfiles/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getLocationProfiles(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetLocationProfiles(user);

            final JSonSerializer ser = getSerializer();

            final List<LocationProfile> locations = getPage(restService.getLocationProfiles(user.getCompany()),
                    pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final LocationProfile location : locations) {
                array.add(ser.toJson(location));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get location profiles", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param id location profile ID.
     * @return location profile.
     */
    @RequestMapping(value = "/getLocationProfile/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getLocationProfile(@PathVariable final String authToken,
            @RequestParam final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetLocationProfiles(user);

            final LocationProfile location = restService.getLocationProfile(user.getCompany(), id);
            return createSuccessResponse(getSerializer().toJson(location));
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
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveNotificationSchedule(user);

            final Long id = restService.saveNotificationSchedule(
                    user.getCompany(), getSerializer().parseNotificationSchedule(getJSonObject(schedule)));
            return createIdResponse(id);
        } catch (final Exception e) {
            log.error("Failed to save notification schedule", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of notification schedules.
     */
    @RequestMapping(value = "/getNotificationSchedules/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getNotificationSchedules(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetNotificationSchedules(user);

            final List<NotificationSchedule> schedules = getPage(restService.getNotificationSchedules(
                    user.getCompany()), pageIndex, pageSize);

            final JSonSerializer ser = getSerializer();
            final JsonArray array = new JsonArray();
            for (final NotificationSchedule schedule : schedules) {
                array.add(ser.toJson(schedule));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get notification schedules", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param id notification schedule ID.
     * @return notification schedule.
     */
    @RequestMapping(value = "/getNotificationSchedule/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getNotificationSchedule(@PathVariable final String authToken,
            @RequestParam final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetNotificationSchedules(user);

            final NotificationSchedule schedule = restService.getNotificationSchedule(
                    user.getCompany(), id);

            return createSuccessResponse(getSerializer().toJson(schedule));
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
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipmentTemplate(user);

            final Long id = restService.saveShipmentTemplate(
                    user.getCompany(), getSerializer().parseShipmentTemplate(getJSonObject(tpl)));
            return createIdResponse(id);
        } catch (final Exception e) {
            log.error("Failed to save shipment template", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipment templates.
     */
    @RequestMapping(value = "/getShipmentTemplates/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipmentTemplates(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipmentTemplates(user);

            final List<ShipmentTemplate> templates = getPage(
                    restService.getShipmentTemplates(user.getCompany()), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final ShipmentTemplate tpl : templates) {
                array.add(getSerializer().toJson(tpl));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get shipment templates", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param id shipment template ID.
     * @return shipment template.
     */
    @RequestMapping(value = "/getShipmentTemplate/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipmentTemplate(@PathVariable final String authToken,
            @RequestParam final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipmentTemplates(user);

            final ShipmentTemplate template = restService.getShipmentTemplate(user.getCompany(), id);
            return createSuccessResponse(getSerializer().toJson(template));
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
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveDevice(user);

            restService.saveDevice(user.getCompany(), getSerializer().parseDevice(getJSonObject(device)));
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of devices.
     */
    @RequestMapping(value = "/getDevices/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getDevices(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetDevices(user);

            final JSonSerializer ser = getSerializer();

            final List<Device> devices = getPage(restService.getDevices(user.getCompany()), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final Device t : devices) {
                array.add(ser.toJson(t));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param id device ID.
     * @return device.
     */
    @RequestMapping(value = "/getDevice/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getDevice(@PathVariable final String authToken,
            @RequestParam final String id) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetDevices(user);

            final Device device = restService.getDevice(user.getCompany(), id);
            return createSuccessResponse(getSerializer().toJson(device));
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
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipment(user);

            final SaveShipmentRequest req = getSerializer().parseSaveShipmentRequest(getJSonObject(shipment));
            final Long id = restService.saveShipment(user.getCompany(), req.getShipment());

            final SaveShipmentResponse resp = new SaveShipmentResponse();
            resp.setShipmentId(id);

            if (req.isSaveAsNewTemplate()) {
                final Long tplId = restService.createShipmentTemplate(
                        user.getCompany(), req.getShipment(), req.getTemplateName());
                resp.setTemplateId(tplId);
            }
            return createSuccessResponse(getSerializer().toJson(resp));
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipments.
     */
    @RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipments(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipments(user);

            final JSonSerializer ser = getSerializer();

            final List<Shipment> shipments = getPage(restService.getShipments(user.getCompany()), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final Shipment t : shipments) {
                array.add(ser.toJson(t));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param id shipment ID.
     * @return shipment.
     */
    @RequestMapping(value = "/getShipment/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getShipment(@PathVariable final String authToken,
            @RequestParam final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipments(user);

            final Shipment shipment = restService.getShipment(user.getCompany(), id);
            return createSuccessResponse(getSerializer().toJson(shipment));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipments.
     */
    @RequestMapping(value = "/getNotifications/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getNotifications(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            final JSonSerializer ser = getSerializer();

            final List<Notification> shipments = getPage(restService.getNotifications(user), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final Notification t : shipments) {
                array.add(ser.toJson(t));
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
            final Set<Long> ids = new HashSet<Long>();

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
            @RequestParam final boolean onlyWithAlerts) {

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipmentData(user);

            final JSonSerializer ser = getSerializer();

            final Date startDate = parseDate(fromDate);
            final Date endDate = parseDate(toDate);

            final List<ShipmentData> data = restService.getShipmentData(
                    user.getCompany(), startDate, endDate, onlyWithAlerts);

            final JsonArray array = new JsonArray();
            for (final ShipmentData d : data) {
                array.add(ser.toJson(d));
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
            final User user = getLoggedInUser(authToken);
            security.checkCanSendCommandToDevice(user);

            final DeviceCommand cmd = getSerializer().parseDeviceCommand(getJSonObject(req));
            restService.sendCommandToDevice(cmd);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param req shipment.
     * @return status.
     */
    @RequestMapping(value = "/createUser/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String createUser(@PathVariable final String authToken,
            final @RequestBody String req) {
        try {
            final User user = getLoggedInUser(authToken);
            final CreateUserRequest r = getSerializer().parseCreateUserRequest(getJSonObject(req));
            security.checkCanCreateUser(user, r);

            final User newUser = r.getUser();
            newUser.setCompany(r.getCompany());
            authService.createUser(newUser, r.getPassword());

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @return profile for given user.
     */
    @RequestMapping(value = "/getProfile/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getProfile(@PathVariable final String authToken) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkGetProfile(user);

            final UserProfile profile = restService.getProfile(user);
            return createSuccessResponse(getSerializer().toJson(profile));
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param profile to save.
     * @return profile for given user.
     */
    @RequestMapping(value = "/saveProfile/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String getProfile(@PathVariable final String authToken,
            @RequestBody final String profile) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkSaveProfile(user);

            final UserProfile p = getSerializer().parseUserProfile(getJSon(profile));
            restService.saveUserProfile(user, p);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param id company ID.
     * @return company.
     */
    @RequestMapping(value = "/getCompany/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getCompany(@PathVariable final String authToken,
            @RequestParam final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetCompany(user, id);

            final Company company;
            if (user.getCompany().getId().equals(id)) {
                company = user.getCompany();
            } else {
                company = restService.getCompany(id);
            }
            return createSuccessResponse(getSerializer().toJson(company));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return company.
     */
    @RequestMapping(value = "/getCompanies/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getCompanies(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetCompanies(user);

            final List<Company> company = getPage(restService.getCompanies(), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final Company c : company) {
                array.add(getSerializer().toJson(c));
            }
            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param id the entity ID.
     * @return JSON response.
     */
    protected String createIdResponse(final Long id) {
        return createSuccessResponse(JSonSerializer.idToJson(id));
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
        return JSonSerializer.createErrorStatus(code, e);
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
     * @return
     */
    private JSonSerializer getSerializer() {
        return serializer;
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
            return JSonSerializer.parseJson(text);
        } catch (final Exception e) {
            throw new RestServiceException(ErrorCodes.INVALID_JSON, "Invalid JSON format");
        }
    }
    /**
     * @param dateStr date string.
     * @return date.
     */
    private Date parseDate(final String dateStr) {
        return dateStr == null || dateStr.length() == 0 ? null : JSonSerializer.parseDate(dateStr);
    }
}
