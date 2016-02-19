/**
 *
 */
package com.visfresh.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Location;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.NotificationItem;
import com.visfresh.io.json.NotificationSerializer;
import com.visfresh.mpl.services.AlertBundle;
import com.visfresh.mpl.services.NotificationBundle;

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
    private AlertBundle descriptionBuilder;
    @Autowired
    private NotificationBundle notificationBundle;
    @Autowired
    private TrackerEventDao trackerEventDao;

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
                    new Sorting(false, getDefaultSortOrder()),
                    filter,
                    page);
            //create notification to location map
            final Map<Long, Location> locations = createLocationMap(ns);
            final Map<Long, TrackerEvent> events = getTrackerEvents(ns);

            final int total = dao.getEntityCount(user, filter);
            final JsonArray array = new JsonArray();
            final DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            for (final Notification t : ns) {
                array.add(ser.toJson(createNotificationItem(t, user, locations.get(t.getId()),
                        isoFormat, events)));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get notifications", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param ns notifications.
     * @return
     */
    private Map<Long, TrackerEvent> getTrackerEvents(final List<Notification> ns) {
        final Set<Long> ids = new HashSet<Long>();
        for (final Notification n : ns) {
            final Long trackerEventId = n.getIssue().getTrackerEventId();
            if (trackerEventId != null) {
                ids.add(trackerEventId);
            }
        }

        final List<TrackerEvent> events = trackerEventDao.findAll(ids);
        final Map<Long, TrackerEvent> map = new HashMap<Long, TrackerEvent>();
        for (final TrackerEvent e : events) {
            map.put(e.getId(), e);
        }
        return map;
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
            log.error("Failed to mark notificaiton as read", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param n notification.
     * @param user user.
     * @return notification item.
     */
    private NotificationItem createNotificationItem(final Notification n, final User user,
            final Location location, final DateFormat isoFormatter,
            final Map<Long, TrackerEvent> trackerEvents) {
        final NotificationItem item = new NotificationItem();
        item.setAlertId(n.getIssue().getId());
        if (n.getType() == NotificationType.Alert) {
            final Alert alert = (Alert) n.getIssue();
            item.setAlertType(alert.getType().name());
        } else if (n.getType() == NotificationType.Arrival) {
            item.setAlertType("ArrivalNotice");
        }

        item.setClosed(n.isRead());
        item.setDate(isoFormatter.format(n.getIssue().getDate()));
        item.setNotificationId(n.getId());

        final Shipment shipment = n.getIssue().getShipment();
        item.setShipmentId(shipment.getId());

        final TrackerEvent trackerEvent = trackerEvents.get(n.getIssue().getTrackerEventId());

        //set description.
        String title = "";
        if (n.getType() == NotificationType.Alert) {
            title = ((Alert) n.getIssue()).getType().name();
        } else if (n.getType() == NotificationType.Arrival) {
            title = "Arrival notification";
        }

        item.setTitle(title);
        item.setType("Alert");

        final String[] lines = notificationBundle.getAppMessage(user, n.getIssue(), trackerEvent).split("\n");
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
    /**
     * @param ns list of notifications.
     * @return map of notification to location.
     */
    private Map<Long, Location> createLocationMap(final List<Notification> ns) {
        final Map<Long, List<TrackerEvent>> eventsCache = new HashMap<>();
        final Map<Long, Location> map = new HashMap<>();

        for (final Notification n : ns) {
            //get events for given shipment.
            final Shipment shipment = n.getIssue().getShipment();
            List<TrackerEvent> events = eventsCache.get(shipment.getId());
            if (events == null) {
                events = trackerEventDao.getEvents(shipment);
                eventsCache.put(shipment.getId(), events);
            }

            //find nearest event for given shipment.
            final TrackerEvent e = getNearestEvent(events, n.getIssue().getDate());
            if (e != null) {
                map.put(n.getId(), new Location(e.getLatitude(), e.getLongitude()));
            }
        }

        return map;
    }
    /**
     * @param items list of tracker events.
     * @param date date.
     * @return nearest event for given date.
     */
    private TrackerEvent getNearestEvent(final List<TrackerEvent> items, final Date date) {
        if (items.isEmpty()) {
            return null;
        }

        for (final TrackerEvent i : items) {
            if (near(i.getTime(), date) || i.getTime().after(date)) {
                return i;
            }
        }
        return items.get(items.size() - 1);
    }
    /**
     * @param d1 first date.
     * @param d2 second date.
     * @return true if the difference between two dates is less then one second.
     */
    private boolean near(final Date d1, final Date d2) {
        return Math.abs(d1.getTime() - d2.getTime()) < 1000l;
    }
}
