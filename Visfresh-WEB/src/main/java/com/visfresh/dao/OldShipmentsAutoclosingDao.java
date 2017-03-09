/**
 *
 */
package com.visfresh.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * @return number of closed shipments.
     */
    int closeShipments(Collection<Long> ids);
    /**
     * @param date date.
     * @return list of device IMEI which devices have not readings after given time.
     */
    List<String> findDevicesWithoutReadingsAfter(Date date);
    /**
     * @param devices devices.
     * @return map of device shipments.
     */
    Map<String, List<Long>> findActiveShipmentsFor(List<String> devices);
}
