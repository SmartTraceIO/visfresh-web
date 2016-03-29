/**
 *
 */
package com.visfresh.rules.state;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentSessionManager {
    /**
     * @param s shipment.
     * @return shipment session if exists, or created new otherwise.
     */
    ShipmentSession getSession(Shipment s);
}
