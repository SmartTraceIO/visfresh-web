/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShortShipmentInfo {
    /**
     * ID.
     */
    private Long id;
    /**
     * Shipment description.
     */
    private String shipmentDescription;
    /**
     * Is shipment template flag.
     */
    private boolean isTemplate;

    /**
     * Default constructor.
     */
    public ShortShipmentInfo() {
        super();
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the shipmentDescription
     */
    public String getShipmentDescription() {
        return shipmentDescription;
    }
    /**
     * @param shipmentDescription the shipmentDescription to set
     */
    public void setShipmentDescription(final String shipmentDescription) {
        this.shipmentDescription = shipmentDescription;
    }
    /**
     * @return the isTemplate
     */
    public boolean isTemplate() {
        return isTemplate;
    }
    /**
     * @param isTemplate the isTemplate to set
     */
    public void setTemplate(final boolean isTemplate) {
        this.isTemplate = isTemplate;
    }
}
