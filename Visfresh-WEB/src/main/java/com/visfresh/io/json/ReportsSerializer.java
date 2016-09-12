/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.io.EmailShipmentReportRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReportsSerializer extends AbstractJsonSerializer {
    private static final String TYPE_EMAIL = "email";
    private static final String VALUE = "value";
    private static final String TYPE_USER = "user";
    private static final String TYPE = "type";
    private static final String RECIPIENTS = "recipients";
    private static final String MESSAGE_BODY = "messageBody";
    private static final String SUBJECT = "subject";
    private static final String TRIP = "trip";
    private static final String SN = "sn";

    /**
     * @param tz time zone.
     */
    public ReportsSerializer() {
        super(TimeZone.getDefault());
    }

    /**
     * @param req email Shipment Request
     * @return JSON object.
     */
    public JsonObject toJson(final EmailShipmentReportRequest req) {
        final JsonObject json = new JsonObject();
        json.addProperty(SN, req.getSn());
        json.addProperty(TRIP, req.getTrip());
        json.addProperty(SUBJECT, req.getSubject());
        json.addProperty(MESSAGE_BODY, req.getMessageBody());

        //recipients
        final JsonArray recipients = new JsonArray();
        json.add(RECIPIENTS, recipients);

        //users
        for (final Long id : req.getUsers()) {
            final JsonObject u = new JsonObject();
            u.addProperty(TYPE, TYPE_USER);
            u.addProperty(VALUE, id);
            recipients.add(u);
        }

        //emails
        for (final String email : req.getEmails()) {
            final JsonObject e = new JsonObject();
            e.addProperty(TYPE, TYPE_EMAIL);
            e.addProperty(VALUE, email);
            recipients.add(e);
        }

        return json;
    }

    public EmailShipmentReportRequest parseEmailShipmentReportRequest(final JsonObject json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        final EmailShipmentReportRequest req = new EmailShipmentReportRequest();
        req.setSn(asString(json.get(SN)));
        req.setTrip(asInt(json.get(TRIP)));
        req.setSubject(asString(json.get(SUBJECT)));
        req.setMessageBody(asString(json.get(MESSAGE_BODY)));

        //users and emails
        final JsonArray array = json.get(RECIPIENTS).getAsJsonArray();
        for (final JsonElement e : array) {
            final JsonObject o = e.getAsJsonObject();
            final String type = o.get(TYPE).getAsString();

            if (TYPE_USER.equals(type)) {
                req.getUsers().add(o.get(VALUE).getAsLong());
            } else {
                req.getEmails().add(o.get(VALUE).getAsString());
            }
        }

        return req;
    }
}
