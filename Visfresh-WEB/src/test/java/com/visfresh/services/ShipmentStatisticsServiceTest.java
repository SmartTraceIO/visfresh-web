/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.impl.services.ShipmentStatisticsServiceImpl;
import com.visfresh.rules.state.ShipmentStatistics;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsServiceTest extends ShipmentStatisticsServiceImpl {
    private final List<TrackerEvent> events = new LinkedList<>();
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsServiceTest() {
        super();
    }
    /**
     * @param events event list.
     * @param shipment shipment.
     * @param latitude latitude.
     * @param longitude longitude.
     * @param time event time.
     * @return tracker event.
     */
    private TrackerEvent addEvent(final double t, final long time) {
        final TrackerEvent e = new TrackerEvent();
        e.setId(1000l + events.size());
        e.setLatitude(10.);
        e.setLongitude(10.);
        e.setTime(new Date(time));
        e.setCreatedOn(e.getTime());
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        e.setTemperature(t);
        events.add(e);
        return e;
    }

    @Before
    public void setUp() {
        final Company company = new Company(77l);
        company.setName("JUnit");

        //create shipment
        final Device d = new Device();
        d.setCompany(company.getCompanyId());
        d.setImei("23948579032574");
        d.setName("JUnit-" + d.getImei());

        final AlertProfile ap = new AlertProfile();
        ap.setId(77l);

        final Shipment s = new Shipment();
        s.setAlertProfile(ap);
        s.setId(77l);
        s.setCompany(company.getCompanyId());
        s.setShipmentDescription("Test_" + s.getId());
        s.setDevice(d);
        s.setStatus(ShipmentStatus.InProgress);

        this.shipment = s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.ShipmentStatisticsServiceImpl#getTrackerEvents(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<TrackerEvent> getTrackerEvents(final Shipment s) {
        return events;
    }

    @Test
    public void testCalculate() {
        final long time = System.currentTimeMillis() - 100000000l;

        addEvent(1., time + 1000l);
        addEvent(2., time + 2000l);
        addEvent(3., time + 3000l);
        addEvent(4., time + 4000l);
        addEvent(5., time + 5000l);

        final ShipmentStatistics stats = calculate(shipment);
        assertNotNull(stats);
        assertEquals(3., stats.getAvgTemperature(), 0.01);
    }
    @Test
    public void testCalculateWithoutAlertProfile() {
        shipment.setAlertProfile(null);
        final long time = System.currentTimeMillis() - 100000000l;

        addEvent(1., time + 1000l);
        addEvent(2., time + 2000l);
        addEvent(3., time + 3000l);
        addEvent(4., time + 4000l);
        addEvent(5., time + 5000l);

        final ShipmentStatistics stats = calculate(shipment);
        assertNotNull(stats);
        assertEquals(3., stats.getAvgTemperature(), 0.01);
    }
}
