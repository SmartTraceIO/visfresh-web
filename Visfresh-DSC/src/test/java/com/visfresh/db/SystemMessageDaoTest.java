/**
 *
 */
package com.visfresh.db;

import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;
import com.visfresh.Location;
import com.visfresh.SystemMessage;
import com.visfresh.spring.mock.JUnitConfig;

import junit.framework.TestCase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageDaoTest extends TestCase {
    private AnnotationConfigApplicationContext spring;
    private SystemMessageDao dao;
    private NamedParameterJdbcTemplate jdbc;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /**
     * Default constructor.
     */
    public SystemMessageDaoTest() {
        super();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
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
        spring = JUnitConfig.createContext();
        dao = spring.getBean(SystemMessageDao.class);
        jdbc = spring.getBean(NamedParameterJdbcTemplate.class);
    }

    public void testSendSystemMessage() throws ParseException {
        final int battery = 90;
        final String imei = "09098098";
        final Location location = new Location();
        location.setLatitude(100.500);
        location.setLongitude(100.500);
        final int numberOfRetry = 4;
        final Date retryOn = new Date(System.currentTimeMillis() + 10000000l);
        final double temperature = 36.6;
        final String gateway = "1234567890";

        //create device command
        final DeviceMessage m = new DeviceMessage();
        m.setBattery(battery);
        m.setImei(imei);
        m.setNumberOfRetry(numberOfRetry);
        m.setRetryOn(retryOn);
        m.setTemperature(temperature);
        m.setTime(new Date());
        m.setType(DeviceMessageType.AUT);
        m.setGateway(gateway);

        SystemMessage sm = dao.sendSystemMessageFor(m, location);
        sm = dao.findOne(sm.getId());

        assertEquals(m.getImei(), sm.getGroup());

        final Reader in = new StringReader(sm.getMessageInfo());
        final JsonObject json = new JsonParser().parse(in).getAsJsonObject();

        assertEquals(battery, json.get("battery").getAsInt());
        assertEquals(imei, json.get("imei").getAsString());
        m.setTemperature(temperature);
        assertEquals(temperature, json.get("temperature").getAsDouble(), 0.01);
        assertTrue(Math.abs(m.getTime().getTime()
                - sdf.parse(json.get("time").getAsString()).getTime()) < 1000l);
        assertEquals(DeviceMessageType.AUT.name(), json.get("type").getAsString());
        assertEquals(gateway, json.get("gateway").getAsString());
    }
    public void testSupportsNullLocations() {
        final int battery = 90;
        final String imei = "09098098";
        final int numberOfRetry = 4;
        final Date retryOn = new Date(System.currentTimeMillis() + 10000000l);
        final double temperature = 36.6;

        //create device command
        final DeviceMessage m = new DeviceMessage();
        m.setBattery(battery);
        m.setImei(imei);
        m.setNumberOfRetry(numberOfRetry);
        m.setRetryOn(retryOn);
        m.setTemperature(temperature);
        m.setTime(new Date());
        m.setType(DeviceMessageType.AUT);

        SystemMessage sm = dao.sendSystemMessageFor(m, null);
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
