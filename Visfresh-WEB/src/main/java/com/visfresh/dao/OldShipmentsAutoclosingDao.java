/**
 *
 */
package com.visfresh.dao;

import java.util.Collection;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface OldShipmentsAutoclosingDao {

    /**
     * @param limit select limit.
     * @return list of shipment ID.
     */
    List<Long> findNotClosedShipmentsWithInactiveDevices(int limit);

    /**
     * @param ids collection of shipment ID.
     * @return TODO
     */
    int closeShipments(Collection<Long> ids);
}
