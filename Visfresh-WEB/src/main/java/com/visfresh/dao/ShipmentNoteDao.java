/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentNote;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentNoteDao extends DaoBase<ShipmentNote, Long> {
    /**
     * @param shipment shipment.
     * @param user note owner.
     * @return list of notes for given shipment and user.
     */
    List<ShipmentNote> findByUserAndShipment(Shipment shipment, User user);
}
