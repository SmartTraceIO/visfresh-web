/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.util.Objects;

import com.visfresh.io.shipment.SingleShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class ParallelItem implements Runnable {
    private final SingleShipmentData data;

    /**
     * Default constructor.
     */
    public ParallelItem(final SingleShipmentData data) {
        super();
        Objects.requireNonNull(data, "SingleShipmentData");
        Objects.requireNonNull(data.getBean(), "SingleShipmentBean");
        this.data = data;
    }
    /**
     * @return the data
     */
    protected SingleShipmentData getData() {
        return data;
    }

}
