/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.visfresh.constants.NotificationConstants;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.io.DeviceResolver;
import com.visfresh.io.ShipmentResolver;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationSerializer extends AbstractJsonSerializer {
    private final AlertSerializer alertSerializer;
    private final ArrivalSerializer arrivalSerializer;

    /**
     * @param tz time zone.
     */
    public NotificationSerializer(final TimeZone tz) {
        super(tz);
        this.alertSerializer = new AlertSerializer(tz);
        this.arrivalSerializer = new ArrivalSerializer(tz);
    }

    public Notification parseNotification(final JsonObject json) {
        final Notification n = new Notification();
        n.setId(asLong(json.get(NotificationConstants.PROPERTY_ID)));
        n.setType(NotificationType.valueOf(asString(json.get(NotificationConstants.PROPERTY_TYPE))));

        switch (n.getType()) {
            case Alert:
                n.setIssue(alertSerializer.parseAlert(json.get("issue").getAsJsonObject()));
                break;
            case Arrival:
                n.setIssue(arrivalSerializer.parseArrival(json.get("issue").getAsJsonObject()));
                break;
        }

        return n;
    }
    /**
     * @param n notification.
     * @return JSON object.
     */
    public JsonObject toJson(final Notification n) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(NotificationConstants.PROPERTY_ID, n.getId());
        obj.addProperty(NotificationConstants.PROPERTY_TYPE, n.getType().name());

        final Object issue = n.getIssue();
        if (issue instanceof Alert) {
            obj.add("issue", alertSerializer.toJson((Alert) issue));
        } else if (issue instanceof Arrival) {
            obj.add("issue", arrivalSerializer.toJson((Arrival) issue));
        } else {
            throw new IllegalArgumentException("Unexpected alert issue " + issue);
        }
        return obj;
    }
    /**
     * @param r the shipmentResolver to set
     */
    public void setShipmentResolver(final ShipmentResolver r) {
        arrivalSerializer.setShipmentResolver(r);
        alertSerializer.setShipmentResolver(r);
    }
    /**
     * @param r the deviceResolver to set
     */
    public void setDeviceResolver(final DeviceResolver r) {
        arrivalSerializer.setDeviceResolver(r);
        alertSerializer.setDeviceResolver(r);
    }
}
