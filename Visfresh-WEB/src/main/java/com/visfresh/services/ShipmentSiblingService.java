/**
 *
 */
package com.visfresh.services;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentSiblingService {
    /**
     * @param shipment shipment.
     * @return list of siblings for given shipment.
     */
    List<Shipment> getSiblings(Shipment shipment);
    /**
     * @param s shipment.
     * @return number of proposed siblings.
     */
    int getSiblingCount(Shipment s);
    /**
     * @param s master shipment.
     * @param siblings sibling shipments.
     * @return map of shipment ID / sibling
     */
    Map<Long, Color> getSiblingColors(Shipment s, List<Shipment> siblings);
}
