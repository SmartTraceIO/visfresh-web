/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemplateDto extends ShipmentBaseDto {
    /**
     * Add data shipped.
     */
    private boolean addDateShipped;
    /**
     * Detect location for shipped from location.
     */
    private boolean detectLocationForShippedFrom;
    /**
     * Name.
     */
    private String name;

    /**
     * Default constructor.
     */
    public ShipmentTemplateDto() {
        super();
    }
    /**
     * @param tpl template.
     */
    public ShipmentTemplateDto(final ShipmentTemplate tpl) {
        super(tpl);

        setName(tpl.getName());
        setAddDateShipped(tpl.isAddDateShipped());
        setDetectLocationForShippedFrom(tpl.isDetectLocationForShippedFrom());
    }
    /**
     * @return the addDateShipped
     */
    public boolean isAddDateShipped() {
        return addDateShipped;
    }
    /**
     * @param addDateShipped the addDateShipped to set
     */
    public void setAddDateShipped(final boolean addDateShipped) {
        this.addDateShipped = addDateShipped;
    }
    /**
     * @return the detectLocationForShippedFrom
     */
    public boolean isDetectLocationForShippedFrom() {
        return detectLocationForShippedFrom;
    }
    /**
     * @param detectLocationForShippedFrom the detectLocationForShippedFrom to set
     */
    public void setDetectLocationForShippedFrom(final boolean detectLocationForShippedFrom) {
        this.detectLocationForShippedFrom = detectLocationForShippedFrom;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
}
