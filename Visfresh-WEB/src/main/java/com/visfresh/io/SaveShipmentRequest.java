/**
 *
 */
package com.visfresh.io;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SaveShipmentRequest {
    /**
     * Template name.
     */
    private String templateName;
    /**
     * Shipment.
     */
    private ShipmentDto shipment;
    /**
     * Indicates whether or not should save the shipment as new template.
     */
    private boolean saveAsNewTemplate;
    private Boolean includePreviousData;

    /**
     * Default constructor.
     */
    public SaveShipmentRequest() {
        super();
    }

    /**
     * @return template name.
     */
    public String getTemplateName() {
        return templateName;
    }
    /**
     * @param templateName the template name.
     */
    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    /**
     * @return shipment.
     */
    public ShipmentDto getShipment() {
        return shipment;
    }
    /**
     * @param shipment shipment.
     */
    public void setShipment(final ShipmentDto shipment) {
        this.shipment = shipment;
    }

    /**
     * @return true if should save also new template.
     */
    public boolean isSaveAsNewTemplate() {
        return saveAsNewTemplate;
    }
    /**
     * @param saveAsNewTemplate the saveAsNewTemplate to set
     */
    public void setSaveAsNewTemplate(final boolean saveAsNewTemplate) {
        this.saveAsNewTemplate = saveAsNewTemplate;
    }
    /**
     * @return true if should include previous data, false otherwise.
     */
    public Boolean isIncludePreviousData() {
        return includePreviousData;
    }
    /**
     * @param includePreviousData the includePreviousData to set
     */
    public void setIncludePreviousData(final Boolean includePreviousData) {
        this.includePreviousData = includePreviousData;
    }
}
