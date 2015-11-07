/**
 *
 */
package com.visfresh.controllers;

import java.util.Collections;
import java.util.Comparator;
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
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;
import com.visfresh.services.RestService;
import com.visfresh.services.lists.NotificationScheduleListItem;

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
            @RequestParam(required = false) final Integer pageIndex, @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final int page = pageIndex == null ? 1 : pageIndex.intValue();
        final int size = pageSize == null ? Integer.MAX_VALUE : pageSize.intValue();

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetNotificationSchedules(user);

            final List<NotificationSchedule> scs = restService.getNotificationSchedules(
                    user.getCompany());
            sort(scs, sc, so);

            final int total = scs.size();
            final List<NotificationSchedule> schedules = getPage(scs, page, size);

            final EntityJSonSerializer ser = getSerializer(user);
            final JsonArray array = new JsonArray();
            for (final NotificationSchedule schedule : schedules) {
                array.add(ser.toJson(new NotificationScheduleListItem(schedule)));
            }

            return createListSuccessResponse(array, total);
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
}
