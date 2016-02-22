/**
 *
 */
package com.visfresh.lists;

import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ListShipmentTemplateItem implements EntityWithId<Long> {
    private long shipmentTemplateId;
    private String shipmentTemplateName;

    private String shipmentDescription;

    private Long shippedFrom;
    private String shippedFromLocationName;

    private Long shippedTo;
    private String shippedToLocationName;

    private Long alertProfile;
    private String alertProfileName;

    /**
     * Default constructor.
     */
    public ListShipmentTemplateItem() {
        super();
    }
    /**
     * @param tpl shipment template.
     */
    public ListShipmentTemplateItem(final ShipmentTemplate tpl) {
        super();
        if (tpl.getAlertProfile() != null) {
            setAlertProfile(tpl.getAlertProfile().getId());
            setAlertProfileName(tpl.getAlertProfile().getName());
        }
        setShipmentDescription(tpl.getShipmentDescription());
        setShipmentTemplateId(tpl.getId());
        setShipmentTemplateName(tpl.getName());
        if (tpl.getShippedFrom() != null) {
            setShippedFrom(tpl.getShippedFrom().getId());
            setShippedFromLocationName(tpl.getShippedFrom().getName());
        }
        if (tpl.getShippedTo() != null) {
            setShippedTo(tpl.getShippedTo().getId());
            setShippedToLocationName(tpl.getShippedTo().getName());
        }
    }

    /**
     * @return the shipmentTemplateId
     */
    public long getShipmentTemplateId() {
        return shipmentTemplateId;
    }
    /**
     * @param shipmentTemplateId the shipmentTemplateId to set
     */
    public void setShipmentTemplateId(final long shipmentTemplateId) {
        this.shipmentTemplateId = shipmentTemplateId;
    }
    /**
     * @return the shipmentTemplateName
     */
    public String getShipmentTemplateName() {
        return shipmentTemplateName;
    }
    /**
     * @param shipmentTemplateName the shipmentTemplateName to set
     */
    public void setShipmentTemplateName(final String shipmentTemplateName) {
        this.shipmentTemplateName = shipmentTemplateName;
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
     * @return the shippedFrom
     */
    public Long getShippedFrom() {
        return shippedFrom;
    }
    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final Long shippedFrom) {
        this.shippedFrom = shippedFrom;
    }
    /**
     * @return the shippedFromLocationName
     */
    public String getShippedFromLocationName() {
        return shippedFromLocationName;
    }
    /**
     * @param shippedFromLocationName the shippedFromLocationName to set
     */
    public void setShippedFromLocationName(final String shippedFromLocationName) {
        this.shippedFromLocationName = shippedFromLocationName;
    }
    /**
     * @return the shippedTo
     */
    public Long getShippedTo() {
        return shippedTo;
    }
    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final Long shippedTo) {
        this.shippedTo = shippedTo;
    }
    /**
     * @return the shippedToLocationName
     */
    public String getShippedToLocationName() {
        return shippedToLocationName;
    }
    /**
     * @param shippedToLocationName the shippedToLocationName to set
     */
    public void setShippedToLocationName(final String shippedToLocationName) {
        this.shippedToLocationName = shippedToLocationName;
    }
    /**
     * @return the alertProfile
     */
    public Long getAlertProfile() {
        return alertProfile;
    }
    /**
     * @param alertProfile the alertProfile to set
     */
    public void setAlertProfile(final Long alertProfile) {
        this.alertProfile = alertProfile;
    }
    /**
     * @return the alertProfileName
     */
    public String getAlertProfileName() {
        return alertProfileName;
    }
    /**
     * @param alertProfileName the alertProfileName to set
     */
    public void setAlertProfileName(final String alertProfileName) {
        this.alertProfileName = alertProfileName;
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return getShipmentTemplateId();
    }
}
