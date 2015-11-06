/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
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

        dto.getAlertSummary().putAll(toSummaryMap(alerts));
        return dto;
    }

    /**
     * @param alerts
     * @return
     */
    public static  Map<AlertType, Integer> toSummaryMap(
            final List<Alert> alerts) {
        final Map<AlertType, Integer> map = new HashMap<AlertType, Integer>();
        for (final Alert alert : alerts) {
            Integer numAlerts = map.get(alert.getType());
            if (numAlerts == null) {
                numAlerts = 0;
            }
            numAlerts = numAlerts + 1;
            map.put(alert.getType(), numAlerts);
        }
        return map;
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
        return new SingleShipmentDto(shipment);
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
