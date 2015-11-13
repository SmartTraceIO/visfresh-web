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
import com.visfresh.io.DeviceResolver;
import com.visfresh.io.ShipmentResolver;
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
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return notifications for given shipment.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<Notification> getNotifications(final Integer pageIndex, final Integer pageSize)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }

        final JsonArray response = sendGetRequest(getPathWithToken("getNotifications"),
                params).getAsJsonArray();
        final List<Notification> notifications = new ArrayList<Notification>(response.size());
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

    public void setDeviceResolver(final DeviceResolver r) {
        serializer.setDeviceResolver(r);
    }
    public void setShipmentResolver(final ShipmentResolver r) {
        serializer.setShipmentResolver(r);
    }
}
