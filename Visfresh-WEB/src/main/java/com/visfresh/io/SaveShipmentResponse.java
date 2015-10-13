/**
 *
 */
package com.visfresh.io;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SaveShipmentResponse {
    /**
     * Shipment ID.
     */
    private Long shipmentId;
    /**
     * Shipment Template ID.
     */
    private Long templateId;

    /**
     * Default constructor.
     */
    public SaveShipmentResponse() {
        super();
    }

    /**
     * @param id Shipment ID.
     */
    public void setShipmentId(final Long id) {
        this.shipmentId = id;
    }
    /**
     * @return the shipment ID
     */
    public Long getShipmentId() {
        return shipmentId;
    }

    /**
     * @param id template ID.
     */
    public void setTemplateId(final Long id) {
        this.templateId = id;
    }
    /**
     * @return the templateId
     */
    public Long getTemplateId() {
        return templateId;
    }
}
