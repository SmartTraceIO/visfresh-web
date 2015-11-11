/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NormalTemperatureRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private Device device;
    private Shipment shipment;
    private double normalTemperature;

    /**
     * Default constructor.
     */
    public NormalTemperatureRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        this.rule = engine.getRule(NormalTemperatureRule.NAME);
        this.device = createDevice("90324870987");

        final Shipment s = new Shipment();
        s.setCompany(device.getCompany());
        s.setDevice(device);
        s.setShipmentDescription("Autocreated by autostart shipment rule");
        s.setShipmentDate(new Date());

        final AlertProfile p = new AlertProfile();
        p.setName("AnyAlert");
        p.setDescription("Any description");
        p.setCompany(company);

        p.setCriticalHighTemperature(normalTemperature + 15);
        p.setCriticalHighTemperatureForMoreThen(0);
        p.setCriticalHighTemperature2(normalTemperature + 14);
        p.setCriticalHighTemperatureForMoreThen2(1);

        p.setCriticalLowTemperature(normalTemperature -15.);
        p.setCriticalLowTemperatureForMoreThen(0);
        p.setCriticalLowTemperature2(normalTemperature -14.);
        p.setCriticalLowTemperatureForMoreThen2(1);

        p.setHighTemperature(normalTemperature + 3);
        p.setHighTemperatureForMoreThen(0);
        p.setHighTemperature2(normalTemperature + 4.);
        p.setHighTemperatureForMoreThen2(2);

        p.setLowTemperature(normalTemperature -10.);
        p.setLowTemperatureForMoreThen(40);
        p.setLowTemperature2(normalTemperature-8.);
        p.setLowTemperatureForMoreThen2(55);
        context.getBean(AlertProfileDao.class).save(p);

        s.setAlertProfile(p);
        context.getBean(ShipmentDao.class).save(s);
        this.shipment = s;
    }

    /**
     * @param t temperature.
     * @return event.
     */
    private TrackerEvent createEvent(final double t) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(100);
        e.setLatitude(10);
        e.setLongitude(10);
        e.setTemperature(t);
        e.setType("INIT");
        e.setDevice(device);
        e.setTime(new Date());
        e.setShipment(shipment);
        return context.getBean(TrackerEventDao.class).save(e);
    }

    @Test
    public void testAccept() {
        final DeviceState state = new DeviceState();
        assertTrue(rule.accept(new RuleContext(createEvent(normalTemperature), state)));

        final AlertProfile a = shipment.getAlertProfile();
        assertFalse(rule.accept(new RuleContext(createEvent(a.getCriticalHighTemperature()), state)));
        assertFalse(rule.accept(new RuleContext(createEvent(a.getCriticalHighTemperature2()), state)));
        assertFalse(rule.accept(new RuleContext(createEvent(a.getCriticalLowTemperature()), state)));
        assertFalse(rule.accept(new RuleContext(createEvent(a.getCriticalLowTemperature2()), state)));
        assertFalse(rule.accept(new RuleContext(createEvent(a.getHighTemperature()), state)));
        assertFalse(rule.accept(new RuleContext(createEvent(a.getHighTemperature2()), state)));
        assertFalse(rule.accept(new RuleContext(createEvent(a.getLowTemperature()), state)));
        assertFalse(rule.accept(new RuleContext(createEvent(a.getLowTemperature2()), state)));
    }
    @Test
    public void testHandle() {
        final DeviceState state = new DeviceState();
        final String key = "230qwhp";
        state.setDate(key, new Date());

        rule.handle(new RuleContext(createEvent(normalTemperature), state));
        assertNull(state.getDate(key));
    }
}
