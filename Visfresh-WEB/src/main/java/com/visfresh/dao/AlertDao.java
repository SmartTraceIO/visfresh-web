/**
 *
 */
package com.visfresh.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AlertDao extends DaoBase<Alert, Long> {
    /**
     * @param shipment
     * @return
     */
    List<Alert> getAlerts(Shipment shipment);
    /**
     * @param oldDevice old device.
     * @param newDevice new device.
     */
    void moveToNewDevice(Device oldDevice, Device newDevice);
    /**
     * @param shipmentIds
     * @return map of alerts for givent shipment ID's.
     */
    Map<Long, List<Alert>> getAlertsForShipmentIds(Collection<Long> shipmentIds);
}
