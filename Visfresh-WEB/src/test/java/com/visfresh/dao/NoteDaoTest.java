/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Note;
import com.visfresh.entities.NoteType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoteDaoTest extends BaseDaoTest<NoteDao> {
    private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public NoteDaoTest() {
        super(NoteDao.class);
    }

    @Before
    public void setUp() {
        final Device d = new Device();
        d.setName("Test Device");
        d.setImei("23984293087034");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        getContext().getBean(DeviceDao.class).save(d);

        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(ShipmentStatus.InProgress);
        this.shipment = getContext().getBean(ShipmentDao.class).save(s);
    }

    @Test
    public void testInsert() {
        final String createdBy = "created by";
        final Date creationDate = new Date(System.currentTimeMillis() - 5555555l);
        final String noteText = "Note text";
        final NoteType noteType = NoteType.Simple;
        final Date timeOnChart = new Date(System.currentTimeMillis() - 4444444l);

        Note n = new Note();
        n.setCreatedBy(createdBy);
        n.setCreationDate(creationDate);
        n.setNoteText(noteText);
        n.setNoteType(noteType);
        n.setTimeOnChart(timeOnChart);

        final Integer noteNum = dao.save(shipment, n).getNoteNum();
        final List<Note> notes = dao.findByShipment(shipment);
        assertEquals(1, notes.size());

        n = notes.get(0);
        assertEquals(createdBy, n.getCreatedBy());
        assertEquals(format.format(creationDate), format.format(n.getCreationDate()));
        assertEquals(noteNum, n.getNoteNum());
        assertEquals(noteText, n.getNoteText());
        assertEquals(noteType, n.getNoteType());
        assertEquals(format.format(timeOnChart), format.format(n.getTimeOnChart()));
    }
    @Test
    public void testUpdate() {
        Note n = createNote("A");

        //test not update if incorrect noteNum
        n.setNoteNum(55);
        dao.save(shipment, n);

        assertEquals(0, dao.findByShipment(shipment).size());

        //save note
        n.setNoteNum(null);
        dao.save(shipment, n);
        assertEquals(1, dao.findByShipment(shipment).size());

        final String createdBy = "created by new";
        final Date creationDate = new Date(System.currentTimeMillis() - 555l);
        final String noteText = "Note text new";
        final NoteType noteType = NoteType.Simple;
        final Date timeOnChart = new Date(System.currentTimeMillis() - 444l);

        n.setCreatedBy(createdBy);
        n.setCreationDate(creationDate);
        n.setNoteText(noteText);
        n.setNoteType(noteType);
        n.setTimeOnChart(timeOnChart);
        final int noteNum = dao.save(shipment, n).getNoteNum();

        final List<Note> notes = dao.findByShipment(shipment);
        assertEquals(1, notes.size());

        n = notes.get(0);
        assertEquals(createdBy, n.getCreatedBy());
        assertEquals(format.format(creationDate), format.format(n.getCreationDate()));
        assertEquals(noteNum, n.getNoteNum().intValue());
        assertEquals(noteText, n.getNoteText());
        assertEquals(noteType, n.getNoteType());
        assertEquals(format.format(timeOnChart), format.format(n.getTimeOnChart()));
    }
    @Test
    public void testGetForShipment() {
        dao.save(shipment, createNote("A"));
        dao.save(shipment, createNote("B"));
        dao.save(shipment, createNote("C"));

        final List<Note> notes = dao.findByShipment(shipment);
        assertEquals(3, notes.size());
        assertEquals(3, notes.get(2).getNoteNum().intValue());

        //test select by left shipment
        Shipment s = new Shipment();
        s.setDevice(shipment.getDevice());
        s.setCompany(shipment.getCompany());
        s.setStatus(ShipmentStatus.InProgress);
        s = getContext().getBean(ShipmentDao.class).save(s);

        assertEquals(0, dao.findByShipment(s).size());
    }
    @Test
    public void testGetNote() {
        final int noteNum = dao.save(shipment, createNote("A")).getNoteNum();

        final Note n = dao.getNote(shipment, noteNum);
        assertNotNull(n);
        assertEquals("A", n.getNoteText());

        //test select by left shipment
        Shipment s = new Shipment();
        s.setDevice(shipment.getDevice());
        s.setCompany(shipment.getCompany());
        s.setStatus(ShipmentStatus.InProgress);
        s = getContext().getBean(ShipmentDao.class).save(s);

        assertNull(dao.getNote(s, noteNum));
    }
    @Test
    public void testDeactivateNote() {
        final Note a = dao.save(shipment, createNote("A"));
        dao.save(shipment, createNote("B"));
        dao.save(shipment, createNote("C"));

        a.setActive(false);
        dao.save(shipment, a);

        final List<Note> notes = dao.findByShipment(shipment);
        assertEquals(2, notes.size());
        assertEquals("B", notes.get(0).getNoteText());
    }
    /**
     * @param noteText
     * @return
     */
    private Note createNote(final String noteText) {
        final Note n = new Note();
        n.setCreatedBy("created by");
        n.setCreationDate(new Date(System.currentTimeMillis() - 5555555l));
        n.setNoteText(noteText);
        n.setNoteType(NoteType.Simple);
        n.setTimeOnChart(new Date(System.currentTimeMillis() - 4444444l));
        return n;
    }
}
