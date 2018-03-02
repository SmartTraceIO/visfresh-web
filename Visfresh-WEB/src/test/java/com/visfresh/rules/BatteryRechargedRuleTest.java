/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BatteryRechargedRuleTest extends BatteryRechargedRule {
    private final int batteryOk = LOW_RECHARGED_LIMIT + 10;
    private final int batteryLow = LOW_RECHARGED_LIMIT - 1;
    private Device device;
    private long id;
    private Company company;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public BatteryRechargedRuleTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        this.company = new Company();
        company.setId(++id);
        this.device = createDevice("90324870987");
        this.shipment = createShipment(ShipmentStatus.InProgress);
    }
    @Test
    public void testAccept() {
        //test accept
        final SessionHolder mgr = new SessionHolder();
        mgr.getSession(shipment).setBatteryLowProcessed(true);

        final TrackerEvent eventOk = createEvent(batteryOk);
        assertTrue(accept(new RuleContext(eventOk, mgr)));

        //test not accept battery low
        assertFalse(accept(new RuleContext(createEvent(batteryLow), mgr)));

        //test not accept battery low not processed
        mgr.getSession(shipment).setBatteryLowProcessed(false);
        assertFalse(accept(new RuleContext(eventOk, mgr)));

        mgr.getSession(shipment).setBatteryLowProcessed(true);

        //test not process without shipment.
        eventOk.setShipment(null);
        assertFalse(accept(new RuleContext(eventOk, mgr)));

    }
    @Test
    public void testHandle() {
        final TrackerEvent e = createEvent(batteryOk);
        final SessionHolder mgr = new SessionHolder();
        mgr.getSession(shipment).setBatteryLowProcessed(true);

        final RuleContext c = new RuleContext(e, mgr);
        assertFalse(handle(c));
        assertFalse(mgr.getSession(shipment).isBatteryLowProcessed());
    }

    /**
     * @param imei
     * @return
     */
    private Device createDevice(final String imei) {
        final Device d = new Device();
        d.setName("Test Device");
        d.setImei(imei);
        d.setCompany(company.getCompanyId());
        d.setDescription("Test device");
        return d;
    }
    /**
     * @param name shipment name.
     * @param status shipment status
     * @return
     */
    private Shipment createShipment(final ShipmentStatus status) {
        final Shipment s = new Shipment();
        s.setId(++id);
        s.setDevice(device);
        s.setCompany(company.getCompanyId());
        s.setStatus(status);
        return s;
    }
    /**
     * @param lat latitude.
     * @param lon longitude.
     * @param date date.
     * @return event.
     */
    private TrackerEvent createEvent(final int battery) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(battery);
        e.setLatitude(25.);
        e.setLongitude(25.);
        e.setTemperature(20.4);
        e.setType(TrackerEventType.AUT);
        e.setShipment(shipment);
        e.setDevice(device);
        e.setTime(new Date());
        e.setId(++id);
        return e;
    }
}
