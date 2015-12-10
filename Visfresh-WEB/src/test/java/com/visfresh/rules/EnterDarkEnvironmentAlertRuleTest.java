/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EnterDarkEnvironmentAlertRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private Device device;

    /**
     * Default constructor.
     */
    public EnterDarkEnvironmentAlertRuleTest() {
        super();
    }
    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        this.rule = engine.getRule(EnterDarkEnvironmentAlertRule.NAME);
        this.device = createDevice("90324870987");
    }
    /**
     * @param lat latitude.
     * @param lon longitude.
     * @param date date.
     * @return event.
     */
    private TrackerEvent createEvent(final TrackerEventType type) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(100);
        e.setLatitude(25);
        e.setLongitude(25);
        e.setTemperature(20.4);
        e.setType(type);
        e.setDevice(device);
        e.setTime(new Date());
        return context.getBean(TrackerEventDao.class).save(e);
    }
    @Test
    public void testAccept() {
        final TrackerEvent e = createEvent(TrackerEventType.DRK);
        final Shipment s = createShipmentWithEnabledAlert(e.getDevice());
        e.setShipment(s);

        //test accept event
        assertTrue(rule.accept(new RuleContext(e, new DeviceState())));

        //ignores without shipment
        e.setShipment(null);
        assertFalse(rule.accept(new RuleContext(e, new DeviceState())));

        //ignore with shipment not configured for given alert
        e.setShipment(createDefaultShipment(ShipmentStatus.InProgress, s.getDevice()));
        assertFalse(rule.accept(new RuleContext(e, new DeviceState())));

        //set alwais accepted even if handled
        e.setShipment(s);
        final RuleContext c = new RuleContext(e, new DeviceState());
        rule.handle(c);
        assertTrue(rule.accept(new RuleContext(e, new DeviceState())));
    }
    @Test
    public void testHandle() {
        final TrackerEvent e = createEvent(TrackerEventType.DRK);
        final Shipment s = createShipmentWithEnabledAlert(e.getDevice());
        e.setShipment(s);
        context.getBean(TrackerEventDao.class).save(e);

        final RuleContext c = new RuleContext(e, new DeviceState());
        rule.handle(c);

        //check shipment created.
        final List<Alert> alerts = context.getBean(AlertDao.class).findAll(null, null, null);
        assertEquals(1, alerts.size());

        final Alert alert = alerts.get(0);
        assertEquals(AlertType.LightOff, alert.getType());
        assertNotNull(alert.getDevice());
        assertNotNull(alert.getShipment());
    }

    /**
     * @param d device.
     * @return shipment by enabled bettery low alert.
     */
    private Shipment createShipmentWithEnabledAlert(final Device d) {
        final Shipment s = createDefaultShipment(ShipmentStatus.InProgress, d);
        //enable battery low alerts
        final AlertProfile p = new AlertProfile();
        p.setName("Enter dark environment alert rule");
        p.setCompany(s.getCompany());
        p.setWatchEnterDarkEnvironment(true);
        context.getBean(AlertProfileDao.class).save(p);

        s.setAlertProfile(p);
        context.getBean(ShipmentDao.class).save(s);

        return s;
    }
}
