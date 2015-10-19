/**
 *
 */
package com.visfresh.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageParser;
import com.visfresh.DeviceMessageType;
import com.visfresh.Location;
import com.visfresh.ResolvedDeviceMessage;
import com.visfresh.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageDaoTest extends TestCase {
    /**
     * Spring context.
     */
    private ClassPathXmlApplicationContext spring;
    /**
     * DAO to test.
     */
    private MessageDao dao;
    /**
     * JDBC helper.
     */
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Default constructor.
     */
    public MessageDaoTest() {
        super();
    }
    /**
     * @param name test case name.
     */
    public MessageDaoTest(final String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        spring = new ClassPathXmlApplicationContext("application-context-junit.xml");
        dao = spring.getBean(MessageDao.class);
        jdbcTemplate = spring.getBean(NamedParameterJdbcTemplate.class);
    }

    public void testSaveDeviceMessage() {
        final DeviceMessage message = new DeviceMessage();

        final int battery = 1014;
        final String imei = "012345678901234";
        final double temperature = 77.77;
        final Date time = new Date(System.currentTimeMillis() - 1000000L);
        final DeviceMessageType type = DeviceMessageType.BRT;
        final Date retryOn = new Date(System.currentTimeMillis() + 11111111L);
        final int numRetry = 135;

        //add first station
        final StationSignal s1 = new StationSignal();
        s1.setCi(1);
        s1.setLac(2);
        s1.setLevel(3);
        s1.setMcc(4);
        s1.setMnc(5);
        message.getStations().add(s1);

        //add second station
        final StationSignal s2 = new StationSignal();
        s2.setCi(6);
        s2.setLac(7);
        s2.setLevel(8);
        s2.setMcc(9);
        s2.setMnc(10);
        message.getStations().add(s2);

        message.setBattery(battery);
        message.setImei(imei);
        message.setTemperature(temperature);
        message.setTime(time);
        message.setType(type);
        message.setNumberOfRetry(numRetry);
        message.setRetryOn(retryOn);

        dao.create(message);

        //check result
        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from "
                + MessageDao.DEVICE_MESSAGES_TABLE, new HashMap<String, Object>());

        assertEquals(1, list.size());

        final Map<String, Object> row = list.get(0);
        assertEquals(battery, row.get(MessageDao.BATTERY_FIELD));
        assertEquals(imei, row.get(MessageDao.IMEI_FIELD));
        assertEquals(Double.toString(temperature), "" + row.get(MessageDao.TEMPERATURE_FIELD));
        assertEquals(format(time, "yyyyMMdd HHmmss"),
                format((Date) row.get(MessageDao.TIME_FIELD), "yyyyMMdd HHmmss"));
        assertEquals(format(retryOn, "yyyyMMdd HHmmss"),
                format((Date) row.get(MessageDao.RETRYON_FIELD), "yyyyMMdd HHmmss"));
        assertEquals(numRetry, row.get(MessageDao.NUMRETRY_FIELD));
        assertEquals(type.toString(), row.get(MessageDao.TYPE_FIELD));

        //Assert stations
        final String encodedStations = (String) row.get(MessageDao.STATIONS_FIELD);
        final String[] lines = encodedStations.trim().split("\n");
        assertEquals(2, lines.length);

        //first station
        final StationSignal ss1 = DeviceMessageParser.parseStationSignal(lines[0]);
        assertEquals(s1.getCi(), ss1.getCi());
        assertEquals(s1.getLac(), ss1.getLac());
        assertEquals(s1.getLevel(), ss1.getLevel());
        assertEquals(s1.getMcc(), ss1.getMcc());
        assertEquals(s1.getMnc(), ss1.getMnc());

        //second station
        final StationSignal ss2 = DeviceMessageParser.parseStationSignal(lines[1]);
        assertEquals(s2.getCi(), ss2.getCi());
        assertEquals(s2.getLac(), ss2.getLac());
        assertEquals(s2.getLevel(), ss2.getLevel());
        assertEquals(s2.getMcc(), ss2.getMcc());
        assertEquals(s2.getMnc(), ss2.getMnc());
    }
    public void testSaveResolvedMessage() {
        final ResolvedDeviceMessage message = new ResolvedDeviceMessage();

        final int battery = 1014;
        final String imei = "012345678901234";
        final double temperature = 77.77;
        final Date time = new Date(System.currentTimeMillis() - 1000000L);
        final DeviceMessageType type = DeviceMessageType.BRT;
        final Date retryOn = new Date(System.currentTimeMillis() + 11111111L);
        final int numRetry = 135;
        final double latitude = 100.500;
        final double longitude = 100.501;

        message.setBattery(battery);
        message.setImei(imei);
        message.setTemperature(temperature);
        message.setTime(time);
        message.setType(type);
        message.setLocation(new Location());
        message.setNumberOfRetry(numRetry);
        message.setRetryOn(retryOn);
        message.getLocation().setLatitude(latitude);
        message.getLocation().setLongitude(longitude);

        dao.create(message);

        //check result
        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from "
                + MessageDao.RESOLVED_MESSAGES_TABLE, new HashMap<String, Object>());

        assertEquals(1, list.size());

        final Map<String, Object> row = list.get(0);
        assertEquals(battery, row.get(MessageDao.BATTERY_FIELD));
        assertEquals(imei, row.get(MessageDao.IMEI_FIELD));
        assertEquals("" + temperature, "" + row.get(MessageDao.TEMPERATURE_FIELD));
        assertEquals(format(time, "yyyyMMdd HHmmss"),
                format((Date) row.get(MessageDao.TIME_FIELD), "yyyyMMdd HHmmss"));
        assertEquals(type.toString(), row.get(MessageDao.TYPE_FIELD));
        assertEquals(format(retryOn, "yyyyMMdd HHmmss"),
                format((Date) row.get(MessageDao.RETRYON_FIELD), "yyyyMMdd HHmmss"));
        assertEquals(numRetry, row.get(MessageDao.NUMRETRY_FIELD));
        assertEquals((int) (latitude * 10000), row.get(MessageDao.LATITUDE_FIELD));
        assertEquals((int) (longitude * 10000), row.get(MessageDao.LONGITUDE_FIELD));
    }

    public void testMarkDeviceMessagesForProcess() {
        final DeviceMessage m1 = new DeviceMessage();
        m1.setImei("11111");
        m1.setTime(new Date());
        m1.setType(DeviceMessageType.INIT);

        final DeviceMessage m2 = new DeviceMessage();
        m2.setImei("22222");
        m2.setTime(new Date());
        m2.setType(DeviceMessageType.INIT);

        dao.create(m1);
        dao.create(m2);

        dao.markDeviceMessagesForProcess("p1", 1);
        assertEquals(1, jdbcTemplate.queryForList(
                "select * from " + MessageDao.DEVICE_MESSAGES_TABLE
                + " where " + MessageDao.PROCESSOR_FIELD + "='p1'",
                new HashMap<String, Object>()).size());

        dao.markDeviceMessagesForProcess("p2", 1000);
        assertEquals(1, jdbcTemplate.queryForList(
                "select * from " + MessageDao.DEVICE_MESSAGES_TABLE
                + " where " + MessageDao.PROCESSOR_FIELD + "='p2'",
                new HashMap<String, Object>()).size());
    }

    public void testMarkResolvedDeviceMessagesForProcess() {
        final ResolvedDeviceMessage m1 = new ResolvedDeviceMessage();
        m1.setImei("11111");
        m1.setTime(new Date());
        m1.setType(DeviceMessageType.INIT);
        m1.setLocation(new Location());

        final ResolvedDeviceMessage m2 = new ResolvedDeviceMessage();
        m2.setImei("22222");
        m2.setTime(new Date());
        m2.setType(DeviceMessageType.INIT);
        m2.setLocation(new Location());

        dao.create(m1);
        dao.create(m2);

        dao.markResolvedMessagesForProcess("p1", 1);
        assertEquals(1, jdbcTemplate.queryForList(
                "select * from " + MessageDao.RESOLVED_MESSAGES_TABLE
                + " where " + MessageDao.PROCESSOR_FIELD + "='p1'",
                new HashMap<String, Object>()).size());

        dao.markResolvedMessagesForProcess("p2", 1000);
        assertEquals(1, jdbcTemplate.queryForList(
                "select * from " + MessageDao.RESOLVED_MESSAGES_TABLE
                + " where " + MessageDao.PROCESSOR_FIELD + "='p2'",
                new HashMap<String, Object>()).size());
    }

    public void testGetResolvedMessagesForProcess() {
        long time = System.currentTimeMillis() + 3 * 100000L;

        final ResolvedDeviceMessage m1 = new ResolvedDeviceMessage();
        m1.setImei("11111");
        m1.setTime(new Date((time += 100000L)));
        m1.setType(DeviceMessageType.INIT);
        m1.setBattery(101);
        m1.setTemperature(500);
        m1.setLocation(new Location());
        m1.getLocation().setLatitude(33);
        m1.getLocation().setLongitude(45);

        final ResolvedDeviceMessage m2 = new ResolvedDeviceMessage();
        m2.setImei("22222");
        m2.setTime(new Date((time += 100000L)));
        m2.setType(DeviceMessageType.INIT);
        m2.setLocation(new Location());

        final ResolvedDeviceMessage m3 = new ResolvedDeviceMessage();
        m3.setImei("22222");
        m3.setTime(new Date((time += 100000L)));
        m3.setType(DeviceMessageType.INIT);
        m3.setLocation(new Location());

        dao.create(m1);
        dao.create(m2);
        dao.create(m3);

        dao.markResolvedMessagesForProcess("p1", 2);
        dao.markResolvedMessagesForProcess("p2", 1000);

        final List<ResolvedDeviceMessage> list = dao.getResolvedMessagesForProcess("p1");

        assertEquals(2, list.size());

        //check first message
        final ResolvedDeviceMessage msg = list.get(0);

        //check first message
        assertEquals(format(m1.getTime(), "yyyyMMdd:HH:mm:ss"), format(msg.getTime(), "yyyyMMdd:HH:mm:ss"));
        assertEquals(m1.getBattery(), msg.getBattery());
        assertEquals(m1.getImei(), msg.getImei());
        assertEquals(m1.getLocation().getLatitude(), msg.getLocation().getLatitude());
        assertEquals(m1.getLocation().getLongitude(), msg.getLocation().getLongitude());
        assertEquals(m1.getTemperature(), msg.getTemperature());
        assertEquals(m1.getType(), msg.getType());
    }
    public void testGetDeviceMessagesForProcess() {
        long time = System.currentTimeMillis() + 3 * 100000L;

        final DeviceMessage m1 = new DeviceMessage();
        m1.setImei("11111");
        m1.setTime(new Date((time += 100000L)));
        m1.setType(DeviceMessageType.INIT);
        m1.setBattery(101);
        m1.setTemperature(500);

        //add first station
        final StationSignal station = new StationSignal();
        station.setCi(1);
        station.setLac(2);
        station.setLevel(3);
        station.setMcc(4);
        station.setMnc(5);
        m1.getStations().add(station);

        final DeviceMessage m2 = new DeviceMessage();
        m2.setImei("22222");
        m2.setTime(new Date((time += 100000L)));
        m2.setType(DeviceMessageType.INIT);

        final DeviceMessage m3 = new DeviceMessage();
        m3.setImei("22222");
        m3.setTime(new Date((time += 100000L)));
        m3.setType(DeviceMessageType.INIT);

        dao.create(m1);
        dao.create(m2);
        dao.create(m3);

        dao.markDeviceMessagesForProcess("p1", 2);
        dao.markDeviceMessagesForProcess("p2", 1000);

        final List<DeviceMessage> list = dao.getDeviceMessagesForProcess("p1");

        assertEquals(2, list.size());

        //check first message
        final DeviceMessage msg = list.get(0);

        //check first message
        assertEquals(format(m1.getTime(), "yyyyMMdd:HH:mm:ss"), format(msg.getTime(), "yyyyMMdd:HH:mm:ss"));
        assertEquals(m1.getBattery(), msg.getBattery());
        assertEquals(m1.getImei(), msg.getImei());
        assertEquals(m1.getTemperature(), msg.getTemperature());
        assertEquals(m1.getType(), msg.getType());
        assertEquals(1, msg.getStations().size());
    }
    public void testSaveForRetry() throws ParseException {
        ResolvedDeviceMessage message = new ResolvedDeviceMessage();
        message.setImei("11111");
        message.setTime(new Date((System.currentTimeMillis() + 100000L)));
        message.setType(DeviceMessageType.INIT);
        message.setBattery(101);
        message.setTemperature(500);
        message.setLocation(new Location());
        message.getLocation().setLatitude(33);
        message.getLocation().setLongitude(45);

        final String processor = "p1";
        dao.create(message);
        dao.markResolvedMessagesForProcess(processor , 1);
        message = dao.getResolvedMessagesForProcess(processor).get(0);

        final String dateFormat = "yyyy-MM-dd HH:mm:ss";
        final int numRetry = 50;
        message.setNumberOfRetry(numRetry);
        final Date retryOn = new SimpleDateFormat(dateFormat).parse("2021-03-22 11:11:11");
        message.setRetryOn(retryOn);

        dao.saveForRetry(message);

        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from "
                + MessageDao.RESOLVED_MESSAGES_TABLE, new HashMap<String, Object>());

        assertEquals(1, list.size());

        final Map<String, Object> row = list.get(0);
        assertEquals(numRetry, row.get(MessageDao.NUMRETRY_FIELD));
        assertEquals(format(retryOn, dateFormat),
                format((Date) row.get(MessageDao.RETRYON_FIELD), dateFormat));
    }
    /**
     * @param time the time to format.
     * @param format time format.
     * @return formatted date.
     */
    private String format(final Date time, final String format) {
        return new SimpleDateFormat(format).format(time);
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        //clean up data base
        jdbcTemplate.update("delete from " + MessageDao.DEVICE_MESSAGES_TABLE,
                new HashMap<String, Object>());
        jdbcTemplate.update("delete from " + MessageDao.RESOLVED_MESSAGES_TABLE,
                new HashMap<String, Object>());

        spring.close();
    }
}
