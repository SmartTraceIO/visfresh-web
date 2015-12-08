/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentNote implements EntityWithId<Long> {
    /**
     * Note ID.
     */
    private Long id;
    /**
     * Shipment.
     */
    private Shipment shipment;
    /**
     * User.
     */
    private User user;
    /**
     * Note text.
     */
    private String text;

    /**
     * Default constructor.
     */
    public ShipmentNote() {
        super();
    }

    /**
     * @return the shipment
     */
    public Shipment getShipment() {
        return shipment;
    }
    /**
     * @param shipment the shipment to set
     */
    public void setShipment(final Shipment shipment) {
        this.shipment = shipment;
    }
    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }
    /**
     * @param user the user to set
     */
    public void setUser(final User user) {
        this.user = user;
    }
    /**
     * @return note text.
     */
    public String getText() {
        return text;
    }
    /**
     * @param text the text to set
     */
    public void setText(final String text) {
        this.text = text;
    }
    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
}
