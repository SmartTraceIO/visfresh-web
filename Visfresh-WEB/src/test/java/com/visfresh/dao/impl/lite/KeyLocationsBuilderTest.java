/**
 *
 */
package com.visfresh.dao.impl.lite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.visfresh.controllers.lite.LiteKeyLocation;
import com.visfresh.controllers.lite.LiteShipment;
import com.visfresh.dao.impl.lite.KeyLocationsBuilder;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class KeyLocationsBuilderTest extends KeyLocationsBuilder {
    private long lastId = 1000;

    /**
     * Default constructor.
     */
    public KeyLocationsBuilderTest() {
        super();
    }

    private LiteShipment createShipment() {
        final long t = System.currentTimeMillis() - 1000000l;

        final LiteShipment s = new LiteShipment();
        s.setActualArrivalDate(new Date(t));
        s.setDeviceSN("12345");
        s.setEstArrivalDate(s.getActualArrivalDate());
        s.setShipmentDate(new Date(t - 1000000000l));
        s.setShipmentId(this.lastId++);
        s.setShippedFrom("ShippedFrom");
        s.setShippedTo("ShippedTo");
        s.setSiblingCount(7);
        s.setStatus(ShipmentStatus.Default);
        s.setTripCount(1);

        return s;
    }
    /**
     * @param groups
     * @return
     */
    private int getTotalLocations(final List<List<LiteKeyLocation>> groups) {
        return groups.stream().mapToInt(g -> g.size()).sum();
    }
    /**
     * @param t temperature.
     * @return key location.
     */
    private LiteKeyLocation createLocation(final double t) {
        final LiteKeyLocation loc = new LiteKeyLocation();
        loc.setId(lastId++);
        loc.setTemperature(t);
        loc.setTime(new Date());
        return loc;
    }

    //Tests:
    @Test
    public void testCreatesSingleGroupForFirstReading() {
        final LiteShipment s = createShipment();
        final long t = System.currentTimeMillis() - 1000000l;

        addNextReading(s, 1l, new Date(t + 10000l), 2., false);
        addNextReading(s, 2l, new Date(t + 20000l), 2., false);
        addNextReading(s, 3l, new Date(t + 30000l), 2., false);

        assertEquals(2, groups.get(s.getShipmentId()).size());
    }
    @Test
    public void testCreatesSingleGroupForReadingWithAlert() {
        final LiteShipment s = createShipment();
        final long t = System.currentTimeMillis() - 1000000l;

        addNextReading(s, 1l, new Date(t + 10000l), 2., false);
        addNextReading(s, 2l, new Date(t + 20000l), 2., false);
        addNextReading(s, 3l, new Date(t + 30000l), 2., true);
        addNextReading(s, 4l, new Date(t + 40000l), 2., false);
        addNextReading(s, 5l, new Date(t + 50000l), 2., false);

        assertEquals(4, groups.get(s.getShipmentId()).size());
    }
    @Test
    public void testCreatesGroupForUpperHot() {
        final double limit = 7.;

        final LiteShipment s = createShipment();
        s.setUpperTemperatureLimit(limit);
        final long t = System.currentTimeMillis() - 1000000l;

        addNextReading(s, 1l, new Date(t + 10000l), 2., false);
        addNextReading(s, 2l, new Date(t + 20000l), 2., false);
        addNextReading(s, 3l, new Date(t + 30000l), limit + 1., false);
        addNextReading(s, 4l, new Date(t + 40000l), limit + 2., false);
        addNextReading(s, 5l, new Date(t + 50000l), 2., false);

        assertEquals(4, groups.get(s.getShipmentId()).size());
    }
    @Test
    public void testCreatesGroupForLowerCold() {
        final double limit = -7.;

        final LiteShipment s = createShipment();
        s.setUpperTemperatureLimit(limit);
        final long t = System.currentTimeMillis() - 1000000l;

        addNextReading(s, 1l, new Date(t + 10000l), 2., false);
        addNextReading(s, 2l, new Date(t + 20000l), 2., false);
        addNextReading(s, 3l, new Date(t + 30000l), limit - 1., false);
        addNextReading(s, 4l, new Date(t + 40000l), limit - 2., false);
        addNextReading(s, 5l, new Date(t + 50000l), 2., false);

        assertEquals(4, groups.get(s.getShipmentId()).size());
    }
    @Test
    public void testTemperatureLimitAndAllertCollision() {
        final double limit = -7.;

        final LiteShipment s = createShipment();
        s.setUpperTemperatureLimit(limit);
        final long t = System.currentTimeMillis() - 1000000l;

        addNextReading(s, 1l, new Date(t + 10000l), 2., false);
        addNextReading(s, 2l, new Date(t + 20000l), 2., false);
        addNextReading(s, 3l, new Date(t + 30000l), limit - 1., true);
        addNextReading(s, 4l, new Date(t + 40000l), limit - 2., true);
        addNextReading(s, 5l, new Date(t + 50000l), limit - 2., false);
        addNextReading(s, 6l, new Date(t + 60000l), 2., false);

        assertEquals(6, groups.get(s.getShipmentId()).size());
    }
    @Test
    public void testSwitchShipment() {
        final LiteShipment s1 = createShipment();
        final LiteShipment s2 = createShipment();
        final long t = System.currentTimeMillis() - 1000000l;

        addNextReading(s1, 1l, new Date(t + 10000l), 2., false);
        addNextReading(s1, 2l, new Date(t + 20000l), 2., false);
        addNextReading(s2, 3l, new Date(t + 30000l), 2., false);
        addNextReading(s2, 4l, new Date(t + 40000l), 2., false);

        assertEquals(2, groups.size());
    }
    @Test
    public void testSwitchShipmentNotifiedListener() {
        final AtomicBoolean notified = new AtomicBoolean(false);
        setListener((s1, s2) -> notified.set(true));

        final LiteShipment s1 = createShipment();
        final LiteShipment s2 = createShipment();
        final long t = System.currentTimeMillis() - 1000000l;

        addNextReading(s1, 1l, new Date(t + 10000l), 2., false);
        addNextReading(s2, 2l, new Date(t + 20000l), 2., false);
        assertTrue(notified.get());
    }
    @Test
    public void testBuild() {
        //add groups for one shipment
        final List<List<LiteKeyLocation>> shipmentGroups = new LinkedList<>();
        final long shipmentId = 1l;
        groups.put(shipmentId, shipmentGroups);

        //create key location group
        final List<LiteKeyLocation> g1 = new LinkedList<>();
        final List<LiteKeyLocation> g2 = new LinkedList<>();
        final List<LiteKeyLocation> g3 = new LinkedList<>();
        shipmentGroups.add(g1);
        shipmentGroups.add(g2);
        shipmentGroups.add(g3);

        //g1
        g1.add(createLocation(1.));

        //g2
        g2.add(createLocation(1.));
        g2.add(createLocation(1.));

        //g3
        g3.add(createLocation(1.));

        setNumberOfKeyLocations(5);
        assertEquals(4, build().get(shipmentId).size());

        setNumberOfKeyLocations(3);
        assertEquals(3, build().get(shipmentId).size());
    }
    @Test
    public void testBuildSeparatesLastReading() {
        //add groups for one shipment
        final List<List<LiteKeyLocation>> shipmentGroups = new LinkedList<>();
        final long shipmentId = 1l;
        groups.put(shipmentId, shipmentGroups);

        //create key location group
        final List<LiteKeyLocation> g1 = new LinkedList<>();
        final List<LiteKeyLocation> g2 = new LinkedList<>();
        final List<LiteKeyLocation> g3 = new LinkedList<>();
        shipmentGroups.add(g1);
        shipmentGroups.add(g2);
        shipmentGroups.add(g3);

        //g1
        g1.add(createLocation(1.));

        //g2
        g2.add(createLocation(1.));
        g2.add(createLocation(1.));

        //g3
        g3.add(createLocation(1.));
        g3.add(createLocation(1.));
        g3.add(createLocation(1.));

        setNumberOfKeyLocations(3);
        final Map<Long, List<LiteKeyLocation>> built = build();
        assertEquals(4, built.get(shipmentId).size());
    }
    @Test
    public void testFinalSplit() {
        //create and populate groups
        final List<List<LiteKeyLocation>> groups = new LinkedList<>();

        final List<LiteKeyLocation> g1 = new LinkedList<>();
        g1.add(createLocation(1.));
        g1.add(createLocation(1.));
        g1.add(createLocation(1.));

        final List<LiteKeyLocation> g2 = new LinkedList<>();
        g2.add(createLocation(1.));
        g2.add(createLocation(1.));
        g2.add(createLocation(1.));

        final List<LiteKeyLocation> g3 = new LinkedList<>();
        g3.add(createLocation(1.));
        g3.add(createLocation(1.));
        g3.add(createLocation(1.));

        groups.add(g1);
        groups.add(g2);
        groups.add(g3);

        //check result
        setNumberOfKeyLocations(100);
        List<List<LiteKeyLocation>> result = finalSplit(groups);
        assertEquals(9, result.size());
        assertEquals(9, getTotalLocations(result));

        setNumberOfKeyLocations(9);
        result = finalSplit(groups);
        assertEquals(9, result.size());
        assertEquals(9, getTotalLocations(result));

        setNumberOfKeyLocations(8);
        result = finalSplit(groups);
        assertEquals(8, result.size());
        assertEquals(9, getTotalLocations(result));

        setNumberOfKeyLocations(3);
        result = finalSplit(groups);
        assertEquals(3, result.size());
        assertEquals(9, getTotalLocations(result));

        setNumberOfKeyLocations(2);
        result = finalSplit(groups);
        assertEquals(3, result.size());
        assertEquals(9, getTotalLocations(result));

        setNumberOfKeyLocations(0);
        result = finalSplit(groups);
        assertEquals(3, result.size());
        assertEquals(9, getTotalLocations(result));
    }

    @Test
    public void testBuildKeyLocations() {
        final Map<Long, List<List<LiteKeyLocation>>> all = new HashMap<>();
        //add groups for one shipment
        final List<List<LiteKeyLocation>> groups = new LinkedList<>();
        final long shipmentId = 1l;
        all.put(shipmentId, groups);

        //create key location group
        final List<LiteKeyLocation> g1 = new LinkedList<>();
        final List<LiteKeyLocation> g2 = new LinkedList<>();
        final List<LiteKeyLocation> g3 = new LinkedList<>();
        groups.add(g1);
        groups.add(g2);
        groups.add(g3);

        //g1
        g1.add(createLocation(1.));

        //g2
        g2.add(createLocation(1.));
        g2.add(createLocation(1.));

        //g3
        g3.add(createLocation(1.));
        g3.add(createLocation(1.));
        g3.add(createLocation(1.));

        final Map<Long, List<LiteKeyLocation>> built = buildKeyLocations(all);
        assertEquals(3, built.get(shipmentId).size());
    }
    @Test
    public void testGetBestKeyLocation() {
        //create key location group
        final List<LiteKeyLocation> group = new LinkedList<>();

        //check for one location
        final LiteKeyLocation l1 = createLocation(1.);
        group.add(l1);

        assertEquals(l1.getId(), getBestKeyLocation(group).getId());

        //test for several locations
        group.add(createLocation(2.));

        final LiteKeyLocation middle = createLocation(3.01);
        group.add(middle);
        group.add(createLocation(4.));
        group.add(createLocation(6.));

        assertEquals(middle.getId(), getBestKeyLocation(group).getId());
    }
}
