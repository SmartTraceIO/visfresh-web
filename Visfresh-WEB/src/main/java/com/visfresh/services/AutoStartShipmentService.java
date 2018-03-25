/**
 *
 */
package com.visfresh.services;

import java.util.Date;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AutoStartShipmentService {
    /**
     * @param device device.
     * @param beaconId beacon ID.
     * @param latitude latitude of start location.
     * @param longitude longitude of start location.
     * @param shipmentDate shipment date.
     * @return
     */
    Shipment autoStartNewShipment(Device device, String beaconId,
            Double latitude, Double longitude, Date shipmentDate);
}
