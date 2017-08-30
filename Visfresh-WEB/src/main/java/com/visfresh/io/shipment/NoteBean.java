/**
 *
 */
package com.visfresh.io.shipment;

import java.util.Date;

import com.visfresh.entities.Note;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoteBean {
    private String noteText;
    private Date timeOnChart;
    private String noteType;
    private Integer noteNum;
    private Date creationDate;
    private String createdBy;
    private boolean active = true;
    private String createdByName;

    /**
     * Default constructor.
     */
    public NoteBean() {
        super();
    }
    /**
     * @param n note.
     */
    public NoteBean(final Note n) {
        super();
        setNoteText(n.getNoteText());
        setTimeOnChart(n.getTimeOnChart());
        setNoteType(n.getNoteType());
        setNoteNum(n.getNoteNum());
        setCreationDate(n.getCreationDate());
        setCreatedBy(n.getCreatedBy());
        setActive(n.isActive());
        setCreatedByName(n.getCreateCreatedByName());
    }

    /**
     * @return the noteText
     */
    public String getNoteText() {
        return noteText;
    }
    /**
     * @param noteText the noteText to set
     */
    public void setNoteText(final String noteText) {
        this.noteText = noteText;
    }
    /**
     * @return the timeOnChart
     */
    public Date getTimeOnChart() {
        return timeOnChart;
    }
    /**
     * @param timeOnChart the timeOnChart to set
     */
    public void setTimeOnChart(final Date timeOnChart) {
        this.timeOnChart = timeOnChart;
    }
    /**
     * @return the noteType
     */
    public String getNoteType() {
        return noteType;
    }
    /**
     * @param noteType the noteType to set
     */
    public void setNoteType(final String noteType) {
        this.noteType = noteType;
    }
    /**
     * @return the noteNum
     */
    public Integer getNoteNum() {
        return noteNum;
    }
    /**
     * @param noteNum the noteNum to set
     */
    public void setNoteNum(final Integer noteNum) {
        this.noteNum = noteNum;
    }
    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }
    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }
    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }
    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }
    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
    /**
     * @param active the active to set
     */
    public void setActive(final boolean active) {
        this.active = active;
    }
    /**
     * @return the createCreatedByName
     */
    public String getCreatedByName() {
        return createdByName;
    }
    /**
     * @param name name of creator.
     */
    public void setCreatedByName(final String name) {
        this.createdByName = name;
    }
}
