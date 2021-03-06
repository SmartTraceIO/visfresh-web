/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.visfresh.constants.NotificationConstants;
import com.visfresh.io.NotificationItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationSerializer extends AbstractJsonSerializer {
    private static final String PROPERTY_LINK = "link";

    /**
     * @param tz time zone.
     */
    public NotificationSerializer(final TimeZone tz) {
        super(tz);
    }

    public NotificationItem parseNotification(final JsonObject json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        final NotificationItem n = new NotificationItem();

        n.setNotificationId(asLong(json.get(NotificationConstants.PROPERTY_NOTIFICATION_ID)));
        n.setClosed(asBoolean(json.get(NotificationConstants.PROPERTY_CLOSED)));
        n.setDate(asString(json.get(NotificationConstants.PROPERTY_DATE)));
        n.setType(asString(json.get(NotificationConstants.PROPERTY_TYPE)));
        n.setAlertType(asString(json.get(NotificationConstants.PROPERTY_ALERT_TYPE)));
        n.setAlertId(asLong(json.get(NotificationConstants.PROPERTY_ALERT_ID)));
        n.setShipmentId(asLong(json.get(NotificationConstants.PROPERTY_SHIPMENT_ID)));
        n.setTitle(asString(json.get(NotificationConstants.PROPERTY_TITLE)));
        n.setLink(asString(json.get(PROPERTY_LINK)));

        int i = 1;
        while (true) {
            final String name = createLineKey(i);
            if (!json.has(name)) {
                break;
            }

            n.getLines().add(asString(json.get(name)));
            i++;
        }

        return n;
    }
    /**
     * @param n notification.
     * @return JSON object.
     */
    public JsonObject toJson(final NotificationItem n) {
        if (n == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();

        obj.addProperty(NotificationConstants.PROPERTY_NOTIFICATION_ID, n.getNotificationId());
        obj.addProperty(NotificationConstants.PROPERTY_CLOSED, n.isClosed());
        obj.addProperty(NotificationConstants.PROPERTY_DATE, n.getDate());
        obj.addProperty(NotificationConstants.PROPERTY_TYPE, n.getType());
        obj.addProperty(NotificationConstants.PROPERTY_ALERT_TYPE, n.getAlertType());
        obj.addProperty(NotificationConstants.PROPERTY_ALERT_ID, n.getAlertId());
        obj.addProperty(NotificationConstants.PROPERTY_SHIPMENT_ID, n.getShipmentId());
        obj.addProperty(NotificationConstants.PROPERTY_TITLE, n.getTitle());
        obj.addProperty(PROPERTY_LINK, n.getLink());

        int i = 1;
        for (final String line : n.getLines()) {
            obj.addProperty(createLineKey(i), line);
            i++;
        }
        return obj;
    }

    /**
     * @param lineNumber
     * @return
     */
    private static String createLineKey(final int lineNumber) {
        return "Line" + lineNumber;
    }
}
