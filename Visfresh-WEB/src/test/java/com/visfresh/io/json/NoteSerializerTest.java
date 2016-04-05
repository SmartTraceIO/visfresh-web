/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.Note;
import com.visfresh.io.NoteDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoteSerializerTest {

    private NoteSerializer serializer;

    /**
     * Default constructor.
     */
    public NoteSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new NoteSerializer(TimeZone.getDefault());
    }

    @Test
    public void testSerialize() {
        final boolean activeflag = true;
        final String createdBy = "vchapaev@smarttrace.com.au";
        final Integer noteNum = 8;
        final String noteText = "Note text";
        final String noteType = "AnyType";
        final Long shipmentId = 7l;
        final String sn = "12345";
        final String timeOnChart = "2016-31-03 11:11";
        final Integer trip = 9;
        final String createdByName = "createdByName";

        NoteDto n = new NoteDto();
        n.setActiveFlag(activeflag);
        n.setCreatedBy(createdBy);
        n.setNoteNum(noteNum);
        n.setNoteText(noteText);
        n.setNoteType(noteType);
        n.setShipmentId(shipmentId);
        n.setSn(sn);
        n.setTimeOnChart(timeOnChart);
        n.setTrip(trip);
        n.setCreatedByName(createdByName);

        n = serializer.parseNoteDto(serializer.toJson(n));

        assertEquals(activeflag, n.isActiveFlag());
        assertEquals(createdBy, n.getCreatedBy());
        assertEquals(noteNum, n.getNoteNum());
        assertEquals(noteText, n.getNoteText());
        assertEquals(noteType, n.getNoteType());
        assertEquals(shipmentId, n.getShipmentId());
        assertEquals(sn, n.getSn());
        assertEquals(timeOnChart, n.getTimeOnChart());
        assertEquals(trip, n.getTrip());
        assertEquals(createdByName, n.getCreatedByName());
    }
    @Test
    public void testSerializeWithEmptyValues() {
        final NoteDto n = new NoteDto();
        //check not exceptions.
        serializer.parseNoteDto(serializer.toJson(n));
    }
    @Test
    public void testSaveResponse() {
        final Note note = new Note();
        note.setNoteNum(8);

        final JsonObject resp = serializer.createSaveResponse(note);
        assertEquals(8, resp.get("noteNum").getAsInt());
    }
}
