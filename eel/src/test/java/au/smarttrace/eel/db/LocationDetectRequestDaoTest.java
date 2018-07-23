/**
 *
 */
package au.smarttrace.eel.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import au.smarttrace.eel.DeviceMessage;
import au.smarttrace.eel.StationSignal;
import au.smarttrace.eel.db.junit.DaoTest;
import au.smarttrace.eel.db.junit.DaoTestRunner;
import au.smarttrace.eel.db.junit.DbSupport;
import au.smarttrace.eel.rawdata.BeaconData;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.LocationPackageBody;
import au.smarttrace.eel.rawdata.MessageParser;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class LocationDetectRequestDaoTest {
    @Autowired
    private DbSupport support;
    @Autowired
    private LocationDetectRequestDao dao;

    /**
     * Default constructor.
     */
    public LocationDetectRequestDaoTest() {
        super();
    }

    @Test
    public void testSaveForNextProcessingInDcs() throws IOException {
        //read test message
        final byte[] bytes = getTestMessage();
        final EelMessage rawMessage = new MessageParser().parseMessage(bytes);
        final LocationPackageBody pos = (LocationPackageBody) rawMessage.getPackages().get(1).getBody();
        final BeaconData bs = pos.getBeacons().get(0);

        final DeviceMessage msg = new AccessibleEelMessageHandler().createDeviceMessage(
                bs, pos.getLocation());
        msg.setGateway(rawMessage.getImei());

        dao.sendRequest(msg);

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
        assertFalse(parseStationSignals((String) row.get("stations")).isEmpty());
    }

    @After
    public void tearDown() {
        support.deleteMessages();
        support.deleteDevices();
        support.deleteCompanies();
    }

    /**
     * @param stations
     * @return
     */
    private static List<StationSignal> parseStationSignals(final String stations) {
        final List<StationSignal> s = new LinkedList<>();

        for (final String line : stations.split("\n")) {
            final String[] splittedLine = line.split(Pattern.quote("|"));

            final StationSignal station = new StationSignal();
            //<MCC>|<MNC>|<LAC>|<CI>|<RXLEV>|
            station.setMcc(Integer.parseInt(splittedLine[0]));
            station.setMnc(Integer.parseInt(splittedLine[1]));
            station.setLac(Integer.parseInt(splittedLine[2]));
            station.setCi(Integer.parseInt(splittedLine[3]));
            station.setLevel(Integer.parseInt(splittedLine[4]));

            s.add(station);
        }

        return s;
    }

    /**
     * @return
     */
    private byte[] getTestMessage() {
        try {
            return Hex.decodeHex(("454c04070c56035254407466498967671a002600015685c1800"
                    + "000000000000000000000000000000000000000000000000000000000000000676712003a00025"
                    + "b4800b20200010001fffe000001000f02880e9f0000000000000000000000d000000000fffffff"
                    + "ffffc010198d09b7241cab1a60d411a50676712005f00015b4841f40001880f9e0000000000000"
                    + "00000009bc000000000ffffffffffff0501e33a24b3cbfcf2ef0fd000000484ea7af3d6f2ec0fb"
                    + "10000922164ba04e6f2e51030000071ed68da5efdf2dd0f9b000098d09b7241cab1bd0d3e1a306"
                    + "76712006b00025b4843230001880f9e00000000000000000001334000000000ffffffffffff060"
                    + "1e33a24b3cbfcf2ee0fd5000071ed68da5efdf2ea0fac00000484ea7af3d6f2e70fb6000092216"
                    + "4ba04e6f2e51031000098d09b7241cab1b40d411a4072f062ed17e4f2a30f4b000067671a00260"
                    + "0015b47eb800000006d000000510000004d000016bb0000006d000000510000004d000016bb676"
                    + "712003b000256855cec0002880f570000000000000000000054e000000000ffff0002fffe02010"
                    + "484ea7af3d6f2ed0f7b0000922164ba04e6f2a710670000676712003b000356855e600002880f5"
                    + "7000000000000000000003d4000000000ffff0001fffe02010484ea7af3d6f2ee0f7b000092216"
                    + "4ba04e6f2a4106f0000676712002f000456855fd50002880f57000000000000000000000088000"
                    + "00000fffffffdfffe01010484ea7af3d6f2ee0f7a000067671200530005568561400002880f530"
                    + "0000000000000000000003000000000ffffffffffff04010484ea7af3d6f2ee0f770000922164b"
                    + "a04e6f2d4106e000098d09b7241cab1b70d451aa072f062ed17e4f2a80f3b0000676712002c000"
                    + "2568c89a80201f90001fffe07f1090d0f02880f3f000000001ba801ed0000196800000000fffff"
                    + "fffffff67671a002600035685c1800000026e0000016a000001b60000806c00000201000001180"
                    + "0000168000069b067671200460004568c8b040201f90001fffe07f1090d0f00880f47000000001"
                    + "9bd01aa000007a800000000ffffffffffff0201ceb7222fb2e4b1db0d37154040eef1f5c5e0b1b"
                    + "80d24106067671200460005568c8c800201f90001fffe07f1090d0f00880f4400000000189101a"
                    + "4000007b800000000ffffffffffff0201ceb7222fb2e4b1db0d3314a040eef1f5c5e0b1b90d241"
                    + "06067671200460006568c8dfa0201f90001fffe07f1090d0f00880f430000000017d501a100000"
                    + "7c000000000ffffffff00000201ceb7222fb2e4b1e60d35144040eef1f5c5e0b1b90d261050676"
                    + "71200460007568c8f720201f90001fffe07f1090d0f02880f4100000000175401a200000068000"
                    + "00000ffff0001ffff0201ceb7222fb2e4b1e60d331410400000000000000000000000").toCharArray());
        } catch (final DecoderException e) {
            throw new RuntimeException(e);
        }
    }
}
