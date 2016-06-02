/**
 *
 */
package com.visfresh.rules;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EtaCalculationRule implements TrackerEventRule {
    public static final String NAME = "EtaCalculation";
    private static final Logger log = LoggerFactory.getLogger(EtaCalculationRule.class);

    /**
     * 60 km/h as meters/milliseconds
     */
    private static final double V_60_KM_H = 60. * 1000 / (60 * 60 * 1000);

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public EtaCalculationRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Shipment s = e.getShipment();

        return s != null
            && !s.hasFinalStatus()
            && s.getShippedFrom() != null
            && s.getShippedTo() != null
            && e.getTime().after(s.getShipmentDate())
            && e.getLatitude() != null
            && e.getLongitude() != null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Shipment s = e.getShipment();
        final Location loc = new Location(e.getLatitude(), e.getLongitude());

        log.debug("Calculate ETA for shipment: " + s.getId());
        final Date eta = estimateArrivalDate(s, loc, s.getShipmentDate(), e.getTime());
        if (eta != null) {
            s.setEta(eta);
            updateEta(s, eta);
            log.debug("ETA for shipment: " + s.getId() + " has updated to " + eta);
        }

        return false;
    }
    /**
     * @param s shipment.
     * @param eta ETA date.
     */
    protected void updateEta(final Shipment s, final Date eta) {
        shipmentDao.updateEta(s, eta);
    }
    /**
     * @param s shipment.
     * @param currentLocation current shipment location.
     * @param startDate start shipment date.
     * @param currentTime current time.
     * @return ETA
     */
    public Date estimateArrivalDate(final Shipment s,
            final Location currentLocation, final Date startDate, final Date currentTime) {
        final long dt = currentTime.getTime() - startDate.getTime();
        final Location from = s.getShippedFrom().getLocation();
        final Location to = s.getShippedTo().getLocation();

        //calculate speed.
        final double pathDone = LocationUtils.getDistanceMeters(
                currentLocation.getLatitude(), currentLocation.getLongitude(),
                from.getLatitude(), from.getLongitude());

        //v should be not less then 60km/h
        final double v = Math.max(V_60_KM_H, pathDone / dt);

        //calculate reminder distance
        double reminder = LocationUtils.getDistanceMeters(
                currentLocation.getLatitude(), currentLocation.getLongitude(),
                to.getLatitude(), to.getLongitude());
        reminder = Math.max(reminder - s.getShippedTo().getRadius(), 0);
        if (reminder == 0) {
            return new Date(currentTime.getTime());
        }

        return new Date(currentTime.getTime() + (long) (reminder / v));
    }
}
