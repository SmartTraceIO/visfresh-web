/**
 *
 */
package com.visfresh.dao;

import java.util.Date;
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
     * @param fromDate
     * @param toDate
     * @return
     */
    List<TrackerEvent> getEvents(Shipment shipment, Date fromDate, Date toDate);
    /**
     * @param event event.
     * @return previous event for given.
     */
    TrackerEvent getPreviousEvent(TrackerEvent event);
}
