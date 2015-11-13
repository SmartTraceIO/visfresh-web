/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationSerializerTest extends AbstractSerializerTest {
    /**
     * Serializer to test.
     */
    private LocationSerializer serializer = new LocationSerializer(UTC);

    /**
     * Default constructor.
     */
    public LocationSerializerTest() {
        super();
    }

    @Test
    public void testLocationProfile() {
        final String company = "Sun Microsystems";
        final Long id = 77l;
        final boolean interim = true;
        final String name = "JUnit-Location";
        final String notes = "Any notes";
        final String address = "Odessa, Deribasovskaya 1, apt.1";
        final int radius = 1000;
        final boolean start = true;
        final boolean stop = true;
        final double x = 100.500;
        final double y = 100.501;

        LocationProfile p = new LocationProfile();

        p.setCompanyName(company);
        p.setId(id);
        p.setInterim(interim);
        p.setName(name);
        p.setNotes(notes);
        p.setAddress(address);
        p.setRadius(radius);
        p.setStart(start);
        p.setStop(stop);
        p.getLocation().setLatitude(x);
        p.getLocation().setLongitude(y);

        final JsonObject obj = serializer.toJson(p).getAsJsonObject();
        p = serializer.parseLocationProfile(obj);

        assertEquals(company, p.getCompanyName());
        assertEquals(id, p.getId());
        assertEquals(interim, p.isInterim());
        assertEquals(name, p.getName());
        assertEquals(notes, p.getNotes());
        assertEquals(address, p.getAddress());
        assertEquals(radius, p.getRadius());
        assertEquals(start, p.isStart());
        assertEquals(stop, p.isStop());
        assertEquals(x, p.getLocation().getLatitude(), 0.00001);
        assertEquals(y, p.getLocation().getLongitude(), 0.00001);
    }
}
