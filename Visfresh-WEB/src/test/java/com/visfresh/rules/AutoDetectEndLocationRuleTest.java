/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.utils.SerializerUtils;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoDetectEndLocationRuleTest extends AutoDetectEndLocationRule {
    private long lastId;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public AutoDetectEndLocationRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        final Shipment s = new Shipment();
        s.setId(lastId++);
        s.setStatus(ShipmentStatus.InProgress);

        this.shipment = s;
    }

    @Test
    public void testSerializeLocation() {
        final Long id = 7l;
        final int radius = 15;
        final double lat = 1.11111;
        final double lon = 2.22222;
        final int numReadings = 99;

        LocationProfile loc = new LocationProfile();
        loc.setId(id);
        loc.setRadius(radius);
        loc.getLocation().setLatitude(lat);
        loc.getLocation().setLongitude(lon);

        AutodetectData data = new AutodetectData();
        data.setNumReadings(numReadings);
        data.getLocations().add(loc);

        final String json = toJSon(data).toString();
        data = parseAutodetectData(SerializerUtils.parseJson(json).getAsJsonObject());

        assertEquals(numReadings, data.getNumReadings());
        assertEquals(1, data.getLocations().size());

        loc = data.getLocations().get(0);
        assertEquals(id, loc.getId());
        assertEquals(radius, loc.getRadius());
        assertEquals(lat, loc.getLocation().getLatitude(), 0.00000001);
        assertEquals(lon, loc.getLocation().getLongitude(), 0.00000001);
    }
    @Test
    public void testOldVersionSerializationCompatibility() {
        final Long id = 7l;
        final int radius = 15;
        final double lat = 1.11111;
        final double lon = 2.22222;

        LocationProfile loc = new LocationProfile();
        loc.setId(id);
        loc.setRadius(radius);
        loc.getLocation().setLatitude(lat);
        loc.getLocation().setLongitude(lon);

        final JsonArray array = new JsonArray();
        array.add(toJson(loc));

        final String json = array.toString();
        final AutodetectData data = parseAutodetectData(SerializerUtils.parseJson(json));

        assertEquals(1, data.getLocations().size());

        loc = data.getLocations().get(0);
        assertEquals(id, loc.getId());
        assertEquals(radius, loc.getRadius());
        assertEquals(lat, loc.getLocation().getLatitude(), 0.00000001);
        assertEquals(lon, loc.getLocation().getLongitude(), 0.00000001);
    }
    @Test
    public void testNeedAutodetect() {
        final DeviceState state = new DeviceState();

        final LocationProfile loc1 = createLocation(1., 0.);
        final LocationProfile loc2 = createLocation(1., 0.);
        final AutoStartShipment autoStart = createAutoStart(loc1, loc2);

        needAutodetect(autoStart, state);

        final String prop = state.getShipmentProperty(getLocationsKey());
        assertNotNull(prop);

        final AutodetectData data = parseAutodetectData(SerializerUtils.parseJson(prop).getAsJsonObject());

        assertEquals(0, data.getNumReadings());
        final List<LocationProfile> locs = data.getLocations();
        assertEquals(loc1.getId(), locs.get(0).getId());
        assertEquals(loc2.getId(), locs.get(1).getId());
    }
    @Test
    public void testAccept() {
        final DeviceState state = new DeviceState();

        final TrackerEvent e = new TrackerEvent();
        e.setShipment(shipment);
        e.setLatitude(2.0);
        e.setLongitude(0.);

        final LocationProfile loc1 = createLocation(1., 0.);
        final LocationProfile loc2 = createLocation(2., 0.);
        final AutoStartShipment autoStart = createAutoStart(loc1, loc2);

        needAutodetect(autoStart, state);

        //test accept correct
        assertTrue(accept(new RuleContext(e, state)));

        //check accept if not matched location
        e.setLatitude(10.);
        e.setLongitude(11.);
        assertTrue(accept(new RuleContext(e, state)));

        e.setLatitude(2.0);
        e.setLongitude(0.);
        assertTrue(accept(new RuleContext(e, state)));

        //test not accept if not shipment
        e.setShipment(null);
        assertFalse(accept(new RuleContext(e, state)));

        e.setShipment(shipment);
        assertTrue(accept(new RuleContext(e, state)));

        //test not accept not need autodetect
        assertFalse(accept(new RuleContext(e, new DeviceState())));
    }
    @Test
    public void testHandle() {
        final DeviceState state = new DeviceState();

        final TrackerEvent e = new TrackerEvent();
        e.setShipment(shipment);
        e.setLatitude(2.0);
        e.setLongitude(0.);

        final LocationProfile loc1 = createLocation(1., 0.);
        final LocationProfile loc2 = createLocation(2., 0.);
        final AutoStartShipment autoStart = createAutoStart(loc1, loc2);

        needAutodetect(autoStart, state);

        //check ignores first reading
        assertFalse(handle(new RuleContext(e, state)));
        assertTrue(accept(new RuleContext(e, state)));
        assertNull(shipment.getShippedTo());

        //check not autodetect next
        assertFalse(handle(new RuleContext(e, state)));
        assertFalse(accept(new RuleContext(e, state)));

        //test location assigned
        assertEquals(loc2.getId(), shipment.getShippedTo().getId());
    }
    /**
     * @param loc locations.
     * @return
     */
    private AutoStartShipment createAutoStart(final LocationProfile... loc) {
        final AutoStartShipment auto = new AutoStartShipment();
        for (final LocationProfile l : loc) {
            auto.getShippedTo().add(l);
        }
        auto.setId(lastId++);
        return auto;
    }
    /**
     * @param lat
     * @param lon
     * @return
     */
    private LocationProfile createLocation(final double lat, final double lon) {
        final LocationProfile loc = new LocationProfile();
        loc.getLocation().setLatitude(lat);
        loc.getLocation().setLongitude(lon);
        loc.setId(lastId++);
        return loc;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AutoDetectEndLocationRule#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    protected void saveShipment(final Shipment shipment) {
        // disable saving
    }
}
