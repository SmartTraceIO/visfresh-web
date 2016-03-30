/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.NoteConstants;
import com.visfresh.entities.Note;
import com.visfresh.io.NoteDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoteSerializer extends AbstractJsonSerializer implements NoteConstants {
    /**
     * @param tz time zone.
     */
    public NoteSerializer(final TimeZone tz) {
        super(tz);
    }

    /**
     * @param dto note DTO.
     * @return
     */
    public JsonObject toJson(final NoteDto dto) {
        final JsonObject json = new JsonObject();

        json.addProperty(ACTIVE_FLAG, dto.isActiveFlag());
        json.addProperty(CREATED_BY, dto.getCreatedBy());
        json.addProperty(CREATION_DATE, dto.getCreationDate());
        json.addProperty(NOTE_NUM, dto.getNoteNum());
        json.addProperty(NOTE_TEXT, dto.getNoteText());
        json.addProperty(SHIPMENT_ID, dto.getShipmentId());
        json.addProperty(NOTE_TYPE, dto.getNoteType());
        json.addProperty(SN, dto.getSn());
        json.addProperty(TRIP, dto.getTrip());
        json.addProperty(TIME_ON_CHART, dto.getTimeOnChart());

        return json;
    }
    /**
     * @param e JSON element.
     * @return Note DTO
     */
    public NoteDto parseNoteDto(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final NoteDto dto = new NoteDto();

        dto.setActiveFlag(!Boolean.FALSE.equals(asBoolean(json.get(ACTIVE_FLAG))));
        dto.setCreatedBy(asString(json.get(CREATED_BY)));
        dto.setCreationDate(asString(json.get(CREATION_DATE)));
        dto.setNoteNum(asInt(json.get(NOTE_NUM)));
        dto.setNoteText(asString(json.get(NOTE_TEXT)));
        dto.setShipmentId(asLong(json.get(SHIPMENT_ID)));
        dto.setNoteType(asString(json.get(NOTE_TYPE)));
        dto.setSn(asString(json.get(SN)));
        dto.setTrip(asInteger(json.get(TRIP)));
        dto.setTimeOnChart(asString(json.get(TIME_ON_CHART)));

        return dto;
    }
    /**
     * @param note note.
     * @return save note response
     */
    public JsonObject createSaveResponse(final Note note) {
        final JsonObject json = new JsonObject();
        json.addProperty(NOTE_NUM, note.getNoteNum());
        return json;
    }
}
