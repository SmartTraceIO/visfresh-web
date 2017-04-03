/**
 *
 */
package com.visfresh.rules;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsRuleTest {
    private Shipment shipment;
    /**
     * Default constructor.
     */
    public ShipmentStatisticsRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        final Device d = new Device();
        d.setImei("2034870239457");
        d.setName("JUnit");

        shipment = new Shipment();
        shipment.setId(7l);
        shipment.setDevice(d);

        final AlertProfile ap = new AlertProfile();

    }

    @Test
    public void testNotAcceptWithoutShipment() {

    }
    @Test
    public void testNotAcceptWithoutAlertProfile() {

    }
    @Test
    public void testNotAcceptAfterAlertsSuppressed() {

    }
    @Test
    public void testNotAcceptAfterArrivalDate() {

    }
    @Test
    public void testNotAcceptInInactiveStatus() {

    }
    @Test
    public void testAccept() {

    }
    @Test
    public void testNotDoublehandled() {

    }
    @Test
    public void testHandleWithNotStatsExists() {

    }
    @Test
    public void testHandleWithStatsExists() {

    }
}
