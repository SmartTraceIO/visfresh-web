/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.io.SingleShipmentDto;
import com.visfresh.io.SingleShipmentTimeItem;
import com.visfresh.services.ReportService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractReportService implements ReportService {
    /**
     * Default constructor.
     */
    public AbstractReportService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ReportService#getSingleShipment(java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public SingleShipmentDto getSingleShipment(final Date fromDate, final Date toDate,
            final Long shipmentId) {
        final Shipment shipment = getShipment(shipmentId);
        if (shipment == null) {
            return null;
        }

        final SingleShipmentDto dto = creatSingleShipmentDto(shipment);

        final List<TrackerEvent> events = getTrackerEvents(shipment, fromDate, toDate);
        for (final TrackerEvent e : events) {
            final SingleShipmentTimeItem item = new SingleShipmentTimeItem();
            item.setEvent(e);
            dto.getItems().add(item);
        }
        Collections.sort(dto.getItems());

        if (events.size() == 0) {
            return dto;
        }

        //add alerts
        final List<Alert> alerts = getAlerts(shipment, fromDate, toDate);
        for (final Alert alert : alerts) {
            final SingleShipmentTimeItem item = getBestCandidate(dto.getItems(), alert.getDate());
            item.getAlerts().add(alert);
        }

        //add arrivals
        final List<Arrival> arrivals = getArrivals(shipment, fromDate, toDate);
        for (final Arrival arrival : arrivals) {
            final SingleShipmentTimeItem item = getBestCandidate(dto.getItems(), arrival.getDate());
            item.getArrivals().add(arrival);
        }

        return dto;
    }

    /**
     * @param shipmentId
     * @return
     */
    protected abstract Shipment getShipment(Long shipmentId);
    /**
     * @param items
     * @param date
     * @return
     */
    private SingleShipmentTimeItem getBestCandidate(final List<SingleShipmentTimeItem> items, final Date date) {
        for (final SingleShipmentTimeItem i : items) {
            if (i.getEvent().getTime().equals(date) || i.getEvent().getTime().after(date)) {
                return i;
            }
        }
        return items.get(items.size() - 1);
    }

    /**
     * @param shipment
     * @return
     */
    private SingleShipmentDto creatSingleShipmentDto(final Shipment shipment) {
        final SingleShipmentDto dto = new SingleShipmentDto();
        dto.setAlertProfile(shipment.getAlertProfile() == null ? null : shipment.getAlertProfile().getId());
        dto.setAlertsNotificationSchedules(getIds(shipment.getAlertsNotificationSchedules()));
        dto.setAlertSuppressionDuringCoolDown(shipment.getAlertSuppressionDuringCoolDown());
        dto.setArrivalNotificationSchedules(getIds(shipment.getArrivalNotificationSchedules()));
        dto.setArrivalNotificationWithIn(shipment.getArrivalNotificationWithIn());
        dto.setAssetNum(shipment.getAssetNum());
        dto.setAssetType(shipment.getAssetType());
        dto.getCustomFields().putAll(shipment.getCustomFields());
        dto.setPalletId(shipment.getPalletId());
        dto.setPoNum(shipment.getPoNum());
        dto.setShipmentDescription(shipment.getShipmentDescription());
        dto.setShippedFrom(shipment.getShippedFrom() == null ? null : shipment.getShippedFrom().getId());
        dto.setShippedTo(shipment.getShippedTo() == null ? null : shipment.getShippedTo().getId());
        dto.setShutdownDevice(shipment.getShutdownDeviceTimeOut());
        dto.setStatus(shipment.getStatus().getLabel());
        dto.setTripCount(shipment.getTripCount());
        return dto;
    }

    /**
     * @param arrivalNotificationSchedules
     * @return
     */
    private <E extends EntityWithId<Long>> long[] getIds(final List<E> entities) {
        final long[] ids = new long[entities.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = entities.get(i).getId();
        }
        return ids;
    }

    /**
     * @param shipment
     * @param fromDate
     * @param toDate
     * @return
     */
    protected abstract List<TrackerEvent> getTrackerEvents(final Shipment shipment,
            final Date fromDate, final Date toDate);

    /**
     * @param shipment
     * @param fromDate
     * @param toDate
     * @return
     */
    protected abstract List<Arrival> getArrivals(final Shipment shipment, final Date fromDate,
            final Date toDate);
    /**
     * @param shipment
     * @param fromDate
     * @param toDate
     * @return
     */
    protected abstract List<Alert> getAlerts(final Shipment shipment, final Date fromDate, final Date toDate);
}
