/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ReportServiceImpl extends AbstractReportService {
    @Autowired
    private ShipmentDao shipomentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private ArrivalDao arrivalDao;
    /**
     * Default constructor.
     */
    public ReportServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.AbstractReportService#getShipment(java.lang.Long)
     */
    @Override
    protected Shipment getShipment(final Long id) {
        return shipomentDao.findOne(id);
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.AbstractReportService#getTrackerEvents(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    protected List<TrackerEvent> getTrackerEvents(final Shipment shipment,
            final Date fromDate, final Date toDate) {
        return trackerEventDao.getEvents(shipment, fromDate, toDate);
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.AbstractReportService#getArrivals(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    protected List<Arrival> getArrivals(final Shipment shipment, final Date fromDate,
            final Date toDate) {
        return arrivalDao.getArrivals(shipment, fromDate, toDate);
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.AbstractReportService#getAlerts(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    protected List<Alert> getAlerts(final Shipment shipment, final Date fromDate,
            final Date toDate) {
        return alertDao.getAlerts(shipment, fromDate, toDate);
    }
}
