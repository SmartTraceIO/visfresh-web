/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceModel;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SiblingDetectionRuleTest extends SiblingDetectionRule {
    private final List<Shipment> shipments = new LinkedList<>();

    /**
     * Default constructor.
     */
    public SiblingDetectionRuleTest() {
        super();
    }

    private Company company;
    private boolean isSiblingsCleared;
    private boolean isSiblingDetectionScheduled;

    @Before
    public void setUp() {
        company = new Company();
        company.setId(1l);
        company.setName("JUnit Company");
    }

    @Test
    public void testNotAcceptIfNotShipment() {
        //crete master event list
        final long t0 = System.currentTimeMillis() - 1000000l;
        //intersected time
        final TrackerEvent e = createTrackerEvent(10., 10., t0 );
        assertFalse(accept(new RuleContext(e, null)));
    }
    @Test
    public void testNotAcceptIfNotActiveShipment() {
        final Shipment s = createShipment(7l);
        s.setStatus(ShipmentStatus.Arrived);

        //crete master event list
        final long t0 = System.currentTimeMillis() - 1000000l;
        //intersected time
        final TrackerEvent e = createTrackerEvent(10., 10., t0 );
        e.setShipment(s);

        assertFalse(accept(new RuleContext(e, null)));
    }
    @Test
    public void testNotAcceptIfAlreadyHandled() {
        final Shipment s = createShipment(7l);
        s.setStatus(ShipmentStatus.Default);

        //crete master event list
        final long t0 = System.currentTimeMillis() - 1000000l;
        //intersected time
        final TrackerEvent e = createTrackerEvent(10., 10., t0 );
        e.setShipment(s);

        final RuleContext context = new RuleContext(e, null);
        assertTrue(accept(context));
        handle(context);
        assertTrue(isSiblingDetectionScheduled);

        assertFalse(accept(context));
    }
    @Test
    public void testBeacon() {
        final Shipment s = createShipment(7l);
        s.setStatus(ShipmentStatus.Default);
        s.getDevice().setModel(DeviceModel.BT04);

        //crete master event list
        final long t0 = System.currentTimeMillis() - 1000000l;
        //intersected time
        final TrackerEvent e = createTrackerEvent(10., 10., t0 );
        e.setShipment(s);

        final RuleContext context = new RuleContext(e, null);
        assertTrue(accept(context));

        handle(context);
        assertFalse(isSiblingDetectionScheduled);
        assertTrue(isSiblingsCleared);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.SiblingDetectionRule#scheduleSiblingDetection(com.visfresh.entities.Shipment, java.util.Date)
     */
    @Override
    protected void scheduleSiblingDetection(final Shipment s, final Date scheduleDate) {
        isSiblingDetectionScheduled = true;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.SiblingDetectionRule#clearSiblings(com.visfresh.entities.Shipment)
     */
    @Override
    protected void clearSiblings(final Shipment s) {
        this.isSiblingsCleared = true;
        s.getSiblings().clear();
        s.setSiblingCount(0);
    }
    /**
     * @param id shipment ID.
     * @return
     */
    protected Shipment createShipment(final long id) {
        final Shipment s = new Shipment();
        s.setId(id);
        s.setCompany(company.getCompanyId());
        s.setShipmentDescription("Test_" + id);
        s.setDevice(createDevice(id));
        s.setStatus(ShipmentStatus.InProgress);

        shipments.add(s);
        return s;
    }
    /**
     * @param id
     * @return
     */
    private Device createDevice(final long id) {
        final String imei = Long.toString(1000000000l + id);
        final Device d = new Device();
        d.setCompany(company.getCompanyId());
        d.setImei(imei);
        d.setName("JUnit-" + id);
        return d;
    }
    /**
     * @param latitude
     * @param longitude
     * @param time
     * @return
     */
    private TrackerEvent createTrackerEvent(final double latitude, final double longitude, final long time) {
        final TrackerEvent e = new TrackerEvent();
        e.setLatitude(latitude);
        e.setLongitude(longitude);
        e.setTime(new Date(time));
        e.setCreatedOn(e.getTime());
        return e;
    }
}
