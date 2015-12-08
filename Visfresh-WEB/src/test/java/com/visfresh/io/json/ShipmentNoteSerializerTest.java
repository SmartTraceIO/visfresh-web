/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.ShipmentNote;
import com.visfresh.io.SaveShipmentNoteRequest;
import com.visfresh.utils.SerializerUtils;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentNoteSerializerTest {
    /**
     * Serializer to test.
     */
    private ShipmentNoteSerializer serializer;

    /**
     * Default constructor.
     */
    public ShipmentNoteSerializerTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        serializer = new ShipmentNoteSerializer(SerializerUtils.UTС);
    }

    /**
     * Tests streaming of shipment note.
     */
    @Test
    public void testShipmentNote() {
        final String text = "Note text";
        final Long id = 777l;

        ShipmentNote note = new ShipmentNote();
        note.setText(text);
        note.setId(id);

        final JsonObject json = serializer.toJson(note);
        note = serializer.parseShipmentNote(json);

        assertEquals(text, note.getText());
        assertEquals(id, note.getId());
    }
    /**
     * Tests save shipment request serialization.
     */
    @Test
    public void testSaveShipmentNoteRequest() {
        final Long noteId = 7l;
        final String noteText = "Note Text";
        final Long shipmentId = 77l;
        final Long userId = 777l;

        SaveShipmentNoteRequest r = new SaveShipmentNoteRequest();
        r.setNoteId(noteId);
        r.setNoteText(noteText);
        r.setShipmentId(shipmentId);
        r.setUserId(userId);

        final JsonObject json = serializer.toJson(r);
        r = serializer.parseSaveShipmentNoteRequest(json);

        assertEquals(noteId, r.getNoteId());
        assertEquals(noteText, r.getNoteText());
        assertEquals(shipmentId, r.getShipmentId());
        assertEquals(userId, r.getUserId());
    }
}
