/**
 *
 */
package com.visfresh.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Language;
import com.visfresh.entities.Location;
import com.visfresh.io.json.DeviceStateSerializer;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceStateSerializerTest {
    private DeviceStateSerializer serializer;
    private final DateFormat dateFormat = DateTimeUtils.createDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss", Language.English, TimeZone.getTimeZone("UTC"));

    /**
     * Default constructor.
     */
    public DeviceStateSerializerTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        serializer = new DeviceStateSerializer();
    }
    @Test
    public void testSerialize() {
        DeviceState s = new DeviceState();
        final Location loc = new Location(10., 20.);
        final Date lastReadingTime = new Date(System.currentTimeMillis() - 1111111111l);

        s.setLastLocation(loc);
        s.setLastReadTime(lastReadingTime);

        final String str = serializer.toString(s);
        s = serializer.parseState(str);

        assertNotNull(s);

        assertEquals(loc.getLatitude(), s.getLastLocation().getLatitude(), 0.00001);
        assertEquals(loc.getLongitude(), s.getLastLocation().getLongitude(), 0.00001);
        assertEquals(dateFormat.format(lastReadingTime), dateFormat.format(s.getLastReadTime()));
    }
}
