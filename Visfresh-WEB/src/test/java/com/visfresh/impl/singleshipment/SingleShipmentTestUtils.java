/**
 *
 */
package com.visfresh.impl.singleshipment;

import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class SingleShipmentTestUtils {
    /**
     * Default constructor.
     */
    private SingleShipmentTestUtils() {
        super();
    }

    /**
     * @param id
     * @param data
     * @return
     */
    public static SingleShipmentBean getSibling(final long id, final SingleShipmentData data) {
        for (final SingleShipmentBean bean : data.getSiblings()) {
            if (id == bean.getShipmentId()) {
                return bean;
            }
        }
        return null;
    }
}
