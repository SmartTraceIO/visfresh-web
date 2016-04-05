/**
 *
 */
package com.visfresh.io;



/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NoteDto {
    private String noteText;
    private String timeOnChart;
    private String noteType;
    private Integer noteNum;
    private String creationDate;
    private String createdBy;
    private Long shipmentId;
    private String sn;
    private Integer trip;
    private String createdByName;
    private boolean activeFlag = true;

    /**
     * Default constructor.
     */
    public NoteDto() {
        super();
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
    public String getTimeOnChart() {
        return timeOnChart;
    }
    /**
     * @param timeOnChart the timeOnChart to set
     */
    public void setTimeOnChart(final String timeOnChart) {
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
    public String getCreationDate() {
        return creationDate;
    }
    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(final String creationDate) {
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
     * @return the shipmentId
     */
    public Long getShipmentId() {
        return shipmentId;
    }
    /**
     * @param shipmentId the shipmentId to set
     */
    public void setShipmentId(final Long shipmentId) {
        this.shipmentId = shipmentId;
    }
    /**
     * @return the sn
     */
    public String getSn() {
        return sn;
    }
    /**
     * @param sn the sn to set
     */
    public void setSn(final String sn) {
        this.sn = sn;
    }
    /**
     * @return the trip
     */
    public Integer getTrip() {
        return trip;
    }
    /**
     * @param trip the trip to set
     */
    public void setTrip(final Integer trip) {
        this.trip = trip;
    }
    /**
     * @return the activeflag
     */
    public boolean isActiveFlag() {
        return activeFlag;
    }
    /**
     * @param activeflag the activeflag to set
     */
    public void setActiveFlag(final boolean activeflag) {
        this.activeFlag = activeflag;
    }
    /**
     * @return the createdByName
     */
    public String getCreatedByName() {
        return createdByName;
    }
    /**
     * @param createdByName the createdByName to set
     */
    public void setCreatedByName(final String createdByName) {
        this.createdByName = createdByName;
    }
}
