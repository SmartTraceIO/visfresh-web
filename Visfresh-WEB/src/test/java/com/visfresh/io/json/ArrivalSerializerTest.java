/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalSerializerTest extends AbstractSerializerTest {
    private ArrivalSerializer serializer = new ArrivalSerializer(UTC);

    /**
     * Default constructor.
     */
    public ArrivalSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer.setDeviceResolver(resolver);
        serializer.setShipmentResolver(resolver);
    }
    @Test
    public void testArrival() {
        final Device device = createDevice("92348072043987");
        final Shipment shipment = createShipment();
        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final Long id = 77L;
        final int numberOfMetersOfArrival = 37;

        Arrival a = new Arrival();
        a.setDate(date);
        a.setDevice(device);
        a.setId(id);
        a.setNumberOfMettersOfArrival(numberOfMetersOfArrival);
        a.setShipment(shipment);

        final JsonElement e = serializer.toJson(a);
        a = serializer.parseArrival(e);

        assertEquals(format(date), format(a.getDate()));
        assertEquals(device.getId(), a.getDevice().getId());
        assertEquals(id, a.getId());
        assertEquals(numberOfMetersOfArrival, a.getNumberOfMettersOfArrival());
        assertEquals(shipment.getId(), a.getShipment().getId());
    }
}
