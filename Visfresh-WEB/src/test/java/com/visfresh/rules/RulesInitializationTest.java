/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RulesInitializationTest extends BaseRuleTest {
    /**
     * Default constructor.
     */
    public RulesInitializationTest() {
        super();
    }

    @Test
    public void testInitialized() {
        assertTrue(engine.hasRule(ArrivalRule.NAME));
        assertTrue(engine.hasRule(AssignShipmentRule.NAME));
        assertTrue(engine.hasRule(AutoDetectEndLocationRule.NAME));
        assertTrue(engine.hasRule(AutoStartShipmentRule.NAME));
        assertTrue(engine.hasRule(BatteryLowAlertRule.NAME));
        assertTrue(engine.hasRule(BatteryRechargedRule.NAME));
        assertTrue(engine.hasRule(CleanShutdownRepeatStateRule.NAME));
        assertTrue(engine.hasRule(CorrectMovingControllRule.NAME));
        assertTrue(engine.hasRule(EnterBrightEnvironmentAlertRule.NAME));
        assertTrue(engine.hasRule(EnterDarkEnvironmentAlertRule.NAME));
        assertTrue(engine.hasRule(EtaCalculationRule.NAME));
        assertTrue(engine.hasRule(InterimStopRule.NAME));
        assertTrue(engine.hasRule(MovementStartAlertRule.NAME));
        assertTrue(engine.hasRule(MovementStopAlertRule.NAME));
        assertTrue(engine.hasRule(RepeatShutdownRule.NAME));
        assertTrue(engine.hasRule(SetShipmentArrivedRule.NAME));
        assertTrue(engine.hasRule(TemperatureAlertRule.NAME));
        assertTrue(engine.hasRule(VeryOldEventRule.NAME));
    }
}
