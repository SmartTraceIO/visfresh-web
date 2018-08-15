/**
 *
 */
package au.smarttrace.tt18.st;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;
import au.smarttrace.geolocation.DeviceMessageType;
import au.smarttrace.tt18.MessageParser;
import au.smarttrace.tt18.MessageParserTest;
import au.smarttrace.tt18.RawMessage;
import au.smarttrace.tt18.junit.FastTest;
import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(FastTest.class)
public class SmartTraceRawMessageHandlerTest {
    private AccessibleSmartTraceRawMessageHandler handler;

    /**
     * Default constructor.
     */
    public SmartTraceRawMessageHandlerTest() {
        super();
    }

    @Before
    public void setUp() {
        handler = new AccessibleSmartTraceRawMessageHandler();
    }

    @Test
    public void testConvert() throws IOException {
        final RawMessage rawMessage = readTestMessage();

        final DataWithGsmInfo<DeviceMessage> info = handler.convert(rawMessage);
        assertNotNull(info);

        final DeviceMessage stMessage = info.getUserData();

        assertEquals(rawMessage.getBattery(), stMessage.getBattery());
        assertEquals(rawMessage.getImei(), stMessage.getImei());
        assertEquals(1, info.getGsmInfo().getStations().size());
        assertEquals(rawMessage.getTemperature().doubleValue(), stMessage.getTemperature(), 0.001);
        assertEqualsDates(rawMessage.getTime(), stMessage.getTime());
        assertEquals(DeviceMessageType.AUT, stMessage.getType());
    }

    /**
     * @param d1
     * @param d2
     */
    private void assertEqualsDates(final Date d1, final Date d2) {
        if (Math.abs(d1.getTime() - d2.getTime()) > 1000) {
            throw new AssertionFailedError("Not equals dates");
        }
    }
    /**
     * @return
     * @throws IOException
     */
    private RawMessage readTestMessage() throws IOException {
        final byte[] bytes = MessageParserTest.readTestMessage();

        final MessageParser parser = new MessageParser();
        return parser.parseMessage(bytes);
    }
}
