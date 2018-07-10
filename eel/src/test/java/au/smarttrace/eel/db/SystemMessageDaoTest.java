/**
 *
 */
package au.smarttrace.eel.db;

import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import au.smarttrace.eel.DeviceMessage;
import au.smarttrace.eel.Location;
import au.smarttrace.eel.SystemMessage;
import au.smarttrace.eel.db.junit.DaoTest;
import au.smarttrace.eel.db.junit.DaoTestRunner;
import junit.framework.TestCase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class SystemMessageDaoTest extends TestCase {
    @Autowired
    private SystemMessageDao dao;
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public SystemMessageDaoTest() {
        super();
    }

    @Test
    public void testSendSystemMessage() {
        final int battery = 90;
        final String imei = "09098098";
        final Location location = new Location();
        location.setLatitude(100.500);
        location.setLongitude(100.500);
        final Date time = new Date(System.currentTimeMillis() + 10000000l);
        final double temperature = 36.6;
        final String gateway = "beacon-gateway";

        //create device command
        final DeviceMessage m = new DeviceMessage();
        m.setBattery(battery);
        m.setImei(imei);
        m.setTemperature(temperature);
        m.setTime(time);
        m.setGateway(gateway);

        SystemMessage sm = dao.sendSystemMessageFor(m);
        sm = dao.findOne(sm.getId());

        assertEquals(m.getImei(), sm.getGroup());

        final Reader in = new StringReader(sm.getMessageInfo());
        final JsonObject json = new JsonParser().parse(in).getAsJsonObject();

        //test message
        assertEquals(battery, json.get("battery").getAsInt());
        assertEquals(imei, json.get("imei").getAsString());
        assertEquals(temperature, json.get("temperature").getAsDouble(), 0.001);
        assertTrue(Math.abs(time.getTime() - parseDate(json.get("time").getAsString()).getTime()) < 2000);
        assertEquals(gateway, json.get("gateway").getAsString());
    }
    /**
     * @param str
     * @return
     */
    private Date parseDate(final String str) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(str);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testSupportsNullLocations() {
        final int battery = 90;
        final String imei = "09098098";
        final double temperature = 36.6;

        //create device command
        final DeviceMessage m = new DeviceMessage();
        m.setBattery(battery);
        m.setImei(imei);
        m.setTemperature(temperature);
        m.setTime(new Date());

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
    }
}
