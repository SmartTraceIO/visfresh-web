/**
 *
 */
package com.visfresh.mock;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentIssue;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.mpl.services.AbstractReportService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockReportService extends AbstractReportService {
    @Autowired
    private MockRestService rest;

    /**
     * Default constructor.
     */
    public MockReportService() {
        super();
    }
    /**
     * @param shipment
     * @param fromDate
     * @param toDate
     * @return
     */
    @Override
    protected List<TrackerEvent> getTrackerEvents(final Shipment shipment,
            final Date fromDate, final Date toDate) {
        final List<TrackerEvent> origin = rest.trackerEvents.get(shipment.getDevice().getId());

        final List<TrackerEvent> alerts = new LinkedList<TrackerEvent>();
        for (final TrackerEvent alert : origin) {
            Shipment s = alert.getShipment();
            Date time = alert.getTime();
            if (s.getId().equals(shipment.getId()) && !time.before(fromDate)
                    && !time.after(toDate)) {
                alerts.add(alert);
            }
        }
        Collections.sort(alerts);
        return alerts;
    }

    /**
     * @param shipment
     * @param fromDate
     * @param toDate
     * @return
     */
    @Override
    protected List<Arrival> getArrivals(final Shipment shipment, final Date fromDate,
            final Date toDate) {
        return getIssues(shipment, fromDate, toDate, rest.arrivals.values());
    }
    /**
     * @param shipment
     * @param fromDate
     * @param toDate
     * @return
     */
    @Override
    protected List<Alert> getAlerts(final Shipment shipment, final Date fromDate, final Date toDate) {
        return getIssues(shipment, fromDate, toDate, rest.alerts.values());
    }
    private <M extends ShipmentIssue> List<M> getIssues(final Shipment shipment,
            final Date fromDate, final Date toDate, final Collection<M> origin) {
        final List<M> alerts = new LinkedList<M>();
        for (final M alert : origin) {
            if (alert.getShipment().getId().equals(shipment.getId()) && !alert.getDate().before(fromDate)
                    && !alert.getDate().after(toDate)) {
                alerts.add(alert);
            }
        }
        Collections.sort(alerts);
        return alerts;
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.AbstractReportService#getShipment(java.lang.Long)
     */
    @Override
    protected Shipment getShipment(final Long shipmentId) {
        return rest.shipments.get(shipmentId);
    }
}
