/**
 *
 */
package com.visfresh.controllers;

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
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.Page;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;
import com.visfresh.services.lists.NotificationScheduleListItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("NotificationSchedule")
@RequestMapping("/rest")
public class NotificationScheduleController extends AbstractController implements NotificationScheduleConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(NotificationScheduleController.class);
    /**
     * Notification schedule DAO.
     */
    @Autowired
    private NotificationScheduleDao dao;

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

            final NotificationSchedule s = getSerializer(user).parseNotificationSchedule(
                    getJSonObject(schedule));
            checkCompanyAccess(user, s);

            s.setCompany(user.getCompany());
            final Long id = dao.save(s).getId();
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
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetNotificationSchedules(user);

            final List<NotificationSchedule> schedules = dao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder()),
                    page,
                    null);

            final int total = dao.getEntityCount(user.getCompany(), null);

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
     * @return
     */
    private String[] getDefaultSortOrder() {
        return new String[]{
            PROPERTY_NOTIFICATION_SCHEDULE_NAME,
            PROPERTY_NOTIFICATION_SCHEDULE_ID,
            PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION
        };
    }

    /**
     * @param authToken authentication token
     * @param personScheduleId person schedule ID.
     * @return list of notification schedules.
     */
    @RequestMapping(value = "/deletePersonSchedule/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String deletePersonSchedule(@PathVariable final String authToken,
            @RequestParam final long notificationScheduleId,
            @RequestParam final long personScheduleId) {

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveNotificationSchedule(user);

            //find schedule
            final NotificationSchedule s = dao.findOne(notificationScheduleId);
            if (s != null && s.getCompany().getId().equals(user.getCompany().getId())) {
                for (final PersonSchedule ps : s.getSchedules()) {
                    if (ps.getId().equals(personScheduleId)) {
                        s.getSchedules().remove(ps);
                        break;
                    }
                }

                dao.save(s);
            }

            return createSuccessResponse(null);
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
    @RequestMapping(value = "/getNotificationSchedule/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getNotificationSchedule(@PathVariable final String authToken,
            @RequestParam final Long notificationScheduleId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetNotificationSchedules(user);

            final NotificationSchedule s = dao.findOne(notificationScheduleId);
            checkCompanyAccess(user, s);

            return createSuccessResponse(getSerializer(user).toJson(s));
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

            final NotificationSchedule s = dao.findOne(notificationScheduleId);
            checkCompanyAccess(user, s);

            dao.delete(s);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get notification schedules", e);
            return createErrorResponse(e);
        }
    }
}
