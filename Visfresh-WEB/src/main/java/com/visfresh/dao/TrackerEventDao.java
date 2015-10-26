/**
 *
 */
package com.visfresh.dao;

import java.util.Date;

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
}
