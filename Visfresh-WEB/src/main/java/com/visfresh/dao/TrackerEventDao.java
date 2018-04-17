/**
 *
 */
package com.visfresh.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.io.TrackerEventDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface TrackerEventDao extends DaoBase<TrackerEvent, TrackerEvent, Long> {
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
    /**
     * @param ids shipment IDs.
     * @return map of shipment ID to list of tracker event.
     */
    Map<Long, List<TrackerEventDto>> getEventsForShipmentIds(Collection<Long> ids);
    /**
     * @param shipments
     * @param page
     * @param size
     * @return list of tracker events.
     */
    List<TrackerEventDto> getEventPart(Set<Long> shipments, int page, int size);
    /**
     * @param id tracker event ID.
     * @param s shipment.
     */
    void assignShipment(Long id, Shipment s);
    /**
     * @param c company.
     * @param startDate start date.
     * @param endDate end date.
     * @param endLocation end location of shipment.
     * @param page page.
     * @return tracker events.
     */
    List<TrackerEvent> findForArrivedShipmentsInDateRanges(Company c, Date startDate,
            Date endDate, LocationProfile endLocation, Page page);
    /**
     * @param e tracker event.
     * @param gateway beacon gateway.
     * @return tracker event.
     */
    <S extends TrackerEvent> S save(S e, String gateway);
}
