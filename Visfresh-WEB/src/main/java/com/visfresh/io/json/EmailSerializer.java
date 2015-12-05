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
import com.visfresh.io.email.EmailMessage;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EmailSerializer extends AbstractJsonSerializer {
    /**
     * Default constructor.
     */
    public EmailSerializer() {
        super(SerializerUtils.UTС);
    }

    /**
     * @param msg email message.
     * @return JSON object.
     */
    public JsonObject toJson(final EmailMessage msg) {
        if (msg == null) {
            return null;
        }

        final JsonObject json = new JsonObject();

        json.addProperty("message", msg.getMessage());
        json.addProperty("subject", msg.getSubject());

        final JsonArray emails = new JsonArray();
        for (final String email : msg.getEmails()) {
            emails.add(new JsonPrimitive(email));
        }
        json.add("emails", emails);

        return json;
    }
    /**
     * @param e JSON element.
     * @return email message.
     */
    public EmailMessage parseEmailMessage(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final EmailMessage msg = new EmailMessage();

        msg.setMessage(asString(json.get("message")));
        msg.setSubject(asString(json.get("subject")));

        final List<String> emails = new LinkedList<String>();
        for (final JsonElement el : json.get("emails").getAsJsonArray()) {
            emails.add(el.getAsString());
        }
        msg.setEmails(emails.toArray(new String[emails.size()]));

        return msg;
    }
}
