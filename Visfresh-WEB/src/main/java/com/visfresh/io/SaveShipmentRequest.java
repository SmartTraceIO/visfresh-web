/**
 *
 */
package com.visfresh.io;

import java.util.List;

import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;

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
    private Shipment shipment;
    /**
     * Indicates whether or not should save the shipment as new template.
     */
    private boolean saveAsNewTemplate;
    private Boolean includePreviousData;
    private List<LocationProfile> interimLocations;

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
    public Shipment getShipment() {
        return shipment;
    }
    /**
     * @param shipment shipment.
     */
    public void setShipment(final Shipment shipment) {
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
    /**
     * @return the interimLocations
     */
    public List<LocationProfile> getInterimLocations() {
        return interimLocations;
    }
    /**
     * @param interimLocations the interimLocations to set
     */
    public void setInterimLocations(final List<LocationProfile> interimLocations) {
        this.interimLocations = interimLocations;
    }
}
