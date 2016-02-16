/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Company;
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
     * @param siblingGroup sibling group.
     * @return list of siblings in given group
     */
    List<Shipment> getSiblingGroup(Long siblingGroup);
    /**
     * @param siblingGroup sibling group.
     * @return count of siblings on given group.
     */
    int getGroupSize(Long siblingGroup);
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
}
