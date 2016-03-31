/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.NoteDao;
import com.visfresh.entities.Note;
import com.visfresh.entities.NoteType;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NoteDaoImpl implements NoteDao {
    /**
     * JDBC template.
     */
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public NoteDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.NoteDao#findByShipment(com.visfresh.entities.Shipment)
     */
    @Override
    public List<Note> findByShipment(final Shipment s) {
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", s.getId());

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from notes where shipment=:shipment and active order by notenum", params);
        final List<Note> notes = new LinkedList<>();
        for (final Map<String,Object> row : rows) {
            notes.add(createNote(row));
        }

        return notes;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NoteDao#findNote(com.visfresh.entities.Shipment, java.lang.Integer)
     */
    @Override
    public Note getNote(final Shipment s, final Integer noteNum) {
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", s.getId());
        params.put("notenum", noteNum);

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from notes where shipment=:shipment and notenum=:notenum", params);
        if (rows.size() > 0) {
            return createNote(rows.get(0));
        }

        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NoteDao#save(com.visfresh.entities.Shipment, com.visfresh.entities.Note)
     */
    @Override
    public Note save(final Shipment s, final Note note) {
        if (s == null) {
            return null;
        }

        final Map<String, Object> params = new HashMap<>();

        params.put("shipment", s.getId());
        params.put("notetext", note.getNoteText());
        params.put("createdby", note.getCreatedBy());
        params.put("createdon", note.getCreationDate());
        params.put("notetype", note.getNoteType().name());
        params.put("timeonchart", note.getTimeOnChart());
        params.put("active", note.isActive());

        if (note.getNoteNum() != null) {
            //update
            params.put("notenum", note.getNoteNum());
            final String sql = "update notes set notetext=:notetext,timeonchart=:timeonchart,"
                    + "notetype=:notetype,createdon=:createdon,createdby=:createdby,active=:active"
                    + " where shipment=:shipment and notenum=:notenum";
            jdbc.update(sql, params);
        } else {
            synchronized (this) {
                final List<Map<String, Object>> rows = jdbc.queryForList(
                        "select max(notenum) + 1 as nextNum from notes where shipment=:shipment",
                        params);
                final Number nextNum = (Number) rows.get(0).get("nextNum");
                final int noteNum = nextNum == null ? 1 : nextNum.intValue();
                params.put("notenum", noteNum);
                note.setNoteNum(noteNum);

                //insert
                final String sql = "insert into notes"
                        + "(shipment,notenum,notetext,timeonchart,notetype,createdon,createdby,active)"
                        + " values(:shipment,:notenum,:notetext,:timeonchart,:notetype,:createdon,:createdby,:active)";
                jdbc.update(sql, params);
            }
        }

        return note;
    }
    /**
     * @param row
     * @return
     */
    private Note createNote(final Map<String, Object> row) {
        final Note n = new Note();
        n.setCreatedBy((String) row.get("createdby"));
        n.setCreationDate((Date) row.get("createdon"));
        n.setNoteNum(((Number) row.get("notenum")).intValue());
        n.setNoteText((String) row.get("notetext"));
        n.setNoteType(NoteType.valueOf((String) row.get("notetype")));
        n.setTimeOnChart((Date) row.get("timeonchart"));
        n.setActive((Boolean) row.get("active"));
        return n;
    }
}
