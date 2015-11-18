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
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.AlertRule;
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

        AlertRule criticalHot = new AlertRule(AlertType.CriticalHot);
        criticalHot.setTemperature(normalTemperature + 15);
        criticalHot.setTimeOutMinutes(0);
        p.getAlertRules().add(criticalHot);

        criticalHot = new AlertRule(AlertType.CriticalHot);
        criticalHot.setTemperature(normalTemperature + 14);
        criticalHot.setTimeOutMinutes(1);
        p.getAlertRules().add(criticalHot);

        AlertRule criticalLow = new AlertRule(AlertType.CriticalCold);
        criticalLow.setTemperature(normalTemperature -15.);
        criticalLow.setTimeOutMinutes(0);
        p.getAlertRules().add(criticalLow);

        criticalLow = new AlertRule(AlertType.CriticalCold);
        criticalLow.setTemperature(normalTemperature -14.);
        criticalLow.setTimeOutMinutes(1);
        p.getAlertRules().add(criticalLow);

        AlertRule hot = new AlertRule(AlertType.Hot);
        hot.setTemperature(normalTemperature + 3);
        hot.setTimeOutMinutes(0);
        p.getAlertRules().add(hot);

        hot = new AlertRule(AlertType.Hot);
        hot.setTemperature(normalTemperature + 4.);
        hot.setTimeOutMinutes(2);
        p.getAlertRules().add(hot);

        AlertRule low = new AlertRule(AlertType.Cold);
        low.setTemperature(normalTemperature -10.);
        low.setTimeOutMinutes(40);
        p.getAlertRules().add(low);

        low = new AlertRule(AlertType.Cold);
        low.setTemperature(normalTemperature-8.);
        low.setTimeOutMinutes(55);
        p.getAlertRules().add(low);

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

        final TrackerEvent e = createEvent(normalTemperature);
        final Shipment s = e.getShipment();
        e.setShipment(null);
        assertFalse(rule.accept(new RuleContext(e, state)));

        e.setShipment(s);
        assertTrue(rule.accept(new RuleContext(e, state)));

        s.setAlertProfile(null);
        assertFalse(rule.accept(new RuleContext(e, state)));
    }
    @Test
    public void testHandle() {
        final DeviceState state = new DeviceState();
        final String key = "230qwhp";
        state.getTemperatureAlerts().getDates().put(key, new Date());

        rule.handle(new RuleContext(createEvent(normalTemperature), state));
        assertNull(state.getTemperatureAlerts().getDates().get(key));
    }
}
