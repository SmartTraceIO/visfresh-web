/**
 *
 */
package com.visfresh.io.json;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.sms.SmsMessage;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SmsSerializer extends AbstractJsonSerializer {

    /**
     * Default constructor.
     */
    public SmsSerializer() {
        super(SerializerUtils.UTС);
    }

    /**
     * @param msg email message.
     * @return JSON object.
     */
    public JsonObject toJson(final SmsMessage msg) {
        if (msg == null) {
            return null;
        }

        final JsonObject json = new JsonObject();

        json.addProperty("message", msg.getMessage());
        json.addProperty("subject", msg.getSubject());

        final JsonArray phones = new JsonArray();
        for (final String phone : msg.getPhones()) {
            phones.add(new JsonPrimitive(phone));
        }
        json.add("phones", phones);

        return json;
    }
    /**
     * @param e JSON element.
     * @return email message.
     */
    public SmsMessage parseSmsMessage(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final SmsMessage msg = new SmsMessage();

        msg.setMessage(asString(json.get("message")));
        msg.setSubject(asString(json.get("subject")));

        final List<String> emails = new LinkedList<String>();
        for (final JsonElement el : json.get("phones").getAsJsonArray()) {
            emails.add(el.getAsString());
        }
        msg.setPhones(emails.toArray(new String[emails.size()]));

        return msg;
    }

}
