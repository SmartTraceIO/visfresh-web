/**
 *
 */
package au.smarttrace.eel.rawdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import au.smarttrace.eel.rawdata.InstructionPackage.InstructionType;
import au.smarttrace.eel.rawdata.WarningPackage.WarningType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageParserTest extends MessageParser {
    /**
     * Default constructor.
     */
    public MessageParserTest() {
        super();
    }

    @Test
    public void testParseLoginPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(
                "67670100180005035254407167747100200205020500010432000088BD".toCharArray());
        final AbstractPackage p = readPackage(bytes);

        assertTrue(p instanceof LoginPackage);
    }
    @Test
    public void testParseHeartbeatPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex("676703000400070188".toCharArray());
        final HeartbeatPackage p = (HeartbeatPackage) readPackage(bytes);

        final Status status = p.getStatus();
        assertNotNull(status);
    }
    @Test
    public void testParseLocationPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("676712004600055B399AFE0201"
                + "CC00002495000014203F01880EC00000000000000000000000000000000000000000000002"
                + "0198D09B7241CAB1D80C491BB051EF4B0A2AD2B1CC0D4F1B80").toCharArray());
        final LocationPackage p = (LocationPackage) readPackage(bytes);
        assertEquals(2, p.getBeacons().size());
        assertEquals(1, p.getLocation().getTowerSignals().size());
    }
    @Test
    public void testParseWarningPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("6767140024000A590BD54903026B940D0C3952AD0021"
                + "000400000501CC0001A53F0170F0AB19020789").toCharArray());
        final WarningPackage p = (WarningPackage) readPackage(bytes);
        assertEquals(1, p.getLocation().getTowerSignals().size());
        assertNotNull(p.getLocation().getGpsData());
        assertEquals(WarningType.Sos, p.getWarningType());
    }
    @Test
    public void testParseMessagePackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("6767160039000D590BD5AF03026B940D0C3952AD00"
                + "21000000000501CC0001A53F0170F0AB173230313835363632323132353"
                + "00000000000000000313233").toCharArray());
        final MessagePackage p = (MessagePackage) readPackage(bytes);
        assertNotNull(p.getPhoneNumber());
        assertEquals("123", p.getMessage());
        assertEquals(1, p.getLocation().getTowerSignals().size());
        assertNotNull(p.getLocation().getGpsData());
    }
    @Test
    public void testParseParamSetPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("67671B009E000500010432009266DF000008053FC0A"
                + "20341303EFE8110D414404C0680185610CEF3A23C8C18154005AB64300BD0AAA84575"
                + "5C0CE331CF0C1B036478B843D0EA288988320B42D068956405053C11A4588FA38803F"
                + "D599EC6EF4B7383D0FC3FB7333919EA637F3D8EFB1D79F9D27B8D7782191146AE344D"
                + "C0766F01599EE898BBE5ED3217444DBECA0AB4BADA4B08224A48F235D59759EDEB2A24"
                + "EE9C20").toCharArray());
        final ParamSetPackage p = (ParamSetPackage) readPackage(bytes);
        assertNotNull(p.getData());
    }
    @Test
    public void testParseInstructionPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("676780000F5788014C754C7576657273696F6E23").toCharArray());
        final InstructionPackage p = (InstructionPackage) readPackage(bytes);
        assertNotNull(p.getUid());
        assertEquals(InstructionType.DeviceCommand, p.getType());
        assertEquals("version#", p.getInstruction());
    }
    //@Test there is not an example
    public void testParseBroadcastPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("").toCharArray());
        final BroadcastPackage p = (BroadcastPackage) readPackage(bytes);
        assertNotNull(p);
    }
    /**
     * @param bytes
     * @return
     */
    private AbstractPackage readPackage(final byte[] bytes) {
        return readPackage(new ReadBuffer(bytes));
    }
}
