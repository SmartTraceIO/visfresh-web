/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Shipment;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentSessionDao {
    /**
     * @param shipment shipment.
     * @return shipment session.
     */
    ShipmentSession getSession(Shipment shipment);

    /**
     * @param shipment shipment.
     * @param session shipment session.
     */
    void saveSession(Shipment shipment, ShipmentSession session);
}
