/**
 *
 */
package com.visfresh.db;

import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.visfresh.DeviceMessageType;
import com.visfresh.Location;
import com.visfresh.ResolvedDeviceMessage;
import com.visfresh.SystemMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageDaoTest extends TestCase {
    private ClassPathXmlApplicationContext spring;
    private SystemMessageDao dao;
    private NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public SystemMessageDaoTest() {
        super();
    }
    /**
     * @param name test case name.
     */
    public SystemMessageDaoTest(final String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        spring = new ClassPathXmlApplicationContext("application-context-junit.xml");
        dao = spring.getBean(SystemMessageDao.class);
        jdbc = spring.getBean(NamedParameterJdbcTemplate.class);
    }

    public void testSendSystemMessage() {
        final int battery = 90;
        final String imei = "09098098";
        final Location location = new Location();
        location.setLatitude(100.500);
        location.setLongitude(100.500);
        final int numberOfRetry = 4;
        final Date retryOn = new Date(System.currentTimeMillis() + 10000000l);
        final double temperature = 36.6;

        //create device command
        final ResolvedDeviceMessage m = new ResolvedDeviceMessage();
        m.setBattery(battery);
        m.setImei(imei);
        m.setLocation(location);
        m.setNumberOfRetry(numberOfRetry);
        m.setRetryOn(retryOn);
        m.setTemperature(temperature);
        m.setTime(new Date());
        m.setType(DeviceMessageType.AUT);

        SystemMessage sm = dao.sendSystemMessageFor(m);
        sm = dao.findOne(sm.getId());

        final Reader in = new StringReader(sm.getMessageInfo());
        final JsonElement e = new JsonParser().parse(in);
        assertNotNull(e);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        //clean up data base
        jdbc.update("delete from " + SystemMessageDao.TABLE, new HashMap<String, Object>());
        spring.close();
    }
}