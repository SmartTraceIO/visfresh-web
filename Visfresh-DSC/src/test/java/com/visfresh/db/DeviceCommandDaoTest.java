/**
 *
 */
package com.visfresh.db;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.Device;
import com.visfresh.DeviceCommand;
import com.visfresh.spring.mock.JUnitConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceCommandDaoTest extends TestCase {
    private AnnotationConfigApplicationContext spring;
    private DeviceCommandDao dao;
    private NamedParameterJdbcTemplate jdbc;
    private Device device;

    /**
     * Default constructor.
     */
    public DeviceCommandDaoTest() {
        super();
    }
    /**
     * @param name test case name.
     */
    public DeviceCommandDaoTest(final String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        spring = JUnitConfig.createContext();
        dao = spring.getBean(DeviceCommandDao.class);
        jdbc = spring.getBean(NamedParameterJdbcTemplate.class);

        //create device
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("description", "Any Description");
        params.put("name", "JUnit");
        final String imei = "923874984";
        params.put("imei", imei);
        params.put("sn", "12345");

        final String sql = "insert into " + DeviceDao.TABLE + "("
                + DeviceDao.DESCRIPTION_FIELD
                + "," + DeviceDao.NAME_FIELD
                + "," + DeviceDao.IMEI_FIELD
                + ") values(:description, :name, :imei)";
        jdbc.update(sql, params);
        this.device = spring.getBean(DeviceDao.class).getByImei(imei);
    }
    public void testReadAndDelete() {
        //create device command
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("command", "stop");
        params.put("date", new Date());
        params.put("device", device.getImei());

        jdbc.update("INSERT INTO devicecommands(command, device, date)"
                + "VALUES(:command, :device, :date)" , params);

        final List<DeviceCommand> list = dao.getFoDevice(device.getImei());
        assertEquals(1, list.size());

        dao.delete(list.get(0));
        assertEquals(0, dao.getFoDevice(device.getImei()).size());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        //clean up data base
        jdbc.update("delete from " + DeviceCommandDao.TABLE, new HashMap<String, Object>());
        jdbc.update("delete from " + DeviceDao.TABLE, new HashMap<String, Object>());
        spring.close();
    }
}
