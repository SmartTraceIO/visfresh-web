/**
 *
 */
package com.visfresh.rules;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class StartShipmentTimeCorrectRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public StartShipmentTimeCorrectRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        //create rule
        rule = engine.getRule(StartShipmentTimeCorrectRule.NAME);

        //create shipment.
        final Device device = createDevice("90324870987");
        shipment = createDefaultShipment(ShipmentStatus.InProgress, device);
    }

    @Test
    public void testAccept() {

    }

    @Test
    public void testHandle() {

    }
}
