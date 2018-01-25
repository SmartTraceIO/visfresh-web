/**
 *
 */
package au.smarttrace.tt18.st;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import au.smarttrace.tt18.MessageParser;
import au.smarttrace.tt18.MessageParserTest;
import au.smarttrace.tt18.RawMessage;
import au.smarttrace.tt18.junit.DaoTest;
import au.smarttrace.tt18.junit.db.DaoTestRunner;
import au.smarttrace.tt18.junit.db.DbSupport;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class MessageDaoTest {
    @Autowired
    private DbSupport support;
    @Autowired
    private MessageDao dao;

    private Long companyId;

    @Before
    public void setUp() {
        companyId = support.createSimpleCompany("JUnit");
    }

    @Test
    public void testCheckDevice() {
        final String imei = "23948703298470983247";
        assertFalse(dao.checkDevice(imei));

        support.createSimpleDevice(companyId, imei);
        assertTrue(dao.checkDevice(imei));
    }

    @Test
    public void testCheckDisabledDevice() {
        final String imei = "23948703298470983247";
        support.createSimpleDevice(companyId, imei);
        assertTrue(dao.checkDevice(imei));

        support.getJdbc().update("update devices set active = false", new HashMap<>());
        assertFalse(dao.checkDevice(imei));
    }

    @Test
    public void testSaveForNextProcessingInDcs() throws IOException {
        //read test message
        final byte[] bytes = MessageParserTest.readTestMessage();
        final RawMessage msg = new MessageParser().parseMessage(bytes);

        //create device for given message
        support.createSimpleDevice(companyId, msg.getImei());

        final DeviceMessage smartTraceMessage = new AccessibleSmartTraceRawMessageHandler().convert(msg);
        dao.saveForNextProcessingInDcs(smartTraceMessage);

        //Field       | Type         | Null | Key | Default           | Extra          |
        //+-------------+--------------+------+-----+-------------------+----------------+
        //| id          | bigint(20)   | NO   | PRI | NULL              | auto_increment |
        //| imei        | varchar(15)  | NO   |     | NULL              |                |
        //| type        | varchar(4)   | NO   |     | NULL              |                |
        //| time        | datetime     | NO   |     | NULL              |                |
        //| battery     | int(11)      | NO   |     | NULL              |                |
        //| temperature | float        | NO   |     | NULL              |                |
        //| processor   | varchar(32)  | YES  |     | NULL              |                |
        //| retryon     | timestamp    | NO   |     | CURRENT_TIMESTAMP |                |
        //| numretry    | int(11)      | NO   |     | 0                 |                |
        //| stations    | varchar(256) | NO   |     | NULL              |                |
        final List<Map<String, Object>> rows = support.getJdbc().queryForList("select * from devicemsg", new HashMap<>());
        assertEquals(1, rows.size());

        final Map<String, Object> row = rows.get(0);
        assertNotNull(row.get("id"));
        assertEquals(msg.getImei(), row.get("imei"));
        assertEquals("AUT", row.get("type"));
        assertTrue(row.get("time") instanceof Date);
        assertTrue(row.get("battery") instanceof Number);
        assertTrue(row.get("temperature") instanceof Number);
        assertNull(row.get("processor"));
        assertTrue(row.get("retryon") instanceof Date);
        assertEquals(msg.getBattery(), ((Number) row.get("battery")).intValue());
        assertNotNull(parseStationSignal((String) row.get("stations")));
    }

    @After
    public void tearDown() {
        support.deleteMessages();
        support.deleteDevices();
        support.deleteCompanies();
    }

    /**
     * @param line
     * @return
     */
    private static StationSignal parseStationSignal(final String line) {
        final String[] splittedLine = line.split(Pattern.quote("|"));

        final StationSignal station = new StationSignal();
        //<MCC>|<MNC>|<LAC>|<CI>|<RXLEV>|
        station.setMcc(Integer.parseInt(splittedLine[0]));
        station.setMnc(Integer.parseInt(splittedLine[1]));
        station.setLac(Integer.parseInt(splittedLine[2]));
        station.setCi(Integer.parseInt(splittedLine[3]));
        station.setLevel(Integer.parseInt(splittedLine[4]));
        return station;
    }
}
