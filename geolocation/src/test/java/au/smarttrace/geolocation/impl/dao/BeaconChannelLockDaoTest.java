/**
 *
 */
package au.smarttrace.geolocation.impl.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.geolocation.junit.DaoTest;
import au.smarttrace.geolocation.junit.db.DaoTestRunner;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class BeaconChannelLockDaoTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private BeaconChannelLockDao dao;

    /**
     * Default constructor.
     */
    public BeaconChannelLockDaoTest() {
        super();
    }

    @Test
    public void testClearLocksOldestThan() {
        final String g1 = "g1";
        final String g2 = "g2";
        final String b1 = "b1";
        final String b2 = "b2";
        final long t = System.currentTimeMillis() - 10000000l;

        createLock(g1, b1, new Date(t - 100000l));
        createLock(g2, b2, new Date(t + 100000l));

        dao.clearLocksOldestThan(new Date(t));
        assertEquals(1, jdbc.queryForList("select beacon from beaconchannels", new HashMap<>()).size());
    }
    @Test
    public void testCreateOrUpdateLocks() {
        final long t = System.currentTimeMillis() - 10000000l;

        final Set<String> beacons = new HashSet<>();
        beacons.add("b1");
        beacons.add("b2");

        dao.createOrUpdateLocks(beacons, "gateway1", new Date(t));
        assertEquals(2, getBeacons("gateway1").size());

        dao.createOrUpdateLocks(beacons, "gateway2", new Date(t));
        assertEquals(2, getBeacons("gateway1").size());
        assertEquals(0, getBeacons("gateway2").size());
    }

    @Test
    public void testCreateOrUpdateLocksUpdate() {
        final long t = System.currentTimeMillis() - 10000000l;

        final Set<String> beacons = new HashSet<>();
        beacons.add("b1");

        dao.createOrUpdateLocks(beacons, "gateway", new Date(t - 1000000l));
        dao.createOrUpdateLocks(beacons, "gateway", new Date(t));

        //get channel lock time
        final Date date = (Date) jdbc.queryForList("select * from beaconchannels",
                new HashMap<>()).get(0).get("unlockon");

        assertEqualsTime(new Date(t), date);
    }
    @Test
    public void testGetLocked() {
        final long t = System.currentTimeMillis() - 10000000l;

        createLock("g1", "b1", new Date(t - 100000l));
        createLock("g2", "b2", new Date(t + 100000l));

        assertEquals(0, dao.getLocked("g1", new Date(t)).size());

        final Set<String> locked = dao.getLocked("g2", new Date(t));
        assertEquals(1, locked.size());
        assertEquals("b2", locked.iterator().next());
    }
    @Test
    public void testFullLock() {
        final long t = System.currentTimeMillis() - 10000000l;
        final String gateway = "gateway1";

        final Set<String> beacons = new HashSet<>();
        beacons.add("b1");
        beacons.add("b2");
        createLock(gateway, "b1", new Date(t - 10000l));
        createLock(gateway, "b2", new Date(t - 10000l));

        dao.createOrUpdateLocks(beacons, gateway, new Date(t));
        assertEquals(2, dao.getLocked(gateway, new Date(t - 1000)).size());
    }
    /**
     * @param d1
     * @param d2
     */
    private void assertEqualsTime(final Date d1, final Date d2) {
        assertTrue(Math.abs(d1.getTime() - d2.getTime()) < 1001);
    }
    /**
     * @param gateway
     * @return
     */
    private List<String> getBeacons(final String gateway) {
        final Map<String, Object> params = new HashMap<>();
        params.put("gateway", gateway);

        final List<String> beacons = new LinkedList<>();
        for (final Map<String, Object> row: jdbc.queryForList(
                "select beacon from beaconchannels where gateway = :gateway", params)) {
            beacons.add((String) row.get("beacon"));
        }
        return beacons;
    }
    /**
     * @param gateway
     * @param beacon
     * @param unlockOn
     */
    private void createLock(final String gateway, final String beacon, final Date unlockOn) {
        final Map<String, Object> params = new HashMap<>();
        params.put("gateway", gateway);
        params.put("beacon", beacon);
        params.put("unlockon", unlockOn);
        jdbc.update("insert into beaconchannels (beacon, gateway, unlockon)"
                + " values(:beacon, :gateway, :unlockon)", params);
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        //clean up data base
        jdbc.update("delete from beaconchannels", new HashMap<String, Object>());
    }
}
