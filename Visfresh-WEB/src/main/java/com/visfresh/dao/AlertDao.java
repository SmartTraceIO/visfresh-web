/**
 *
 */
package com.visfresh.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AlertDao extends DaoBase<Alert, Alert, Long> {
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
     * @return map of alerts for given shipment ID's.
     */
    Map<Long, List<Alert>> getAlertsForShipmentIds(Collection<Long> shipmentIds);
    /**
     * @param device device.
     * @param startDate start date.
     * @param endDate end date.
     * @return the list of alerts for given device and date ranges.
     */
    List<Alert> getAlerts(String device, Date startDate, Date endDate);
    /**
     * @param company company.
     * @param startDate start date.
     * @param endDate end date.
     * @return list of alerts.
     */
    List<Alert> getAlerts(Company company, Date startDate, Date endDate);
}
