/**
 *
 */
package com.visfresh.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.visfresh.dao.NotificationDao;
import com.visfresh.entities.Notification;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("Notification")
@RequestMapping("/rest")
public class NotificationController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationDao dao;

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
    public @ResponseBody String getNotifications(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex, @RequestParam(required = false) final Integer pageSize) {
        final int page = pageIndex == null ? 1 : pageIndex.intValue();
        final int size = pageSize == null ? Integer.MAX_VALUE : pageSize.intValue();

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            final EntityJSonSerializer ser = getSerializer(user);

            final List<Notification> ns = dao.findForUser(user);
            sort(ns);

            final int total = ns.size();
            final List<Notification> shipments = getPage(ns, page, size);
            final JsonArray array = new JsonArray();
            for (final Notification t : shipments) {
                array.add(ser.toJson(t));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param ns
     */
    private void sort(final List<Notification> ns) {
        sortById(ns, true);
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

            dao.deleteByUserAndId(user, ids);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
}
