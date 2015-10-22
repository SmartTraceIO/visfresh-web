/**
 *
 */
package com.visfresh.services;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface OpenJtsFacade {
    void addUser(User user, String password);
    void addTrackerEvent(Shipment shipment, TrackerEvent e);
}
