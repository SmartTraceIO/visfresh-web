/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Language;
import com.visfresh.io.InterimStopDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopSerializerTest extends AbstractSerializerTest {
    private InterimStopSerializer serializer;

    /**
     * Default constructor.
     */
    public InterimStopSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new InterimStopSerializer(Language.English, TimeZone.getDefault());
    }

    @Test
    public void testSaveInterimStopRequest() {
        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final Long shipmentId = 77l;
        final int time = 14;
        final Long locationId = 87l;

        InterimStopDto req = new InterimStopDto();

        req.setDate(date);
        req.setShipmentId(shipmentId);
        req.setTime(time);
        req.setLocationId(locationId);

        req = this.serializer.parseInterimStopDto(serializer.toJson(req));

        assertEquals(format(date), format(req.getDate()));
        assertEquals(locationId, req.getLocationId());
        assertEquals(shipmentId, req.getShipmentId());
        assertEquals(time, req.getTime());
    }
}
