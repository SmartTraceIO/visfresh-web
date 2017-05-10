/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.ShipmentAuditConstants;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentAuditsSerializer extends AbstractJsonSerializer
        implements ShipmentAuditConstants {

    /**
     * @param timeZone user time zone.
     */
    public ShipmentAuditsSerializer(final TimeZone timeZone) {
        super(timeZone);
    }

    /**
     * @param item shipment audit item.
     * @return JSON object.
     */
    public JsonObject toJson(final ShipmentAuditItem item) {
        final JsonObject json = new JsonObject();
        json.addProperty(ShipmentAuditConstants.ID, item.getId());
        json.addProperty(ShipmentAuditConstants.ACTION, item.getAction().toString());
        json.addProperty(ShipmentAuditConstants.SHIPMENT_ID, item.getShipmentId());
        json.addProperty(ShipmentAuditConstants.TIME, formatDate(item.getTime()));
        json.addProperty(ShipmentAuditConstants.USER_ID, item.getUserId() == null ? null : item.getUserId());
        json.add(ShipmentAuditConstants.ADDITIONAL_INFO, SerializerUtils.toJson(item.getAdditionalInfo()));
        return json;
    }

    public ShipmentAuditItem parseShipmentAuditItem(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();
        final ShipmentAuditItem item = new ShipmentAuditItem();
        item.setId(asLong(json.get(ShipmentAuditConstants.ID)));
        item.setAction(ShipmentAuditAction.valueOf(json.get(ShipmentAuditConstants.ACTION).getAsString()));
        item.setShipmentId(asLong(json.get(ShipmentAuditConstants.SHIPMENT_ID)));
        item.setTime(parseDate(json.get(ShipmentAuditConstants.TIME).getAsString()));
        item.setUserId(asLong(json.get(ShipmentAuditConstants.USER_ID)));
        item.getAdditionalInfo().putAll(SerializerUtils.parseStringMap(json.get(ADDITIONAL_INFO)));
        return item;
    }
}
