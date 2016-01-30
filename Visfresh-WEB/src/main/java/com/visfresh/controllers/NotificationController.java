/**
 *
 */
package com.visfresh.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.NotificationConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.User;
import com.visfresh.io.NotificationItem;
import com.visfresh.io.json.NotificationSerializer;
import com.visfresh.mpl.services.AlertDescriptionBuilder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Notification")
@RequestMapping("/rest")
public class NotificationController extends AbstractController implements NotificationConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationDao dao;
    @Autowired
    private AlertDescriptionBuilder descriptionBuilder;

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
     */
    @RequestMapping(value = "/getNotifications/{authToken}", method = RequestMethod.GET)
    public JsonObject getNotifications(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final Boolean includeRead) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            final NotificationSerializer ser = createSerializer(user);

            Filter filter;
            if (Boolean.TRUE.equals(includeRead)) {
                filter = null;
            } else {
                filter = new Filter();
                filter.addFilter(PROPERTY_CLOSED, Boolean.FALSE);
            }

            final List<Notification> ns = dao.findForUser(user,
                    new Sorting(getDefaultSortOrder()),
                    filter,
                    page);

            final int total = dao.getEntityCount(user, filter);
            final JsonArray array = new JsonArray();
            final DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            for (final Notification t : ns) {
                array.add(ser.toJson(createNotificationItem(t, user, isoFormat)));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param n notification.
     * @param user user.
     * @return notification item.
     */
    private NotificationItem createNotificationItem(final Notification n, final User user, final DateFormat isoFormatter) {
        final NotificationItem item = new NotificationItem();
        item.setAlertId(n.getIssue().getId());
        if (n.getType() == NotificationType.Alert) {
            final Alert alert = (Alert) n.getIssue();
            item.setAlertType(alert.getType());
        }

        item.setClosed(n.isRead());
        item.setDate(isoFormatter.format(n.getIssue().getDate()));
        item.setNotificationId(n.getId());
        item.setShipmentId(n.getIssue().getShipment().getId());

        //set description.
        final String desc = descriptionBuilder.buildDescription(n.getIssue(), user);
        final int offset = desc.indexOf('.');

        item.setTitle(offset > -1 ? desc.substring(0, offset) : desc);
        item.setType(n.getType());

        item.getLines().add(offset > -1 ? desc.substring(offset + 1).trim() : desc);
        item.getLines().add("Some long shipment description here");
        item.getLines().add("About 200km from XYZ Warehouse");
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
    @RequestMapping(value = "/markNotificationsAsRead/{authToken}", method = RequestMethod.POST)
    public JsonObject markNotificationsAsRead(@PathVariable final String authToken,
            @RequestBody final JsonArray notificationIds) {
        try {
            //check logged in.
            final User user = authService.getUserForToken(authToken);
            getLoggedInUser(authToken);

            final Set<Long> ids = new HashSet<Long>();

            for (final JsonElement e : notificationIds) {
                ids.add(e.getAsLong());
            }

            dao.markAsReadenByUserAndId(user, ids);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
}
