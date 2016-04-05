/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.io.SimulatorDto;
import com.visfresh.io.StartSimulatorRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SimulatorSerializerTest {
    /**
     * Serializer to test.
     */
    private SimulatorSerializer ser;

    /**
     * Default constructor.
     */
    public SimulatorSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        ser = new SimulatorSerializer(TimeZone.getDefault());
    }

    @Test
    public void testSerializeDto() {
        final String sourceDevice = "sourceDevice";
        final String targetDevice = "targetDevice";
        final String user = "user";
        final boolean started = true;

        SimulatorDto dto = new SimulatorDto();
        dto.setSourceDevice(sourceDevice);
        dto.setTargetDevice(targetDevice);
        dto.setUser(user);
        dto.setStarted(started);

        dto = ser.parseSimulator(ser.toJson(dto));

        assertEquals(sourceDevice, dto.getSourceDevice());
        assertEquals(targetDevice, dto.getTargetDevice());
        assertEquals(user, dto.getUser());
        assertEquals(started, dto.isStarted());
    }
    @Test
    public void testSerializeStartRequest() {
        final String endDate = "end-date";
        final String startDate = "start-date";
        final String user = "user";
        final int velosity = 111;

        StartSimulatorRequest req = new StartSimulatorRequest();

        req.setEndDate(endDate);
        req.setStartDate(startDate);
        req.setUser(user);
        req.setVelosity(velosity);

        req = ser.parseStartRequest(ser.toJson(req));

        assertEquals(endDate, req.getEndDate());
        assertEquals(startDate, req.getStartDate());
        assertEquals(user, req.getUser());
        assertEquals(velosity, req.getVelosity());
    }
}
