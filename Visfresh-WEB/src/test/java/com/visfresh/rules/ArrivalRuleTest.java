/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalRuleTest extends BaseRuleTest {
    private ArrivalRule rule;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public ArrivalRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        rule = context.getBean(ArrivalRule.class);
        shipment = createDefaultShipment(ShipmentStatus.InProgress, createDevice("9283470987"));
    }

    @Test
    public void testAccept() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final RuleContext req = new RuleContext(e, new DeviceState());
        //final location not set
        assertFalse(rule.accept(req));

        LocationProfile loc = new LocationProfile();
        loc.setAddress("SPb");
        loc.setCompany(company);
        loc.setName("Finish location");
        loc.getLocation().setLatitude(10);
        loc.getLocation().setLongitude(10);
        loc = context.getBean(LocationProfileDao.class).save(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //so far
        assertFalse(rule.accept(req));

        //set nearest location
        e.setLatitude(10);
        e.setLongitude(10);
        assertTrue(rule.accept(req));
    }
    @Test
    public void testHandle() {
        shipment.setArrivalNotificationWithinKm(1);

        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setShipment(shipment);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        e.setLatitude(10);
        e.setLongitude(10);

        LocationProfile loc = new LocationProfile();
        loc.setAddress("SPb");
        loc.setCompany(company);
        loc.setName("Finish location");
        loc.getLocation().setLatitude(10);
        loc.getLocation().setLongitude(10);
        loc = context.getBean(LocationProfileDao.class).save(loc);

        shipment.setShippedTo(loc);
        context.getBean(ShipmentDao.class).save(shipment);

        //set nearest location
        final RuleContext req = new RuleContext(e, new DeviceState());
        rule.accept(req);
        rule.handle(req);

        //check arrival created
        assertEquals(1, context.getBean(ArrivalDao.class).findAll(null, null, null).size());
    }
}
