/**
 *
 */
package com.visfresh.rules.correctmoving;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.visfresh.entities.Location;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LastLocationInfoParserTest extends LastLocationInfoParser {
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    /**
     * Default constructor.
     */
    public LastLocationInfoParserTest() {
        super();
    }

    @Test
    public void testParse() {
        final Location loc = new Location(123675.12, 9384.98);
        final Date time = new Date(System.currentTimeMillis() - 1000000l);

        LastLocationInfo info = new LastLocationInfo();

        info.setLastLocation(loc);
        info.setLastReadTime(time);

        info = parseLastLocationInfo(toJSon(info));

        assertEquals(loc.getLatitude(), info.getLastLocation().getLatitude(), 0.00001);
        assertEquals(loc.getLongitude(), info.getLastLocation().getLongitude(), 0.00001);
        assertEquals(format.format(time), format.format(info.getLastReadTime()));
    }
}
