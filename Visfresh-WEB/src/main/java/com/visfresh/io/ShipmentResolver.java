/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentResolver {
    Shipment getShipment(Long id);
}
