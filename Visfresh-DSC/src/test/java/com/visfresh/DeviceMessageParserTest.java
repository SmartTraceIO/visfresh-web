/**
 *
 */
package com.visfresh;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;
import au.smarttrace.geolocation.DeviceMessageType;
import au.smarttrace.gsm.StationSignal;
import junit.framework.TestCase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceMessageParserTest extends TestCase {
    /**
     * Parser to test.
     */
    private DeviceMessageParser parser;
    private SimpleDateFormat utcFormat;

    /**
     * Default constructor.
     */
    public DeviceMessageParserTest() {
        super();
    }

    /**
     * @param name test case name.
     */
    public DeviceMessageParserTest(final String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        parser = new DeviceMessageParser();
        utcFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Tests parsing.
     * @throws IOException
     */
    public void testParse() throws IOException {
        final List<DataWithGsmInfo<DeviceMessage>> req;
        final Reader r = new InputStreamReader(DeviceMessageParserTest.class.getClassLoader()
                .getResourceAsStream("msg.txt"));
        try {
            req = parser.parse(r);
        } finally {
            r.close();
        }

        //test correct parsed

        //358688000000158|AUT|2013/10/18 13:28:29|
        //4023|-10.24|
        //460|1|9533|16114|34|
        //460|1|9533|16111|37|
        //460|1|9533|16904|31|
        //460|1|9533|16113|23|
        //460|1|9533|16142|21|
        //460|1|9533|16526|18|
        assertEquals(6, req.size());

        final DeviceMessage msg = req.get(2).getUserData();
        assertEquals("358688000000158", msg.getImei());
        assertEquals(DeviceMessageType.AUT, msg.getType());
        assertEquals("2013/10/18 13:28:29", utcFormat.format(msg.getTime()));
        assertEquals(4023, msg.getBattery());
        assertEquals("-10.24", Double.toString(msg.getTemperature()));

        //test station signals
        assertEquals(5, req.get(0).getGsmInfo().getStations().size());
        assertEquals(5, req.get(1).getGsmInfo().getStations().size());
        assertEquals(6, req.get(2).getGsmInfo().getStations().size());
        assertEquals(5, req.get(3).getGsmInfo().getStations().size());
        assertEquals(5, req.get(4).getGsmInfo().getStations().size());
        assertEquals(0, req.get(5).getGsmInfo().getStations().size());

        //test one station fully
        final StationSignal station = req.get(2).getGsmInfo().getStations().get(3);
        // 460 |  1  | 9533|16113|  23   |
        //<MCC>|<MNC>|<LAC>|<CI> |<RXLEV>|
        assertEquals(460, station.getMcc());
        assertEquals(1, station.getMnc());
        assertEquals(9533, station.getLac());
        assertEquals(16113, station.getCi());
        assertEquals(23, station.getLevel());
    }
}
