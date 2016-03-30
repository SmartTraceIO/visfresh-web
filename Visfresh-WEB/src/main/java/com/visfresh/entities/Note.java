/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Note {
    private String noteText;
    private Date timeOnChart;
    private NoteType noteType;
    private Integer noteNum;
    private Date creationDate = new Date();
    private String createdBy;

    /**
     * Default constructor.
     */
    public Note() {
        super();
    }

    /**
     * @return the notetext
     */
    public String getNoteText() {
        return noteText;
    }
    /**
     * @param notetext the notetext to set
     */
    public void setNoteText(final String notetext) {
        this.noteText = notetext;
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
     * @return the notetype
     */
    public NoteType getNoteType() {
        return noteType;
    }
    /**
     * @param notetype the notetype to set
     */
    public void setNoteType(final NoteType notetype) {
        this.noteType = notetype;
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
}
