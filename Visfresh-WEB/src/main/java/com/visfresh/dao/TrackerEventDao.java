/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface TrackerEventDao extends DaoBase<TrackerEvent, Long> {
    /**
     * @param shipment
     * @return
     */
    List<TrackerEvent> getEvents(Shipment shipment);
    /**
     * @param event event.
     * @return previous event for given.
     */
    TrackerEvent getPreviousEvent(TrackerEvent event);
    /**
     * @param s shipment.
     * @return last event for given shipment.
     */
    TrackerEvent getLastEvent(Shipment s);
    /**
     * @param s shipment.
     * @return first event for given shipment.
     */
    TrackerEvent getFirstEvent(Shipment s);
}
