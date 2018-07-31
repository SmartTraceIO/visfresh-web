/**
 *
 */
package au.smarttrace.eel.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import au.smarttrace.eel.Beacon;
import au.smarttrace.eel.GatewayBinding;
import au.smarttrace.eel.db.junit.DaoTest;
import au.smarttrace.eel.db.junit.DaoTestRunner;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class BeaconDaoTest {
    @Autowired
    private BeaconDao dao;
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    private Long company;

    /**
     * Default constructor.
     */
    public BeaconDaoTest() {
        super();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        company = createCompany("JUnit");
    }

    @Test
    public void testGetBeacon() {
        final String imei = "imei-1";
        createBeacon(imei);
        createBeacon("imei-2");

        final Beacon b = dao.getBeaconById(imei);
        assertEquals(imei, b.getImei());
        assertEquals(company, b.getCompany());
        assertTrue(b.isActive());
    }

    @Test
    public void testGetBeaconWithParedPhone() {
        final String beaconId = "imei-1";
        final String phone = "22394870392487";

        createBeacon(beaconId);

        final GatewayBinding g = new GatewayBinding();
        g.setActive(true);
        g.setCompany(company);
        g.setGateway(phone);

        saveGateway(beaconId, g);

        final Beacon b = dao.getBeaconById(beaconId);
        assertNotNull(b.getGateway());

        assertEquals(g.isActive(), b.getGateway().isActive());
        assertEquals(g.getCompany(), b.getGateway().getCompany());
        assertEquals(g.getGateway(), b.getGateway().getGateway());
        assertEquals(g.getId(), b.getGateway().getId());
    }

    @Test
    public void testGetBeaconWithDisabledPairedPhone() {
        final String beaconId = "imei-1";
        final String phone = "22394870392487";

        createBeacon(beaconId);

        final GatewayBinding g = new GatewayBinding();
        g.setActive(false);
        g.setCompany(company);
        g.setGateway(phone);

        saveGateway(beaconId, g);

        final Beacon b = dao.getBeaconById(beaconId);
        assertNotNull(b.getGateway());
        assertFalse(g.isActive());
    }
    @Test
    public void testGetBeaconWithDisabledAndEnabledPairedPhone() {
        final String beaconId = "imei-1";
        final String phone = "22394870392487";

        createBeacon(beaconId);

        final GatewayBinding g2 = new GatewayBinding();
        g2.setActive(false);
        g2.setCompany(company);
        g2.setGateway("230498709723987");

        final GatewayBinding g1 = new GatewayBinding();
        g1.setActive(true);
        g1.setCompany(company);
        g1.setGateway(phone);

        saveGateway(beaconId, g1);
        saveGateway(beaconId, g2);

        final Beacon b = dao.getBeaconById(beaconId);
        assertNotNull(b.getGateway());
        assertEquals(g1.getId(), b.getGateway().getId());
    }


    /**
     * @param beacon
     * @param g
     */
    private void saveGateway(final String beacon, final GatewayBinding g) {
        final Map<String, Object> params = new HashMap<String, Object>();

        params.put("active", true);
        params.put("beaconid", beacon);
        params.put("imei", g.getGateway());
        params.put("company", g.getCompany());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update("insert into pairedphones (active, beaconid, imei, company)"
                + " values(:active, :beaconid, :imei, :company)",
                new MapSqlParameterSource(params), keyHolder);

        g.setId(keyHolder.getKey().longValue());
    }

    /**
     * @param imei beacon IMEI.
     */
    private void createBeacon(final String imei) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("description", "JUnit");
        params.put("name", "JUnit");
        params.put("imei", imei);
        params.put("company", company);
        params.put("active", true);

        final String sql = "insert into devices (description, name, imei, active, company, model)"
                + " values(:description, :name, :imei, :active, :company, 'BT04')";

        jdbc.update(sql, params);
    }
    /**
     * @param name
     * @return
     */
    private Long createCompany(final String name) {
        final Map<String, Object> params = new HashMap<String, Object>();

        params.put("name", name);
        params.put("email", name + "@junit.org");

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update("insert into companies (name, email) values(:name, :email)",
                new MapSqlParameterSource(params), keyHolder);
        return keyHolder.getKey().longValue();
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        //clean up data base
        jdbc.update("delete from pairedphones", new HashMap<String, Object>());
        jdbc.update("delete from devices", new HashMap<String, Object>());
        jdbc.update("delete from companies", new HashMap<String, Object>());
    }
}
