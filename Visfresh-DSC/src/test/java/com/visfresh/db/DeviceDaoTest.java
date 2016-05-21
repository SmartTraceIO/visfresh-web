/**
 *
 */
package com.visfresh.db;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.visfresh.Device;
import com.visfresh.spring.mock.JUnitConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDaoTest extends TestCase {
    /**
     * Spring context.
     */
    private AnnotationConfigApplicationContext spring;
    /**
     * DAO to test.
     */
    private DeviceDao dao;
    /**
     * JDBC helper.
     */
    private NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public DeviceDaoTest() {
        super();
    }
    /**
     * @param name test case name.
     */
    public DeviceDaoTest(final String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        spring = JUnitConfig.createContext();
        dao = spring.getBean(DeviceDao.class);
        jdbc = spring.getBean(NamedParameterJdbcTemplate.class);
    }

    public void testGetByImei() {
        final String description = "Any device";
        final String name = "DeviceName";
        final String imei = "9328749587";
        final boolean active = true;

        createDevice(imei, name, description, active);

        final Device device = dao.getByImei(imei);
        assertEquals(description, device.getDescription());
        assertEquals(imei, device.getImei());
        assertEquals(name, device.getName());
        assertEquals(active, device.isActive());
    }

    public void testGetCompanyEmail() {
        final String imei = "9328749587";
        final String email = "a@b.c";

        createDevice(imei, "DeviceName", "Any device", true);
        final Long companyId = createCompany("JUnit", email);

        //set company to device
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("imei", imei);
        params.put("company", companyId);

        jdbc.update("update devices set company = :company where imei = :imei", params);

        //test method getCompanyEmail
        assertEquals(email, dao.getCompanyEmail(imei));
    }

    /**
     * @param imei
     * @param name
     * @param description
     * @param active
     */
    private void createDevice(final String imei, final String name,
            final String description, final boolean active) {
        final Map<String, Object> params = new HashMap<String, Object>();

        params.put("description", description);
        params.put("name", name);
        params.put("imei", imei);
        params.put("active", active);

        final String sql = "insert into " + DeviceDao.TABLE + "("
                + DeviceDao.DESCRIPTION_FIELD
                + "," + DeviceDao.NAME_FIELD
                + "," + DeviceDao.IMEI_FIELD
                + "," + DeviceDao.ACTIVE_FIELD
                + ") values(:description, :name, :imei, :active)";

        jdbc.update(sql, params);
    }
    /**
     * @param name company name.
     * @param email default company email.
     * @return company ID.
     */
    private Long createCompany(final String name, final String email) {
        final Map<String, Object> params = new HashMap<String, Object>();

        params.put("name", name);
        params.put("email", email);

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update("insert into companies (name, email) values(:name, :email)",
                new MapSqlParameterSource(params), keyHolder);
        return keyHolder.getKey().longValue();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        //clean up data base
        jdbc.update("delete from " + DeviceDao.TABLE, new HashMap<String, Object>());
        jdbc.update("delete from companies", new HashMap<String, Object>());
        spring.close();
    }
}
