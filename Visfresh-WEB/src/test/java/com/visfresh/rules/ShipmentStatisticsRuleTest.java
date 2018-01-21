/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentStatistics;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsRuleTest extends ShipmentStatisticsRule {
    private Shipment shipment;
    private final Map<Long, ShipmentStatistics> stats = new HashMap<>();
    private Device device;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsRuleTest() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.ShipmentStatisticsRule#getStatistics(com.visfresh.entities.Shipment)
     */
    @Override
    protected ShipmentStatistics getStatistics(final Shipment s) {
        return stats.get(s.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.ShipmentStatisticsRule#saveStatistics(com.visfresh.rules.state.ShipmentStatistics)
     */
    @Override
    protected void saveStatistics(final ShipmentStatistics stats) {
        this.stats.put(stats.getShipmentId(), stats);
    }

    @Before
    public void setUp() {
        final Device d = new Device();
        d.setImei("2034870239457");
        d.setName("JUnit");
        this.device = d;

        shipment = new Shipment();
        shipment.setId(7l);
        shipment.setDevice(d);
        shipment.setStatus(ShipmentStatus.Default);
    }
    /**
     * @param events event list.
     * @param shipment shipment.
     * @param latitude latitude.
     * @param longitude longitude.
     * @param time event time.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Shipment s) {
        final TrackerEvent e = new TrackerEvent();
        e.setId(7l);
        e.setLatitude(10.);
        e.setLongitude(10.);
        e.setTime(new Date());
        e.setCreatedOn(e.getTime());
        e.setShipment(s);
        if (s != null) {
            e.setDevice(s.getDevice());
        } else {
            e.setDevice(device);
        }

        e.setTemperature(11.);
        return e;
    }

    @Test
    public void testNotAcceptWithoutShipment() {
        assertFalse(accept(new RuleContext(createEvent(null), new SessionHolder())));
    }
    @Test
    public void testAcceptWithoutAlertProfile() {
        assertTrue(accept(new RuleContext(createEvent(shipment), new SessionHolder())));
    }
    @Test
    public void testNotAcceptAfterAlertsSuppressed() {
        final AlertProfile ap = new AlertProfile();
        ap.setId(77l);

        final long dt = 100000000l;
        final long startTime = System.currentTimeMillis() - dt;
        shipment.setAlertProfile(ap);
        shipment.setShipmentDate(new Date(startTime));
        final TrackerEvent e = createEvent(shipment);
        assertTrue(accept(new RuleContext(e, new SessionHolder())));

        //set alert suppression
        shipment.setAlertSuppressionMinutes(10);
        e.setTime(new Date(shipment.getShipmentDate().getTime() + 9 * 60 * 1000l));
        assertFalse(accept(new RuleContext(e, new SessionHolder())));
    }
    @Test
    public void testNotAcceptAfterArrivalDate() {
        shipment.setAlertProfile(new AlertProfile());
        assertTrue(accept(new RuleContext(createEvent(shipment), new SessionHolder())));

        shipment.setArrivalDate(new Date(System.currentTimeMillis() - 10000l));
        assertFalse(accept(new RuleContext(createEvent(shipment), new SessionHolder())));
    }
    @Test
    public void testNotAcceptInInactiveStatus() {
        shipment.setAlertProfile(new AlertProfile());
        assertTrue(accept(new RuleContext(createEvent(shipment), new SessionHolder())));

        shipment.setStatus(ShipmentStatus.Arrived);
        assertFalse(accept(new RuleContext(createEvent(shipment), new SessionHolder())));

        shipment.setStatus(ShipmentStatus.Ended);
        assertFalse(accept(new RuleContext(createEvent(shipment), new SessionHolder())));
    }
    @Test
    public void testAccept() {
        shipment.setAlertProfile(new AlertProfile());
        assertTrue(accept(new RuleContext(createEvent(shipment), new SessionHolder())));
    }
    @Test
    public void testNotDoublehandled() {
        shipment.setAlertProfile(new AlertProfile());
        final RuleContext context = new RuleContext(createEvent(shipment), new SessionHolder());
        assertTrue(accept(context));

        stats.put(shipment.getId(), new ShipmentStatistics(shipment.getId()));
        handle(context);
        assertFalse(accept(context));
    }
    @Test
    public void testHandle() {
        shipment.setAlertProfile(new AlertProfile());
        stats.put(shipment.getId(), new ShipmentStatistics(shipment.getId()));
        final RuleContext context = new RuleContext(createEvent(shipment), new SessionHolder());

        assertFalse(handle(context));
    }
    @Test
    public void testHandleWithoutAlertProfile() {
        shipment.setAlertProfile(null);
        stats.put(shipment.getId(), new ShipmentStatistics(shipment.getId()));
        final RuleContext context = new RuleContext(createEvent(shipment), new SessionHolder());

        assertFalse(handle(context));
    }
    @Test
    public void testHandleWithoutAlertProfileWithoutPresetStats() {
        shipment.setAlertProfile(null);
        stats.put(shipment.getId(), new ShipmentStatistics(shipment.getId()));
        final RuleContext context = new RuleContext(createEvent(shipment), new SessionHolder());

        assertFalse(handle(context));
    }
}
