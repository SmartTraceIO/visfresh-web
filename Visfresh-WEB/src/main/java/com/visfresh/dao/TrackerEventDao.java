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
     * @param e event.
     * @param minimalTemperature minimal critical temperature.
     * @return first critical temperature issue date.
     */
    Date getFirstHotOccurence(TrackerEvent e, double minimalTemperature);
    /**
     * @param e event.
     * @param maximalTemperature maximal critical temperature.
     * @return first critical temperature issue date.
     */
    Date getFirstColdOccurence(TrackerEvent e, double maximalTemperature);
    /**
     * @param shipment
     * @param fromDate
     * @param toDate
     * @return
     */
    List<TrackerEvent> getEvents(Shipment shipment, Date fromDate, Date toDate);
}
