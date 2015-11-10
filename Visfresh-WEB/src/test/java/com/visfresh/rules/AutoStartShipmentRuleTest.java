/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private Device device;
    /**
     * Default constructor.
     */
    public AutoStartShipmentRuleTest() {
        super();
    }
    @Before
    public void setUp() {
        this.rule = engine.getRule(AutoStartShipmentRule.NAME);
        this.device = createDevice("90324870987");
    }

    /**
     * @param lat latitude.
     * @param lon longitude.
     * @param date date.
     * @return event.
     */
    private TrackerEvent createEvent(final double lat, final double lon, final Date date) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(100);
        e.setLatitude(lat);
        e.setLongitude(lon);
        e.setTemperature(20.4);
        e.setType("INIT");
        e.setDevice(device);
        e.setTime(date);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    @Test
    public void testAutostartOnFirstEvent() {
        final TrackerEvent e = createEvent(13.14, 15.16, new Date());
        assertTrue(rule.accept(new RuleContext(e, new DeviceState())));
    }
    @Test
    public void testAutostartAfterTimeOut() {
        createEvent(13.14, 15.16, new Date(System.currentTimeMillis() - 10000000L));
        final TrackerEvent e = createEvent(13.14, 15.16, new Date());
        assertTrue(rule.accept(new RuleContext(e, new DeviceState())));
    }
    @Test
    public void testAutostartWhenLocationChanged() {
        createEvent(13.14, 15.16, new Date());
        final TrackerEvent e = createEvent(17.14, 18.16, new Date());
        assertTrue(rule.accept(new RuleContext(e, new DeviceState())));
    }
    @Test
    public void testNotAutostart() {
        createEvent(13.14, 15.16, new Date());
        final TrackerEvent e = createEvent(13.14, 15.16, new Date());
        assertFalse(rule.accept(new RuleContext(e, new DeviceState())));
    }
    @Test
    public void testHandle() {
        final TrackerEvent e = createEvent(17.14, 18.16, new Date());

        final RuleContext c = new RuleContext(e, new DeviceState());
        rule.handle(c);

        //check shipment created.
        assertNotNull(e.getShipment());
        assertNotNull(context.getBean(ShipmentDao.class).findOne(e.getShipment().getId()));

        //check not duplicate handle
        assertFalse(rule.accept(c));
    }
}
