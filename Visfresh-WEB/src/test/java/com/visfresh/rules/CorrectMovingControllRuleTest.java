/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Location;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.correctmoving.LastLocationInfo;
import com.visfresh.rules.state.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CorrectMovingControllRuleTest extends CorrectMovingControllRule {
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private List<TrackerEvent> savedEvents = new LinkedList<>();
    private Location startLocation;
    private Location normalLocation;
    private Location farLocation;

    /**
     * Default constructor.
     */
    public CorrectMovingControllRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        startLocation = new Location(1., 1.);
        normalLocation = new Location(1., 2.);
        farLocation = new Location(5., 5.);
    }

    @Test
    public void testAccept() {
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        final RuleContext context = new RuleContext(e, new SessionHolder());

        //not accept without location
        assertFalse(accept(context));

        //accept with location
        e.setLatitude(startLocation.getLatitude());
        e.setLongitude(startLocation.getLongitude());
        assertTrue(accept(context));
    }

    @Test
    public void testNotPrevious() {
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setLatitude(normalLocation.getLatitude());
        e.setLongitude(normalLocation.getLongitude());
        e.setTime(new Date());

        final RuleContext context = new RuleContext(e, new SessionHolder());
        context.setDeviceState(new DeviceState());

        assertFalse(handle(context));

        assertNotNull(getLastLocationInfo(context.getDeviceState(), e.getBeaconId()));
    }
    @Test
    public void testNotClearLocationBigTimeOut() {
        //create event
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setLatitude(farLocation.getLatitude());
        e.setLongitude(farLocation.getLongitude());
        e.setTime(new Date());

        final RuleContext context = new RuleContext(e, new SessionHolder());
        context.setDeviceState(new DeviceState());

        //supply last location info
        LastLocationInfo info = new LastLocationInfo();
        info.setLastLocation(startLocation);
        info.setLastReadTime(new Date(e.getTime().getTime() - TIME_OUT - 10000l));
        setLastLocationInfo(context.getDeviceState(), info, e.getBeaconId());

        //do test
        assertFalse(handle(context));

        //test updated location and time
        info = getLastLocationInfo(context.getDeviceState(), e.getBeaconId());
        assertEquals(e.getLatitude(), info.getLastLocation().getLatitude(), 0.00001);
        assertEquals(e.getLongitude(), info.getLastLocation().getLongitude(), 0.00001);
        assertEquals(format.format(e.getTime()), format.format(info.getLastReadTime()));

        //not accept after handle
        assertFalse(accept(context));
    }
    @Test
    public void testNotClearLocationNotFar() {
        //create event
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setLatitude(normalLocation.getLatitude());
        e.setLongitude(normalLocation.getLongitude());
        e.setTime(new Date(System.currentTimeMillis() - 10000000l));

        final RuleContext context = new RuleContext(e, new SessionHolder());
        context.setDeviceState(new DeviceState());

        //supply last location info
        LastLocationInfo info = new LastLocationInfo();
        info.setLastLocation(startLocation);
        info.setLastReadTime(new Date(e.getTime().getTime() - TIME_OUT + 10000l));
        setLastLocationInfo(context.getDeviceState(), info, e.getBeaconId());

        //do test
        assertFalse(handle(context));

        //test updated location and time
        info = getLastLocationInfo(context.getDeviceState(), e.getBeaconId());
        assertEquals(e.getLatitude(), info.getLastLocation().getLatitude(), 0.00001);
        assertEquals(e.getLongitude(), info.getLastLocation().getLongitude(), 0.00001);
        assertEquals(format.format(e.getTime()), format.format(info.getLastReadTime()));

        //not accept after handle
        assertFalse(accept(context));
    }
    @Test
    public void testClearLocation() {
        //create event
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setLatitude(farLocation.getLatitude());
        e.setLongitude(farLocation.getLongitude());
        e.setTime(new Date(System.currentTimeMillis() - 10000000l));

        final RuleContext context = new RuleContext(e, new SessionHolder());
        context.setDeviceState(new DeviceState());

        //supply last location info
        LastLocationInfo info = new LastLocationInfo();
        info.setLastLocation(startLocation);
        info.setLastReadTime(new Date(e.getTime().getTime() - TIME_OUT + 10000l));
        setLastLocationInfo(context.getDeviceState(), info, e.getBeaconId());

        //do test
        assertTrue(handle(context));
        assertNull(e.getLatitude());
        assertNull(e.getLongitude());

        //test updated location and time
        info = getLastLocationInfo(context.getDeviceState(), e.getBeaconId());
        assertEquals(farLocation.getLatitude(), info.getLastLocation().getLatitude(), 0.00001);
        assertEquals(farLocation.getLongitude(), info.getLastLocation().getLongitude(), 0.00001);
        assertEquals(format.format(e.getTime()), format.format(info.getLastReadTime()));

        //not accept after handle
        assertFalse(accept(context));
    }
    @Test
    public void testNotClearLocationInitEventType() {
        //create event
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.INIT);
        e.setLatitude(farLocation.getLatitude());
        e.setLongitude(farLocation.getLongitude());
        e.setTime(new Date(System.currentTimeMillis() - 10000000l));

        final RuleContext context = new RuleContext(e, new SessionHolder());
        context.setDeviceState(new DeviceState());

        //supply last location info
        LastLocationInfo info = new LastLocationInfo();
        info.setLastLocation(startLocation);
        info.setLastReadTime(new Date(e.getTime().getTime() - TIME_OUT + 10000l));
        setLastLocationInfo(context.getDeviceState(), info, e.getBeaconId());

        //do test
        assertFalse(handle(context));

        //test updated location and time
        info = getLastLocationInfo(context.getDeviceState(), e.getBeaconId());
        assertEquals(e.getLatitude(), info.getLastLocation().getLatitude(), 0.00001);
        assertEquals(e.getLongitude(), info.getLastLocation().getLongitude(), 0.00001);
        assertEquals(format.format(e.getTime()), format.format(info.getLastReadTime()));

        //not accept after handle
        assertFalse(accept(context));
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.CorrectMovingControllRule#saveTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected void saveTrackerEvent(final TrackerEvent e) {
        savedEvents.add(e);
    }
}
