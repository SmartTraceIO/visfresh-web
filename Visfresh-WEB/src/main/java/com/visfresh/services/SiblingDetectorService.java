/**
 *
 */
package com.visfresh.services;

import java.util.List;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SiblingDetectorService {
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
}
