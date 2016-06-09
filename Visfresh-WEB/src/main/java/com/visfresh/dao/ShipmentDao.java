/**
 *
 */
package com.visfresh.dao;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentDao extends EntityWithCompanyDaoBase<Shipment, Long> {
    /**
     * @param imei device IMEI.
     * @return active shipment for given devcie.
     */
    Shipment findLastShipment(String imei);
    /**
     * @param imei device IMEI.
     * @return active shipments for given devcie.
     */
    List<Shipment> findActiveShipments(String imei);
    /**
     * @param company company.
     * @return list of active shipments for given company.
     */
    List<Shipment> findActiveShipments(Company company);
    /**
     * @param s the shipment.
     * @return next shipment for given.
     */
    Shipment findNextShipmentFor(Shipment s);
    /**
     * @param tpl shipment template.
     * @return new shipment from given template.
     */
    Shipment createNewFrom(ShipmentTemplate tpl);
    @Override
    <S extends Shipment> S save(S s);
    /**
     * @param company company
     * @param sn serial number.
     * @param trip trip count.
     * @return shipment
     */
    Shipment findBySnTrip(Company company, String sn, Integer trip);
    /**
     * @param s shipment.
     * @param eta ETA.
     */
    void updateEta(Shipment s, Date eta);
    /**
     * @param s shipment.
     */
    void markAsAutostarted(Shipment s);
    /**
     * @param oldDevice old device.
     * @param newDevice new device.
     */
    void moveToNewDevice(Device oldDevice, Device newDevice);
    /**
     * @param shipment shipment.
     * @param date new last event date.
     * @return shipoment.
     */
    void updateLastEventDate(Shipment shipment, Date date);
    /**
     * @param s shipment.
     */
    void updateSiblingInfo(Shipment s);
}
