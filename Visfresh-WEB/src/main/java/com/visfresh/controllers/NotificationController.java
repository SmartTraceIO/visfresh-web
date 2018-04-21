/**
 *
 */
package com.visfresh.controllers;

import java.text.DateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.NotificationConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.impl.NotificationDaoImpl;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.entities.UserNotification;
import com.visfresh.io.NotificationItem;
import com.visfresh.io.json.NotificationSerializer;
import com.visfresh.l12n.NotificationBundle;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Notification")
@RequestMapping("/rest")
public class NotificationController extends AbstractController implements NotificationConstants {
    @Autowired
    private NotificationDao dao;
    @Autowired
    private NotificationBundle notificationBundle;

    /**
     * Default constructor.
     */
    public NotificationController() {
        super();
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipments.
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getNotifications", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getNotifications(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final Boolean includeRead) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        //check logged in.
        final User user = getLoggedInUser();
        final NotificationSerializer ser = createSerializer(user);

        final Filter filter = new Filter();
        filter.addFilter(NotificationDaoImpl.HIDDEN_FIELD, false);
        if (!Boolean.TRUE.equals(includeRead)) {
            filter.addFilter(PROPERTY_CLOSED, Boolean.FALSE);
        }

        final List<UserNotification> ns = dao.findForUser(user,
                true,
                new Sorting(false, getDefaultSortOrder()),
                filter,
                page);

        final int total = dao.getEntityCount(user, true, filter);
        final JsonArray array = new JsonArray();
        final DateFormat isoFormat = DateTimeUtils.createDateFormat(
                "yyyy-MM-dd'T'HH:mm", user.getLanguage(), user.getTimeZone());

        for (final UserNotification t : ns) {
            array.add(ser.toJson(createNotificationItem(t, user, isoFormat)));
        }

        return createListSuccessResponse(array, total);
    }

    @RequestMapping(value = "/markNotificationsAsRead", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject markNotificationsAsRead(@RequestBody final JsonArray notificationIds) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final Set<Long> ids = new HashSet<Long>();

        for (final JsonElement e : notificationIds) {
            ids.add(e.getAsLong());
        }

        dao.markAsReadenByUserAndId(user, ids);
        return createSuccessResponse(null);
    }
    /**
     * @param n notification.
     * @param user user.
     * @return notification item.
     */
    private NotificationItem createNotificationItem(final UserNotification n, final User user,
            final DateFormat dateFormatter) {
        final NotificationItem item = new NotificationItem();
        item.setAlertId(n.getIssueId());
        if (n.getType() == NotificationType.Alert) {
            item.setAlertType(n.getAlertType().name());
        } else if (n.getType() == NotificationType.Arrival) {
            item.setAlertType("ArrivalNotice");
        }

        item.setClosed(n.isRead());
        item.setDate(dateFormatter.format(n.getIssueDate()));
        item.setNotificationId(n.getId());

        item.setShipmentId(n.getShipmentId());

        //set description.
        String title = "";
        if (n.getType() == NotificationType.Alert) {
            title = n.getAlertType().name();
        } else if (n.getType() == NotificationType.Arrival) {
            title = "Arrival notification";
        }

        item.setTitle(title);
        item.setType("Alert");
        item.setLink(notificationBundle.getLinkToShipment(n));

        final String[] lines = notificationBundle.getAppMessage(
                n, user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits()).split("\n");
        for (final String line : lines) {
            item.getLines().add(line);
        }
        return item;
    }
    /**
     * @param user user.
     * @return notification serializer.
     */
    private NotificationSerializer createSerializer(final User user) {
        return new NotificationSerializer(user.getTimeZone());
    }
    /**
     * @return
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            PROPERTY_NOTIFICATION_ID,
            PROPERTY_TYPE
        };
    }
}
