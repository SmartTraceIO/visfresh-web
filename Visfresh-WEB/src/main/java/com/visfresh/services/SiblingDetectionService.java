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
public interface SiblingDetectionService {
    /**
     * @param s shipment for detect siblings.
     * @param scheduleDate TODO
     */
    void scheduleSiblingDetection(Shipment s, Date scheduleDate);
}
