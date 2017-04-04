/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentStatistics;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsInitRuleTest extends ShipmentStatisticsInitRule {
    private Shipment shipment;
    private final Map<Long, ShipmentStatistics> stats = new HashMap<>();
    private Device device;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsInitRuleTest() {
        super();
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
    /* (non-Javadoc)
     * @see com.visfresh.rules.ShipmentStatisticsInitRule#calculateStatistics(com.visfresh.entities.Shipment)
     */
    @Override
    protected ShipmentStatistics calculateStatistics(final Shipment s) {
        return new ShipmentStatistics(s.getId());
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

    @Test
    public void testNotAcceptWithoutShipment() {
        assertFalse(accept(new RuleContext(createEvent(null), new SessionHolder())));
        assertTrue(accept(new RuleContext(createEvent(shipment), new SessionHolder())));
    }
    @Test
    public void testNotAcceptWithExistsStatistics() {
        assertTrue(accept(new RuleContext(createEvent(shipment), new SessionHolder())));

        stats.put(shipment.getId(), new ShipmentStatistics(shipment.getId()));
        assertFalse(accept(new RuleContext(createEvent(shipment), new SessionHolder())));
    }
    @Test
    public void testNotDoublehandled() {
        final RuleContext context = new RuleContext(createEvent(shipment), new SessionHolder());
        assertTrue(accept(context));

        handle(context);
        assertFalse(accept(context));
    }
    @Test
    public void testHandle() {
        final RuleContext context = new RuleContext(createEvent(shipment), new SessionHolder());
        assertFalse(handle(context));

        assertNotNull(stats.get(shipment.getId()));
    }
}
