/**
 *
 */
package com.visfresh.controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;
import com.visfresh.io.SavePersonScheduleRequest;
import com.visfresh.services.RestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("NotificationSchedule")
@RequestMapping("/rest")
public class NotificationScheduleController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(NotificationScheduleController.class);
    /**
     * REST service.
     */
    @Autowired
    private RestService restService;

    /**
     * Default constructor.
     */
    public NotificationScheduleController() {
        super();
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
                    user.getCompany(), getSerializer(user).parseNotificationSchedule(getJSonObject(schedule)));
            return createIdResponse("notificationScheduleId", id);
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
            @RequestParam final int pageIndex, @RequestParam final int pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetNotificationSchedules(user);

            final List<NotificationSchedule> scs = restService.getNotificationSchedules(
                    user.getCompany());
            sort(scs, sc, so);

            final List<NotificationSchedule> schedules = getPage(scs, pageIndex, pageSize);

            final EntityJSonSerializer ser = getSerializer(user);
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
     * @param profiles
     * @param sc
     * @param so
     */
    private void sort(final List<NotificationSchedule> profiles, final String sc, final String so) {
        final boolean ascent = !"desc".equals(so);
        Collections.sort(profiles, new Comparator<NotificationSchedule>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final NotificationSchedule o1, final NotificationSchedule o2) {
                if ("notificationScheduleName".equalsIgnoreCase(sc)) {
                    return compareTo(o1.getName(), o2.getName(), ascent);
                } else if ("notificationScheduleDescription".equalsIgnoreCase(sc)) {
                    return compareTo(o1.getDescription(), o2.getDescription(), ascent);
                }
                return compareTo(o1.getId(), o2.getId(), ascent);
            }
        });

    }

    /**
     * @param authToken authentication token.
     * @param notificationScheduleId notification schedule ID.
     * @return notification schedule.
     */
    @RequestMapping(value = "/getNotificationSchedule/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getNotificationSchedule(@PathVariable final String authToken,
            @RequestParam final Long notificationScheduleId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetNotificationSchedules(user);

            final NotificationSchedule schedule = restService.getNotificationSchedule(
                    user.getCompany(), notificationScheduleId);

            return createSuccessResponse(getSerializer(user).toJson(schedule));
        } catch (final Exception e) {
            log.error("Failed to get notification schedules", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param notificationScheduleId notification schedule ID.
     * @return notification schedule.
     */
    @RequestMapping(value = "/deleteNotificationSchedule/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String deleteNotificationSchedule(@PathVariable final String authToken,
            @RequestParam final Long notificationScheduleId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveNotificationSchedule(user);

            restService.deleteNotificationSchedule(user.getCompany(), notificationScheduleId);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get notification schedules", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param schedule notification schedule.
     * @return ID of saved notification schedule.
     */
    @RequestMapping(value = "/savePersonSchedule/{authToken}", method = RequestMethod.POST)
    public @ResponseBody String savePersonSchedule(@PathVariable final String authToken,
            final @RequestBody String schedule) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveNotificationSchedule(user);

            final SavePersonScheduleRequest req = getSerializer(user).parseSavePersonScheduleRequest(
                    getJSon(schedule));
            final NotificationSchedule s = restService.getNotificationSchedule(user.getCompany(),
                    req.getNotificationScheduleId());
            final PersonSchedule personSchedule = req.getSchedule();
            if (personSchedule.getId() != null) {
                removePersonSchedule(s, personSchedule.getId());
            }
            s.getSchedules().add(personSchedule);
            restService.saveNotificationSchedule(user.getCompany(), s);
            return createIdResponse("personScheduleId", personSchedule.getId());
        } catch (final Exception e) {
            log.error("Failed to save notification schedule", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param notificationScheduleId notification schedule ID.
     * @return notification schedule.
     */
    @RequestMapping(value = "/getPersonSchedule/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getPersonSchedule(@PathVariable final String authToken,
            @RequestParam final Long notificationScheduleId,
            @RequestParam final Long personScheduleId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetNotificationSchedules(user);

            final NotificationSchedule schedule = restService.getNotificationSchedule(
                    user.getCompany(), notificationScheduleId);

            if (schedule == null) {
                return createSuccessResponse(null);
            }

            return createSuccessResponse(getSerializer(user).toJson(
                    findPersonScheduleById(schedule, personScheduleId)));
        } catch (final Exception e) {
            log.error("Failed to get notification schedules", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param schedule
     * @param id
     * @return
     */
    private PersonSchedule findPersonScheduleById(final NotificationSchedule schedule,
            final Long id) {
        for (final PersonSchedule s : schedule.getSchedules()) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * @param s
     * @param id
     */
    private void removePersonSchedule(final NotificationSchedule s, final Long id) {
        final Iterator<PersonSchedule> iter = s.getSchedules().iterator();
        while (iter.hasNext()) {
            if (iter.next().getId().equals(id)) {
                iter.remove();
                break;
            }
        }
    }
}
