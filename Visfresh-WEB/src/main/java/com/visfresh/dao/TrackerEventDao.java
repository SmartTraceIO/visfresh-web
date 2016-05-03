/**
 *
 */
package com.visfresh.dao;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShortTrackerEvent;
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
    /**
     * @param devices list of devices.
     * @return list of tracker events.
     */
    List<ShortTrackerEvent> getLastEvents(List<Device> devices);
    /**
     * @param device device.
     * @return short tracker event.
     */
    ShortTrackerEvent getLastEvent(Device device);
    /**
     * @param device device IMEI.
     * @param startDate start date.
     * @param endDate end date.
     * @return list of tracker events for given device and time ranges.
     */
    List<ShortTrackerEvent> findBy(String device, Date startDate, Date endDate);
    /**
     * @param oldDevice old device.
     * @param newDevice new device.
     */
    void moveToNewDevice(Device oldDevice, Device newDevice);
    /**
     * @param s shipment.
     * @param date start date.
     * @return list of tracker events for given shipment after given date.
     */
    List<TrackerEvent> getEventsAfterDate(Shipment s, Date date);
}
