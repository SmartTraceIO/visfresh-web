/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemplate extends ShipmentBase {
    /**
     * Add data shipped.
     */
    private boolean addDateShipped;
    /**
     * Detect location for shipped from location.
     */
    private boolean detectLocationForShippedFrom;
    /**
     * Is autostart template flag.
     */
    private boolean isAutostart;
    /**
     * Name.
     */
    private String name;

    /**
     * Default constructor.
     */
    public ShipmentTemplate() {
        super();
    }
    /**
     * @param shipment the shipment to copy.
     */
    public ShipmentTemplate(final ShipmentBase shipment) {
        super(shipment);
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
     * @return the useLocationNearestToDevice
     */
    public boolean isDetectLocationForShippedFrom() {
        return detectLocationForShippedFrom;
    }
    /**
     * @param useLocationNearestToDevice the useLocationNearestToDevice to set
     */
    public void setDetectLocationForShippedFrom(final boolean useLocationNearestToDevice) {
        this.detectLocationForShippedFrom = useLocationNearestToDevice;
    }
    /**
     * @return the isAutostart
     */
    public boolean isAutostart() {
        return isAutostart;
    }
    /**
     * @param isAutostart the isAutostart to set
     */
    public void setAutostart(final boolean isAutostart) {
        this.isAutostart = isAutostart;
    }
}
