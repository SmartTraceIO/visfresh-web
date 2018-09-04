/**
 *
 */
package au.smarttrace.bt04;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageParserTest {
    private MessageParser parser;

    /**
     * Default constructor.
     */
    public MessageParserTest() {
        super();
    }

    @Before
    public void setUp() {
        parser = new MessageParser();
    }

    @Test
    public void testParse() {
        final String rawData = "356024089973101|1522093635378|21.0512713|105.7945854|0.0|20.0|0.0|\n"
                + "11160058|RT_T|26.28|79.68|-82|0.16822005355867573|98|1522093633809|3901|\n"
                + "11160059|RT_T|26.28|79.68|-82|0.16822005355867573|98|1522093633809|3901|";

        final Bt04Message msg = parser.parse(rawData);

        //message
        // phone-imei|epoch-time|latitude|longitude|altitude|accuracy|speedKPH|<\n>
        assertEquals("356024089973101", msg.getImei());
        assertNotNull(msg.getTime());
        assertEquals(21.0512713, msg.getLatitude(), 0.0001);
        assertEquals(105.7945854, msg.getLongitude(), 0.001);
        assertEquals(0.0, msg.getAltitude(), 0.001);
        assertEquals(20.0, msg.getAccuracy(), 0.1);
        assertEquals(rawData, msg.getRawData());
        assertEquals(2, msg.getBeacons().size());

        //beacon
        // SN|Name|Temperature|Humidity|RSSI|Distance|battery|LastScannedTime|HardwareModel|<\n>
        // 11160058|RT_T|26.28|79.68|-82|0.16822005355867573|98|1522093633809|3901|
        final BeaconSignal b = msg.getBeacons().get(0);
        assertEquals("11160058", b.getSn());
        assertEquals("RT_T", b.getName());
        assertEquals(26.28, b.getTemperature(), 0.01);
        assertEquals(79.68, b.getHumidity(), 0.1);
        assertEquals(0.16822005355867573, b.getDistance(), 0.0001);
        assertEquals(98, b.getBattery(), 0.1);
        assertNotNull(b.getLastScannedTime());
        assertEquals("3901", b.getHardwareModel());
        assertEquals(79.68, b.getHumidity().doubleValue(), 0.001);
    }
    @Test
    public void testUseBeaconOnlyByLatestSchanedTime() {
        final String rawData = "356024089973101|1522093635378|21.0512713|105.7945854|0.0|20.0|0.0|\n"
                + "11160058|RT_T|26.28|79.68|-82|0.16822005355867573|98|1522093631000|3901|\n"
                + "11160058|RT_T|26.28|79.68|-82|0.16822005355867573|99|1522093633809|3901|\n"
                + "11160058|RT_T|26.28|79.68|-82|0.16822005355867573|100|1522093630000|3901|";

        final Bt04Message msg = parser.parse(rawData);

        //beacon
        assertEquals(1, msg.getBeacons().size());
        assertEquals(99., msg.getBeacons().get(0).getBattery(), 0.01);
    }
    @Test
    public void testParseWithEmptyLocations() {
        final String rawData = "356024089973101|1522093635378||||||\n"
                + "11160058|RT_T|26.28|79.68|-82|0.16822005355867573|98|1522093633809|3901|\n"
                + "11160058|RT_T|26.28|79.68|-82|0.16822005355867573|98|1522093633809|3901|";

        final Bt04Message msg = parser.parse(rawData);
        assertNull(msg.getLatitude());
        assertNull(msg.getAccuracy());
        assertNull(msg.getAltitude());
        assertNull(msg.getLongitude());
    }
    @Test
    public void testParseWithTruncatedLocations() {
        final String rawData = "356024089973101|1522093635378|\n"
                + "11160058|RT_T|26.28|79.68|-82|0.16822005355867573|98|1522093633809|3901|\n"
                + "11160058|RT_T|26.28|79.68|-82|0.16822005355867573|98|1522093633809|3901|";

        final Bt04Message msg = parser.parse(rawData);
        assertNull(msg.getLatitude());
        assertNull(msg.getAccuracy());
        assertNull(msg.getAltitude());
        assertNull(msg.getLongitude());
    }
}
