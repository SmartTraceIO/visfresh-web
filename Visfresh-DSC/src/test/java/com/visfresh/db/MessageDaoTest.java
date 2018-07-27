/**
 *
 */
package com.visfresh.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageParser;
import com.visfresh.DeviceMessageType;
import com.visfresh.RadioType;
import com.visfresh.StationSignal;
import com.visfresh.spring.mock.JUnitConfig;

import junit.framework.TestCase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageDaoTest extends TestCase {
    /**
     * Spring context.
     */
    private AnnotationConfigApplicationContext spring;
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
        spring = JUnitConfig.createContext();
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
        final int humidity = 99;
        final RadioType radio = RadioType.lte;
        final String gateway = "1234566789";

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
        message.setHumidity(humidity);
        message.setTime(time);
        message.setType(type);
        message.setRadio(radio);
        message.setNumberOfRetry(numRetry);
        message.setRetryOn(retryOn);
        message.setGateway(gateway);

        dao.create(message);

        //check result
        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from "
                + MessageDao.TABLE, new HashMap<String, Object>());

        assertEquals(1, list.size());

        final Map<String, Object> row = list.get(0);
        assertEquals(battery, row.get(MessageDao.BATTERY_FIELD));
        assertEquals(humidity, ((Number) row.get(MessageDao.HUMIDITY_FIELD)).intValue());
        assertEquals(imei, row.get(MessageDao.IMEI_FIELD));
        assertEquals(radio.name(), row.get(MessageDao.RADIO_FIELD));
        assertEquals(Double.toString(temperature), "" + row.get(MessageDao.TEMPERATURE_FIELD));

        final String dateFormat = "yyyyMMdd HHmm";
        assertEquals(format(time, dateFormat),
                format((Date) row.get(MessageDao.TIME_FIELD), dateFormat));
        assertEquals(format(retryOn, dateFormat),
                format((Date) row.get(MessageDao.RETRYON_FIELD), dateFormat));
        assertEquals(numRetry, row.get(MessageDao.NUMRETRY_FIELD));
        assertEquals(type.toString(), row.get(MessageDao.TYPE_FIELD));
        assertEquals(gateway, row.get(MessageDao.GATEWAY_FIELD));

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

    public void testMarkDeviceMessagesForProcess() {
        final Date retryOn = new Date(System.currentTimeMillis() - 10000);

        final DeviceMessage m1 = new DeviceMessage();
        m1.setImei("11111");
        m1.setTime(new Date());
        m1.setRetryOn(retryOn);
        m1.setType(DeviceMessageType.INIT);

        final DeviceMessage m2 = new DeviceMessage();
        m2.setImei("22222");
        m2.setTime(new Date());
        m2.setRetryOn(retryOn);
        m2.setType(DeviceMessageType.INIT);

        dao.create(m1);
        dao.create(m2);

        dao.markDeviceMessagesForProcess("p1", 1);
        assertEquals(1, jdbcTemplate.queryForList(
                "select * from " + MessageDao.TABLE
                + " where " + MessageDao.PROCESSOR_FIELD + "='p1'",
                new HashMap<String, Object>()).size());

        dao.markDeviceMessagesForProcess("p2", 1000);
        assertEquals(1, jdbcTemplate.queryForList(
                "select * from " + MessageDao.TABLE
                + " where " + MessageDao.PROCESSOR_FIELD + "='p2'",
                new HashMap<String, Object>()).size());
    }
    public void testGetDeviceMessagesForProcess() {
        long time = System.currentTimeMillis() + 3 * 100000L;
        final Date retryOn = new Date(System.currentTimeMillis() - 100000l);

        final DeviceMessage m1 = new DeviceMessage();
        m1.setImei("11111");
        m1.setTime(new Date((time += 100000L)));
        m1.setRetryOn(retryOn);
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
        m2.setRetryOn(retryOn);
        m2.setType(DeviceMessageType.INIT);

        final DeviceMessage m3 = new DeviceMessage();
        m3.setImei("22222");
        m3.setTime(new Date((time += 100000L)));
        m3.setRetryOn(retryOn);
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
        final String dateFormat = "yyyyMMdd:HH:mm";
        assertEquals(format(m1.getTime(), dateFormat), format(msg.getTime(), dateFormat));
        assertEquals(m1.getBattery(), msg.getBattery());
        assertEquals(m1.getImei(), msg.getImei());
        assertEquals(m1.getTemperature(), msg.getTemperature());
        assertEquals(m1.getType(), msg.getType());
        assertEquals(1, msg.getStations().size());
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
        jdbcTemplate.update("delete from " + MessageDao.TABLE,
                new HashMap<String, Object>());
        spring.close();
    }
}
