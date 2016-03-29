/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractAlertRuleTest extends AbstractAlertRule {

    private Shipment shipment;
    private TrackerEvent event;

    /**
     * Default constructor.
     */
    public AbstractAlertRuleTest() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractAlertRule#handleInternal(com.visfresh.rules.RuleContext)
     */
    @Override
    protected Alert[] handleInternal(final RuleContext context) {
        return new Alert[0];
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractNotificationRule#getName()
     */
    @Override
    public String getName() {
        return "UnitTestForAbstractAlertRule";
    }

    @Before
    public void setUp() {
        this.shipment = new Shipment();
        shipment.setId(7l);
        shipment.setShipmentDate(new Date());

        this.event = new TrackerEvent();
        event.setTime(new Date());
        event.setShipment(shipment);
        event.setType(TrackerEventType.AUT);
    }

    @Test
    public void testAlertProfile() {
        final AlertProfile ap = new AlertProfile();
        shipment.setAlertProfile(ap);

        assertTrue(accept(new RuleContext(event, new ShipmentSession())));

        shipment.setAlertProfile(null);
        assertFalse(accept(new RuleContext(event, new ShipmentSession())));
    }
    @Test
    public void testNoAlertsAfterArrivalMinutes() {
        final AlertProfile ap = new AlertProfile();
        shipment.setAlertProfile(ap);

        final Integer minutes = 10;
        shipment.setNoAlertsAfterArrivalMinutes(minutes);

        shipment.setArrivalDate(new Date(event.getTime().getTime() - (minutes + 1) * 60 * 1000l));
        assertTrue(accept(new RuleContext(event, new ShipmentSession())));

        shipment.setStatus(ShipmentStatus.Arrived);
        assertFalse(accept(new RuleContext(event, new ShipmentSession())));
    }
    @Test
    public void testNoAlertsAfterStartMinutes() {
        final Integer minutes = 10;
        shipment.setAlertProfile(new AlertProfile());
        shipment.setNoAlertsAfterStartMinutes(minutes);

        shipment.setShipmentDate(new Date(event.getTime().getTime() - minutes * 60 * 1000L - 1l));
        assertFalse(accept(new RuleContext(event, new ShipmentSession())));

        shipment.setShipmentDate(new Date(event.getTime().getTime() - minutes * 60 * 1000L + 1l));
        assertTrue(accept(new RuleContext(event, new ShipmentSession())));
    }
    @Test
    public void testSuppressAlertsAfterStartMinutes() {
        final int minutes = 10;
        shipment.setAlertProfile(new AlertProfile());
        shipment.setAlertSuppressionMinutes(minutes);

        shipment.setShipmentDate(new Date(event.getTime().getTime() - minutes * 60 * 1000L - 1l));
        assertTrue(accept(new RuleContext(event, new ShipmentSession())));

        shipment.setShipmentDate(new Date(event.getTime().getTime() - minutes * 60 * 1000L + 1l));
        assertFalse(accept(new RuleContext(event, new ShipmentSession())));
    }
}
