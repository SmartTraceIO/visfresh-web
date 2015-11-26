/**
 *
 */
package com.visfresh.db;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDaoTest extends TestCase {
    /**
     * Spring context.
     */
    private ClassPathXmlApplicationContext spring;
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
        spring = new ClassPathXmlApplicationContext("application-context-junit.xml");
        dao = spring.getBean(DeviceDao.class);
        jdbc = spring.getBean(NamedParameterJdbcTemplate.class);
    }

    public void testGetByImei() {
        final String description = "Any device";
        final String name = "DeviceName";
        final String imei = "9328749587";
        final String sn = "12345";

        final Map<String, Object> params = new HashMap<String, Object>();

        params.put("description", description);
        params.put("name", name);
        params.put("imei", imei);
        params.put("sn", sn);

        final String sql = "insert into " + DeviceDao.TABLE + "("
                + DeviceDao.DESCRIPTION_FIELD
                + "," + DeviceDao.NAME_FIELD
                + "," + DeviceDao.IMEI_FIELD
                + "," + DeviceDao.SN_FIELD
                + ") values(:description, :name, :imei, :sn)";

        jdbc.update(sql, params);

        final Device device = dao.getByImei(imei);
        assertEquals(description, device.getDescription());
        assertEquals(imei, device.getImei());
        assertEquals(name, device.getName());
        assertEquals(sn, device.getSn());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        //clean up data base
        jdbc.update("delete from " + DeviceDao.TABLE, new HashMap<String, Object>());
        spring.close();
    }
}
