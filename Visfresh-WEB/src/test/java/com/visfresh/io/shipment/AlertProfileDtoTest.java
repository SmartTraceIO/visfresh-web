/**
 *
 */
package com.visfresh.io.shipment;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.visfresh.entities.AlertProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileDtoTest {
    /**
     * Default constructor.
     */
    public AlertProfileDtoTest() {
        super();
    }

    @Test
    public void testCopyConstructor() {
        final Long id = 77l;
        final String name = "Alert Profile Name";
        final String description = "Alert Profile Description";
        final boolean watchEnterBrightEnvironment = true;
        final boolean watchEnterDarkEnvironment = true;
        final boolean watchBatteryLow = true;
        final boolean watchMovementStart = true;
        final boolean watchMovementStop = true;
        final double lowerTemperatureLimit = 11.;
        final double upperTemperatureLimit = 12.;

        //create Alert Profile
        final AlertProfile ap = new AlertProfile();
        ap.setId(id);
        ap.setName(name);
        ap.setDescription(description);
        ap.setWatchEnterBrightEnvironment(watchEnterBrightEnvironment);
        ap.setWatchEnterDarkEnvironment(watchEnterDarkEnvironment);
        ap.setWatchMovementStart(watchMovementStart);
        ap.setWatchMovementStop(watchMovementStop);
        ap.setWatchBatteryLow(watchBatteryLow);
        ap.setLowerTemperatureLimit(lowerTemperatureLimit);
        ap.setUpperTemperatureLimit(upperTemperatureLimit);

        final AlertProfileDto dto = new AlertProfileDto(ap);

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(watchEnterBrightEnvironment, dto.isWatchEnterBrightEnvironment());
        assertEquals(watchEnterDarkEnvironment, dto.isWatchEnterDarkEnvironment());
        assertEquals(watchMovementStart, dto.isWatchMovementStart());
        assertEquals(watchMovementStop, dto.isWatchMovementStop());
        assertEquals(watchBatteryLow, dto.isWatchBatteryLow());
        assertEquals(lowerTemperatureLimit, dto.getLowerTemperatureLimit(), 0.001);
        assertEquals(upperTemperatureLimit, dto.getUpperTemperatureLimit(), 0.001);
    }
}
