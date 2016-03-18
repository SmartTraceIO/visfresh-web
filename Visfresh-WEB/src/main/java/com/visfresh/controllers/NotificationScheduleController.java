/**
 *
 */
package com.visfresh.controllers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.NotificationScheduleConstants;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.impl.ShipmentBaseDao;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.ReferenceInfo;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.io.UserResolver;
import com.visfresh.io.json.NotificationScheduleSerializer;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("NotificationSchedule")
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
    @Autowired
    private UserResolver userResolver;

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
    public JsonObject saveNotificationSchedule(@PathVariable final String authToken,
            final @RequestBody JsonObject schedule) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final NotificationSchedule s = createSerializer(user).parseNotificationSchedule(schedule);
            s.setCompany(user.getCompany());

            final NotificationSchedule old = dao.findOne(s.getId());
            checkCompanyAccess(user, old);

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
    public JsonObject getNotificationSchedules(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            final List<NotificationSchedule> schedules = dao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 2),
                    page,
                    null);

            final int total = dao.getEntityCount(user.getCompany(), null);

            final NotificationScheduleSerializer ser = createSerializer(user);
            final JsonArray array = new JsonArray();
            for (final NotificationSchedule schedule : schedules) {
                array.add(ser.toJson(new ListNotificationScheduleItem(schedule)));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get notification schedules", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private NotificationScheduleSerializer createSerializer(final User user) {
        final NotificationScheduleSerializer s = new NotificationScheduleSerializer(user.getTimeZone());
        s.setUserResolver(userResolver);
        return s;
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
    public JsonObject deletePersonSchedule(@PathVariable final String authToken,
            @RequestParam final long notificationScheduleId,
            @RequestParam final long personScheduleId) {

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            //find schedule
            final NotificationSchedule s = dao.findOne(notificationScheduleId);
            checkCompanyAccess(user, s);

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
    public JsonObject getNotificationSchedule(@PathVariable final String authToken,
            @RequestParam final Long notificationScheduleId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final NotificationSchedule s = dao.findOne(notificationScheduleId);
            checkCompanyAccess(user, s);

            return createSuccessResponse(createSerializer(user).toJson(s));
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
    public JsonObject deleteNotificationSchedule(@PathVariable final String authToken,
            @RequestParam final Long notificationScheduleId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final NotificationSchedule s = dao.findOne(notificationScheduleId);
            checkCompanyAccess(user, s);

            dao.delete(s);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete notification schedule " + notificationScheduleId, e);
            final List<ReferenceInfo> refs = dao.getDbReferences(notificationScheduleId);
            if (!refs.isEmpty()) {
                return createErrorResponse(ErrorCodes.ENTITY_IN_USE, createEntityInUseMessage(refs));
            }
            return createErrorResponse(e);
        }
    }
    /**
     * @param refs list of references.
     * @return
     */
    private String createEntityInUseMessage(final List<ReferenceInfo> refs) {
        final String alerts = ShipmentBaseDao.ALERTNOTIFSCHEDULES_TABLE;
        final String arrivals = ShipmentBaseDao.ARRIVALNOTIFSCHEDULES_TABLE;

        //create references map
        final Map<String, List<Long>> refMap = new HashMap<>();
        refMap.put(alerts, new LinkedList<Long>());
        refMap.put(arrivals, new LinkedList<Long>());

        //group references by tables
        for (final ReferenceInfo ref : refs) {
            refMap.get(ref.getType()).add((Long) ref.getId());
        }

        final StringBuilder sb = new StringBuilder("Notificatino schedule can't be deleted because"
                + " is referenced by ");
        if (!refMap.get(alerts).isEmpty()) {
            sb.append("alert notification schedules (");
            sb.append(StringUtils.combine(refMap.get(alerts), ", "));
            sb.append(')');
        }
        if (!refMap.get(arrivals).isEmpty()) {
            if (!refMap.get(alerts).isEmpty()) {
                sb.append("and ");
            }

            sb.append("arrivals notification schedules (");
            sb.append(StringUtils.combine(refMap.get(arrivals), ", "));
            sb.append(')');
        }
        return sb.toString();
    }
}
