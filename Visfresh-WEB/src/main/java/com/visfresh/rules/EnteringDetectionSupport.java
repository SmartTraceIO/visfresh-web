/**
 *
 */
package com.visfresh.rules;

import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EnteringDetectionSupport {
    private final int maxNumEntering;
    private final String propertyName;

    /**
     * @param maxNumEntering
     * @param propertyName
     */
    public EnteringDetectionSupport(final String propertyName) {
        this(2, propertyName);
    }
    /**
     * @param maxNumEntering
     * @param propertyName
     */
    public EnteringDetectionSupport(final int maxNumEntering, final String propertyName) {
        super();
        this.maxNumEntering = maxNumEntering;
        this.propertyName = propertyName;
    }

    public boolean handleEntered(final ShipmentSession ss) {
        final String value = ss.getShipmentProperty(propertyName);

        final int num = (value == null ? 0 : Integer.parseInt(value)) + 1;
        ss.setShipmentProperty(propertyName, Integer.toString(num));

        if (num >= maxNumEntering) {
            return true;
        }
        return false;
    }
    public boolean isInControl(final ShipmentSession ss) {
        return ss.getShipmentProperty(propertyName) != null;
    }
    public void clearInControl(final ShipmentSession ss) {
        ss.setShipmentProperty(propertyName, null);
    }
}
