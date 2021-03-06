/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEventSerializerTest extends AbstractSerializerTest {
    private TrackerEventSerializer serializer = new TrackerEventSerializer(UTC);

    /**
     * Default constructor.
     */
    public TrackerEventSerializerTest() {
        super();
    }

    @Test
    public void testTrackerEvent() {
        final int battery = 12;
        final Long id = 7l;
        final double temperature = 77.77;
        final Date time = new Date(System.currentTimeMillis() - 1000000000L);
        final Date createdOn = new Date(System.currentTimeMillis() - 1000000777L);
        final TrackerEventType type = TrackerEventType.RSP;
        final double latitude = 10.10;
        final double longitude = 11.11;

        TrackerEvent e = new TrackerEvent();
        e.setBattery(battery);
        e.setId(id);
        e.setTemperature(temperature);
        e.setTime(time);
        e.setType(type);
        e.setLatitude(latitude);
        e.setLongitude(longitude);
        e.setCreatedOn(createdOn);

        final JsonObject obj= serializer.toJson(e);
        e = serializer.parseTrackerEvent(obj);

        assertEquals(battery, e.getBattery());
        assertEquals(id, e.getId());
        assertEquals(temperature, e.getTemperature(), 0.00001);
        assertEquals(format(time), format(e.getTime()));
        assertEquals(format(createdOn), format(e.getCreatedOn()));
        assertEquals(type, e.getType());
        assertEquals(latitude, e.getLatitude(), 0.000001);
        assertEquals(longitude, e.getLongitude(), 0.00001);
    }
    @Test
    public void testSerializeDefaultEvent() {
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);

        final JsonObject obj= serializer.toJson(e);
        assertNotNull(serializer.parseTrackerEvent(obj));
    }
}
