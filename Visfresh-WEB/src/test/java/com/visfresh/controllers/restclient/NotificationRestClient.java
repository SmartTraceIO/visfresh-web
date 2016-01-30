/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.visfresh.entities.Notification;
import com.visfresh.io.NotificationItem;
import com.visfresh.io.json.NotificationSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationRestClient extends RestClient {
    private NotificationSerializer serializer;
    /**
     *
     */
    public NotificationRestClient(final TimeZone tz) {
        super();
        serializer = new NotificationSerializer(tz);
    }
    /**
     * @param includeRead include read flag
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return notifications for given shipment.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<NotificationItem> getNotifications(final boolean includeRead,
            final Integer pageIndex, final Integer pageSize)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        if (includeRead) {
            params.put("includeRead", "true");
        }

        final JsonArray response = sendGetRequest(getPathWithToken("getNotifications"),
                params).getAsJsonArray();
        final List<NotificationItem> notifications = new ArrayList<>(response.size());
        for (int i = 0; i < response.size(); i++) {
            notifications.add(serializer.parseNotification(response.get(i).getAsJsonObject()));
        }
        return notifications;
    }
    /**
     * @param toReaden
     * @throws RestServiceException
     * @throws IOException
     */
    public void markNotificationsAsRead(final List<Notification> toReaden) throws IOException, RestServiceException {
        final JsonArray req = new JsonArray();
        for (final Notification n : toReaden) {
            req.add(new JsonPrimitive(n.getId()));
        }

        sendPostRequest(getPathWithToken("markNotificationsAsRead"), req);
    }
}
