/**
 *
 */
package com.visfresh.services;

import java.util.Date;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentShutdownService {
    /**
     * @param shipment shipment.
     * @param date shutdown time.
     */
    void sendShipmentShutdown(Shipment shipment, Date date);
}
