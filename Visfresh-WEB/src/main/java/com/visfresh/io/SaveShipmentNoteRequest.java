/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.ShipmentNote;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SaveShipmentNoteRequest {
    /**
     * Shipment ID.
     */
    private Long shipmentId;
    /**
     * User ID.
     */
    private Long userId;
    /**
     * Note ID.
     */
    private Long noteId;
    /**
     * Note Text
     */
    private String noteText;

    /**
     * Default constructor.
     */
    public SaveShipmentNoteRequest() {
        super();
    }
    /**
     * @param note shipment note.
     */
    public SaveShipmentNoteRequest(final ShipmentNote note) {
        super();
        if (note.getShipment() != null) {
            setShipmentId(note.getShipment().getId());
        }
        if (note.getUser() != null) {
            setUserId(note.getUser().getId());
        }
        setNoteId(note.getId());
        setNoteText(note.getText());
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
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }
    /**
     * @param userId the userId to set
     */
    public void setUserId(final Long userId) {
        this.userId = userId;
    }
    /**
     * @return the noteId
     */
    public Long getNoteId() {
        return noteId;
    }
    /**
     * @param noteId the noteId to set
     */
    public void setNoteId(final Long noteId) {
        this.noteId = noteId;
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
}
