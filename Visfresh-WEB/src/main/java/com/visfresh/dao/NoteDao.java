/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Note;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NoteDao {
    /**
     * @param s shipment.
     * @return list of notes.
     */
    List<Note> findByShipment(Shipment s);
    /**
     * @param s shipment.
     * @param note shipment note.
     * @return saved note.
     */
    Note save(Shipment s, Note note);
}
