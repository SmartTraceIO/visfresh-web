/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="shipmenttemplates")
public class ShipmentTemplate extends ShipmentBase {
    /**
     * Add data shipped.
     */
    @Column
    private boolean addDateShipped;
    /**
     * Detect location for shipped from location.
     */
    @Column(name="detectstartlocation")
    private boolean detectLocationForShippedFrom;
    /**
     * Use current time for shipped date.
     */
    @Column(name="usecurrenttime")
    private boolean useCurrentTimeForDateShipped;
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
     * @return the useCurrentTimeForDateShipped
     */
    public boolean isUseCurrentTimeForDateShipped() {
        return useCurrentTimeForDateShipped;
    }
    /**
     * @param useCurrentTimeForDateShipped the useCurrentTimeForDateShipped to set
     */
    public void setUseCurrentTimeForDateShipped(final boolean useCurrentTimeForDateShipped) {
        this.useCurrentTimeForDateShipped = useCurrentTimeForDateShipped;
    }
}
