/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.NoteRestClient;
import com.visfresh.dao.NoteDao;
import com.visfresh.entities.Note;
import com.visfresh.entities.NoteType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.io.NoteDto;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoteControllerTest extends AbstractRestServiceTest {
    private NoteRestClient client = new NoteRestClient(UTC);
    private Shipment shipment;
    private User user;
    private DateFormat isoFormat;
    private NoteDao dao;

    /**
     * Default constructor.
     */
    public NoteControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        this.shipment = createShipment(true);
        this.dao = context.getBean(NoteDao.class);

        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
        user = context.getBean(AuthService.class).getUserForToken(client.getAuthToken());

        isoFormat = DateTimeUtils.createIsoFormat(user);
    }

    @Test
    public void testSaveNewByShipmentId() throws IOException, RestServiceException {
        final NoteDto dto = new NoteDto();

        final String noteText = "Note text";
        final String noteType = NoteType.Simple.name();
        final String timeOnChart = isoFormat.format(new Date(System.currentTimeMillis() - 1111111l));

        dto.setNoteText(noteText);
        dto.setNoteType(noteType);
        dto.setShipmentId(shipment.getId());
        dto.setTimeOnChart(timeOnChart);

        final int num = client.saveNote(dto);

        final Note note = dao.findByShipment(shipment).get(0);

        assertEquals(num, note.getNoteNum().intValue());
        assertEquals(user.getEmail(), note.getCreatedBy());
        assertNotNull(note.getCreationDate());
        assertEquals(noteText, note.getNoteText());
        assertEquals(noteType, note.getNoteType().name());
        assertEquals(timeOnChart, isoFormat.format(note.getTimeOnChart()));
    }
    @Test
    public void testSaveNewBySnTrip() throws IOException, RestServiceException {
        final NoteDto dto = new NoteDto();

        final String noteText = "Note text";
        final String noteType = NoteType.Simple.name();
        final String timeOnChart = isoFormat.format(new Date(System.currentTimeMillis() - 1111111l));

        dto.setNoteText(noteText);
        dto.setNoteType(noteType);
        dto.setTimeOnChart(timeOnChart);
        dto.setTrip(shipment.getTripCount());
        dto.setSn(shipment.getDevice().getSn());

        final int num = client.saveNote(dto);

        final Note note = context.getBean(NoteDao.class).findByShipment(shipment).get(0);

        assertEquals(num, note.getNoteNum().intValue());
        assertEquals(user.getEmail(), note.getCreatedBy());
        assertNotNull(note.getCreationDate());
        assertEquals(noteText, note.getNoteText());
        assertEquals(noteType, note.getNoteType().name());
        assertEquals(timeOnChart, isoFormat.format(note.getTimeOnChart()));
    }
    @Test
    public void testGetNotesByShipmentId() throws IOException, RestServiceException {
        createNote("A");
        createNote("B");

        final List<NoteDto> notes = client.getNotes(shipment.getId());
        assertEquals(2, notes.size());

        final NoteDto n = notes.get(1);
        assertEquals(2, n.getNoteNum().intValue());
        assertEquals("B", n.getNoteText());
    }
    @Test
    public void testGetNotesBySnTrip() throws IOException, RestServiceException {
        createNote("A");
        createNote("B");

        final List<NoteDto> notes = client.getNotes(shipment.getDevice().getSn(), shipment.getTripCount());
        assertEquals(2, notes.size());

        final NoteDto n = notes.get(1);
        assertEquals(2, n.getNoteNum().intValue());
        assertEquals("B", n.getNoteText());
    }
    @Test
    public void testUpdateExistingNote() throws IOException, RestServiceException {
        final Note n = createNote("A");

        final NoteDto dto = new NoteDto();
        dto.setNoteText("B");
        dto.setNoteType(n.getNoteType().name());
        dto.setNoteNum(n.getNoteNum());
        dto.setTimeOnChart(isoFormat.format(n.getTimeOnChart()));
        dto.setCreatedBy(n.getCreatedBy());
        dto.setCreationDate(isoFormat.format(n.getCreationDate()));

        dto.setTrip(shipment.getTripCount());
        dto.setSn(shipment.getDevice().getSn());

        final int num = client.saveNote(dto);
        assertEquals(n.getNoteNum().intValue(), num);

        final List<Note> notes = dao.findByShipment(shipment);
        assertEquals(1, notes.size());

        assertEquals("B", notes.get(0).getNoteText());
    }

    /**
     * @param text note text.
     */
    private Note createNote(final String text) {
        final Note n = new Note();
        n.setCreatedBy(user.getEmail());
        n.setCreationDate(new Date());
        n.setNoteText(text);
        n.setNoteType(NoteType.Simple);
        n.setTimeOnChart(new Date());
        return dao.save(shipment, n);
    }
}
