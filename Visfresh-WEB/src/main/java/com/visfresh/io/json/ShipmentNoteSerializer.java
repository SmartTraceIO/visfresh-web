/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.ShipmentNote;
import com.visfresh.io.SaveShipmentNoteRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentNoteSerializer extends AbstractJsonSerializer {
    /**
     * @param tz time zone.
     */
    public ShipmentNoteSerializer(final TimeZone tz) {
        super(tz);
    }

    /**
     * @param note shipment note.
     * @return
     */
    public JsonObject toJson(final ShipmentNote note) {
        if (note == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("id", note.getId());
        json.addProperty("text", note.getText());
        return json;
    }
    /**
     * @param e JSON element.
     * @return shipment note.
     */
    public ShipmentNote parseShipmentNote(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }
        final JsonObject json = e.getAsJsonObject();

        final ShipmentNote note = new ShipmentNote();
        note.setText(asString(json.get("text")));
        note.setId(asLong(json.get("id")));
        return note;
    }
    /**
     * @param r save shipment note request.
     * @return save shipment note request as JSON object.
     */
    public JsonObject toJson(final SaveShipmentNoteRequest r) {
        if (r == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty("noteId", r.getNoteId());
        json.addProperty("noteText", r.getNoteText());
        json.addProperty("userId", r.getUserId());
        json.addProperty("shipmentId", r.getShipmentId());
        return json;
    }
    /**
     * @param json JSON object.
     * @return save shipment request.
     */
    public SaveShipmentNoteRequest parseSaveShipmentNoteRequest(final JsonObject json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        final SaveShipmentNoteRequest r = new SaveShipmentNoteRequest();
        r.setNoteId(asLong(json.get("noteId")));
        r.setNoteText(asString(json.get("noteText")));
        r.setUserId(asLong(json.get("userId")));
        r.setShipmentId(asLong(json.get("shipmentId")));

        return r;
    }
}
