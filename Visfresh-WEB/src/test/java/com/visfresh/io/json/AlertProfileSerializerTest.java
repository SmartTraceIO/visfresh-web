/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TemperatureIssue;

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
        serializer = new AlertProfileSerializer(UTC);
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

        AlertProfile p = new AlertProfile();
        p.setDescription(description);
        p.setId(id);
        p.setName(name);
        p.setWatchBatteryLow(watchBatteryLow);
        p.setWatchEnterBrightEnvironment(watchEnterBrightEnvironment);
        p.setWatchEnterDarkEnvironment(watchEnterDarkEnvironment);
        p.setWatchMovementStart(watchMovementStart);
        p.setWatchMovementStop(watchMovementStop);
        p.getTemperatureIssues().add(new TemperatureIssue(AlertType.Hot));
        p.getTemperatureIssues().add(new TemperatureIssue(AlertType.CriticalHot));

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
        assertEquals(2, p.getTemperatureIssues().size());
    }
    @Test
    public void testTemperatureIssues() {
        final double temperature = 15.;
        final int timeOutMinutes = 20;
        final AlertType type = AlertType.CriticalCold;
        final Long id = 77l;

        TemperatureIssue issue = new TemperatureIssue();

        issue.setId(id);
        issue.setTemperature(temperature);
        issue.setTimeOutMinutes(timeOutMinutes);
        issue.setType(type);

        final JsonObject obj = serializer.toJson(issue);
        issue = serializer.parseTemperatureIssue(obj);

        assertEquals(temperature, issue.getTemperature(), 0.0001);
        assertEquals(type, issue.getType());
        assertEquals(timeOutMinutes, issue.getTimeOutMinutes());
        assertEquals(id, issue.getId());
    }
}
