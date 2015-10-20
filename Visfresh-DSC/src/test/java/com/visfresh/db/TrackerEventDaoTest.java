/**
 *
 */
package com.visfresh.db;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.Device;
import com.visfresh.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEventDaoTest extends TestCase {
    /**
     * Spring context.
     */
    private ClassPathXmlApplicationContext spring;
    /**
     * DAO to test.
     */
    private TrackerEventDao dao;
    /**
     * JDBC helper.
     */
    private NamedParameterJdbcTemplate jdbc;
    private Device device;

    /**
     * Default constructor.
     */
    public TrackerEventDaoTest() {
        super();
    }
    /**
     * @param name test case name.
     */
    public TrackerEventDaoTest(final String name) {
        super(name);
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        spring = new ClassPathXmlApplicationContext("application-context-junit.xml");
        dao = spring.getBean(TrackerEventDao.class);
        jdbc = spring.getBean(NamedParameterJdbcTemplate.class);

        //create device
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("description", "Any Description");
        params.put("name", "JUnit");
        params.put("id", "JUnit");
        final String imei = "923874984";
        params.put("imei", imei);
        params.put("sn", "12345");

        final String sql = "insert into " + DeviceDao.TABLE + "("
                + DeviceDao.DESCRIPTION_FIELD
                + "," + DeviceDao.NAME_FIELD
                + "," + DeviceDao.ID_FIELD
                + "," + DeviceDao.IMEI_FIELD
                + "," + DeviceDao.SN_FIELD
                + ") values(:description, :name, :id, :imei, :sn)";
        jdbc.update(sql, params);
        this.device = spring.getBean(DeviceDao.class).getByImei(imei);
    }

    public void testCreate() {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(10);
        e.setTemperature(50.50);
        e.setTime(new Date());
        e.setType("AUT");

        dao.create(device, e);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        //clean up data base
        jdbc.update("delete from " + TrackerEventDao.TABLE, new HashMap<String, Object>());
        jdbc.update("delete from " + DeviceDao.TABLE, new HashMap<String, Object>());
        spring.close();
    }
}
