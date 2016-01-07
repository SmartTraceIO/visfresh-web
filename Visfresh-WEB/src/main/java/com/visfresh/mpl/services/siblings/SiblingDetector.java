/**
 *
 */
package com.visfresh.mpl.services.siblings;

import java.util.List;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SiblingDetector {
    List<Shipment> getSiblings(final Shipment shipment);
    int getSiblingCount(final Shipment s);
}
