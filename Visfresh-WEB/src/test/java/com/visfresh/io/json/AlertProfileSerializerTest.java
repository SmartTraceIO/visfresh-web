/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveActionList;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.lists.ListAlertProfileItem;
import com.visfresh.utils.LocalizationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileSerializerTest extends AbstractSerializerTest {
    private AlertProfileSerializer serializer;
    /**
     * Default constructor.
     */
    public AlertProfileSerializerTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        serializer = new AlertProfileSerializer(null, UTC, TemperatureUnits.Celsius);
    }

    @Test
    public void testAlertProfile() {
        final String description = "Any description";
        final Long id = 7L;
        final String name = "AnyName";
        final boolean watchBatteryLow = true;
        final boolean watchEnterBrightEnvironment = true;
        final boolean watchEnterDarkEnvironment = false;
        final boolean watchMovementStart = true;
        final boolean watchMovementStop = true;
        final double loverTemperatureLimit = -11.11;
        final double upperTemperatureLimit = 22.22;

        AlertProfile p = new AlertProfile();
        p.setDescription(description);
        p.setId(id);
        p.setName(name);
        p.setWatchBatteryLow(watchBatteryLow);
        p.setWatchEnterBrightEnvironment(watchEnterBrightEnvironment);
        p.setWatchEnterDarkEnvironment(watchEnterDarkEnvironment);
        p.setWatchMovementStart(watchMovementStart);
        p.setWatchMovementStop(watchMovementStop);
        p.getAlertRules().add(new TemperatureRule(AlertType.Hot));
        p.getAlertRules().add(new TemperatureRule(AlertType.CriticalHot));
        p.setLowerTemperatureLimit(loverTemperatureLimit);
        p.setUpperTemperatureLimit(upperTemperatureLimit);

        final JsonObject json = serializer.toJson(p).getAsJsonObject();
        p = serializer.parseAlertProfile(json);

        assertEquals(description, p.getDescription());
        assertEquals(id, p.getId());
        assertEquals(name, p.getName());
        assertEquals(watchBatteryLow, p.isWatchBatteryLow());
        assertEquals(watchEnterBrightEnvironment, p.isWatchEnterBrightEnvironment());
        assertEquals(watchEnterDarkEnvironment, p.isWatchEnterDarkEnvironment());
        assertEquals(watchMovementStart, p.isWatchMovementStart());
        assertEquals(watchMovementStop, p.isWatchMovementStop());
        assertEquals(2, p.getAlertRules().size());
        assertEquals(loverTemperatureLimit, p.getLowerTemperatureLimit(), 0.001);
        assertEquals(upperTemperatureLimit, p.getUpperTemperatureLimit(), 0.001);
    }
    @Test
    public void testTemperatureIssues() {
        final double temperature = 15.;
        final int timeOutMinutes = 20;
        final AlertType type = AlertType.CriticalCold;
        final Integer maxRateMinutes = 78;
        final Long id = 77l;

        TemperatureRule issue = new TemperatureRule();

        issue.setId(id);
        issue.setTemperature(temperature);
        issue.setTimeOutMinutes(timeOutMinutes);
        issue.setType(type);
        issue.setCumulativeFlag(true);
        issue.setMaxRateMinutes(maxRateMinutes);

        final JsonObject obj = serializer.toJson(issue);
        issue = serializer.parseTemperatureIssue(obj);

        assertEquals(temperature, issue.getTemperature(), 0.0001);
        assertEquals(type, issue.getType());
        assertEquals(timeOutMinutes, issue.getTimeOutMinutes());
        assertEquals(maxRateMinutes, issue.getMaxRateMinutes());
        assertEquals(id, issue.getId());
        assertTrue(issue.isCumulativeFlag());
    }
    @Test
    public void testTemperatureUnits() {
        final double temperature = 15.;

        final TemperatureRule issue = new TemperatureRule();

        issue.setId(77l);
        issue.setTemperature(temperature);
        issue.setType(AlertType.CriticalCold);

        final TemperatureUnits units = TemperatureUnits.Fahrenheit;
        final AlertProfileSerializer serializer = new AlertProfileSerializer(null, UTC, units);

        final JsonObject obj = serializer.toJson(issue);

        assertEquals(LocalizationUtils.convertToUnits(temperature, units),
                obj.get("temperature").getAsDouble(), 0.00001);

    }
    @Test
    public void testListAlertProfileItem() {
        final String alertProfileDescription = "alertProfileDescription";
        final long alertProfileId = 77l;
        final String alertProfileName = "JUnit alert profile";

        ListAlertProfileItem item = new ListAlertProfileItem();
        item.setAlertProfileDescription(alertProfileDescription);
        item.setAlertProfileId(alertProfileId);
        item.setAlertProfileName(alertProfileName);
        item.getAlertRuleList().add("abra");
        item.getAlertRuleList().add("kadabra");

        final JsonObject json = serializer.toJson(item);
        item = serializer.parseListAlertProfileItem(json);

        assertEquals(alertProfileDescription, item.getAlertProfileDescription());
        assertEquals(alertProfileId, item.getAlertProfileId());
        assertEquals(alertProfileName, item.getAlertProfileName());

        assertEquals("abra", item.getAlertRuleList().get(0));
        assertEquals("kadabra", item.getAlertRuleList().get(1));
    }
    @Test
    public void testCorrectiveActions() {
        AlertProfile ap = new AlertProfile();
        ap.setId(77l);
        ap.setName("Junit Alerts");

        final Long loId = 44l;
        final String loName = "Light On Actions";
        final Long blId = 55l;
        final String blName = "Battery Low Actions";

        final CorrectiveActionList loActions = new CorrectiveActionList();
        loActions.setId(loId);
        loActions.setName(loName);

        ap.setLightOnCorrectiveActions(loActions);

        final CorrectiveActionList blActions = new CorrectiveActionList();
        blActions.setId(blId);
        blActions.setName(blName);
        ap.setBatteryLowCorrectiveActions(blActions);

        ap = serializer.parseAlertProfile(serializer.toJson(ap));

        assertEquals(loId, ap.getLightOnCorrectiveActions().getId());
        assertEquals(loName, ap.getLightOnCorrectiveActions().getName());

        assertEquals(blId, ap.getBatteryLowCorrectiveActions().getId());
        assertEquals(blName, ap.getBatteryLowCorrectiveActions().getName());
    }
    @Test
    public void testRuleCorrectiveActions() {
        final AlertProfile ap = new AlertProfile();
        ap.setId(77l);
        ap.setName("Junit Alerts");

        final Long id = 44l;
        final String name = "Light On Actions";

        final TemperatureRule issue = new TemperatureRule();
        issue.setId(77l);
        issue.setTemperature(19);
        issue.setTimeOutMinutes(5);
        issue.setType(AlertType.CriticalCold);
        issue.setCumulativeFlag(true);

        CorrectiveActionList actions = new CorrectiveActionList();
        actions.setId(id);
        actions.setName(name);

        issue.setCorrectiveActions(actions);
        ap.getAlertRules().add(issue);

        actions = serializer.parseAlertProfile(serializer.toJson(ap))
                .getAlertRules().get(0).getCorrectiveActions();

        assertEquals(id, actions.getId());
        assertEquals(name, actions.getName());
    }
}
