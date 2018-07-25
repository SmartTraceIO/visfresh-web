/**
 *
 */
package au.smarttrace.eel.rawdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import au.smarttrace.eel.IncorrectPacketLengthException;
import au.smarttrace.eel.rawdata.InstructionPackageBody.InstructionType;
import au.smarttrace.eel.rawdata.PackageHeader.PackageIdentifier;
import au.smarttrace.eel.rawdata.WarningPackageBody.WarningType;

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
        final PackageBody p = readPackage(bytes);

        assertTrue(p instanceof LoginPackageBody);
    }
    @Test
    public void testParseHeartbeatPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex("676703000400070188".toCharArray());
        final HeartbeatPackageBody p = (HeartbeatPackageBody) readPackage(bytes);

        final Status status = p.getStatus();
        assertNotNull(status);
    }
    @Test
    public void testParseLocationPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("676712004600055B399AFE0201"
                + "CC00002495000014203F01880EC00000000000000000000000000000000000000000000002"
                + "0198D09B7241CAB1D80C491BB051EF4B0A2AD2B1CC0D4F1B80").toCharArray());
        final LocationPackageBody p = (LocationPackageBody) readPackage(bytes);
        assertEquals(2, p.getBeacons().size());
        assertEquals(1, p.getLocation().getTowerSignals().size());
    }
    @Test
    public void testParseWarningPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("6767140024000A590BD54903026B940D0C3952AD0021"
                + "000400000501CC0001A53F0170F0AB19020789").toCharArray());
        final WarningPackageBody p = (WarningPackageBody) readPackage(bytes);
        assertEquals(1, p.getLocation().getTowerSignals().size());
        assertNotNull(p.getLocation().getGpsData());
        assertEquals(WarningType.Sos, p.getWarningType());
    }
    @Test
    public void testParseMessagePackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("6767160039000D590BD5AF03026B940D0C3952AD00"
                + "21000000000501CC0001A53F0170F0AB173230313835363632323132353"
                + "00000000000000000313233").toCharArray());
        final MessagePackageBody p = (MessagePackageBody) readPackage(bytes);
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
        final ParamSetPackageBody p = (ParamSetPackageBody) readPackage(bytes);
        assertNotNull(p.getData());
    }
    @Test
    public void testParseInstructionPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("676780000F5788014C754C7576657273696F6E23").toCharArray());

        final ReadBuffer buff = new ReadBuffer(bytes);
        final PackageHeader header = readPackageHeader(buff);
        final ReadBuffer pckgBuff = buff.readToNewBuffer(header.getSize());

        final InstructionPackageBody p = parseInstructionPackage(pckgBuff);
        assertNotNull(p.getUid());
        assertEquals(InstructionType.DeviceCommand, p.getType());
        assertEquals("version#", p.getInstruction());
    }
    //@Test there is not an example
    public void testParseBroadcastPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("").toCharArray());
        final BroadcastPackageBody p = (BroadcastPackageBody) readPackage(bytes);
        assertNotNull(p);
    }
    @Test
    public void testParseRealRawLoginMessage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("454c0027f339035254407466497"
                + "167670100180001035254407466497101280207120700010432005bd8eb").toCharArray());
        final MessageParser parser = new MessageParser();
        final EelMessage msg = parser.parseMessage(bytes);
        assertNotNull(msg);
    }
    @Test
    public void testMessageWithUndefinedPackage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("454c03e0e062035254407466497167671a002600013a22f5800000"
                + "00000000000000000000000000000000000000000000000000000000000067671a002600035685"
                + "c18000000032000000230000002300000a6700000032000000230000002300000a6767671a0026"
                + "00093a2447000000004a000000320000003400000f5f000000180000000f00000010000004f867"
                + "671a002600075b5277800000004a000000320000003400000f5f00000000000000000000000000"
                + "000000676712008200c85b5479d60201f90001fffe07f1090d0f00880f77000000001594020300"
                + "000dd800000000ffffffff00010701898af1533ef9f2ef0ee615ac71ed68da5efdf2e80fa715bb"
                + "0484ea7af3d6f2d20f7c15cd40eef1f5c5e0b1ca0d261200ceb7222fb2e4b1c70d231220ddc94e"
                + "34d7d5b1c30d2011f00733e35535feb1bc0d171210676712008200c95b547b490201f90001fffe"
                + "07f1090d0f00880f7c00000000159c020400000dd000000000ffffffff00010701898af1533ef9"
                + "f2ef0ee715ac71ed68da5efdf2ea0fa715bb0484ea7af3d6f2cc0f7b15e740eef1f5c5e0b1cb0d"
                + "2e1200ddc94e34d7d5b1c50d2011f0ceb7222fb2e4b1be0d2712200733e35535feb1ba0d181210"
                + "676712008200ca5b547cb60201f90001fffe07f1090d0f00880f7900000000159f020300000da0"
                + "00000000ffffffff00010701898af1533ef9f2ef0ee515b471ed68da5efdf2ea0fa615c20484ea"
                + "7af3d6f2cb0f741615ceb7222fb2e4b1c60d23123040eef1f5c5e0b1c40d2b1210ddc94e34d7d5"
                + "b1c30d2012000733e35535feb1b30d181220676712008200cb5b547e250201f90001fffe07f109"
                + "0d0f00880f7a0000000015a3020400000d6800000000ffffffff00010701898af1533ef9f2ef0e"
                + "e515b771ed68da5efdf2ea0fa615be0484ea7af3d6f2ca0f7b163540eef1f5c5e0b1ca0d281210"
                + "ceb7222fb2e4b1c50d2212300733e35535feb1be0d181220ddc94e34d7d5b1bc0d1f1200676712"
                + "008200cc5b547f8e0201f90001fffe07f1090d0f00880f770000000015ae020300000d70000000"
                + "00ffffffff00010701898af1533ef9f2ef0ee215bb71ed68da5efdf2ea0fa515c60484ea7af3d6"
                + "f2d20f7b163d40eef1f5c5e0b1cb0d291210ceb7222fb2e4b1c70d241230ddc94e34d7d5b1c30d"
                + "2312000733e35535feb1b60d181230676712008200cd5b5481020201f90001fffe07f1090d0f00"
                + "880f770000000015b1020300000d9000000000ffffffff00010701898af1533ef9f2ef0ee415be"
                + "71ed68da5efdf2ea0fa315c90484ea7af3d6f2d30f7a1643ceb7222fb2e4b1c60d25124040eef1"
                + "f5c5e0b1c40d261220ddc94e34d7d5b1c30d2112100733e35535feb1be0d191230").toCharArray());
        final MessageParser parser = new MessageParser();
        final EelMessage msg = parser.parseMessage(bytes);
        assertNotNull(msg);
    }
    @Test
    public void testCheckSum() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("454c0027f339035254407466497"
                + "167670100180001035254407466497101280207120700010432005bd8eb").toCharArray());

        //header without IMEI
        final byte[] header = new byte[6];
        //body with IMEI
        final byte[] body = new byte[bytes.length - header.length];

        System.arraycopy(bytes, 0, header, 0, header.length);
        System.arraycopy(bytes, header.length, body, 0, body.length);

        //get origin checksum
        final ReadBuffer headerBuffer = new ReadBuffer(header);
        headerBuffer.readTwo();
        headerBuffer.readTwo();
        final int originChecksum = headerBuffer.readTwo();

        final int checkSum = MessageWriter.calculateCheckSum(body);
        assertEquals(originChecksum, checkSum);
    }
    @Test
    public void testLoginMessageEqualsByOrigin() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(("454c0027f339035254407466497"
                + "167670100180001035254407466497101280207120700010432005bd8eb").toCharArray());
        final EelMessage msg = new MessageParser().parseMessage(bytes);
        final byte[] bytesNew = new MessageWriter().writeMessage(msg);

        assertTrue(Arrays.equals(bytes, bytesNew));
    }
    @Test
    public void testReadFullMessage() {
        final String mark = "6868";
        final String imei = "2390870987987";
        final int sequence = 17;

        final EelMessage msg = new EelMessage();
        msg.setMark(mark);
        msg.setImei(imei);

        //add one package for test
        final EelPackage pack = new EelPackage();
        msg.getPackages().add(pack);

        final HeartbeatPackageBody body = new HeartbeatPackageBody();
        body.setStatus(new Status(1));
        pack.setBody(body);

        final PackageHeader h = new PackageHeader();
        h.setMark(mark);
        h.setPid(PackageIdentifier.Heartbeat);
        h.setSequence(sequence);
        pack.setHeader(h);

        final byte[] bytes = new MessageWriter().writeMessage(msg);
        final EelMessage m = new MessageParser().parseMessage(bytes);

        //check message
        assertEquals(mark, m.getMark());
        assertEquals(imei, m.getImei());

        final EelPackage p = m.getPackages().get(0);
        //check header
        assertEquals(mark, p.getHeader().getMark());
        assertEquals(PackageIdentifier.Heartbeat, p.getHeader().getPid());
        assertEquals(sequence, p.getHeader().getSequence());

        //check body
        final HeartbeatPackageBody b = (HeartbeatPackageBody) p.getBody();
        assertTrue(b.getStatus().isGpsFixed());
    }
    @Test
    public void testReadMessageFromStream() throws IOException, IncorrectPacketLengthException {
        final EelMessage msg = new EelMessage();
        msg.setMark("6767");
        msg.setImei("2390870987987");

        //add one package for test
        final EelPackage pack = new EelPackage();
        msg.getPackages().add(pack);

        final HeartbeatPackageBody body = new HeartbeatPackageBody();
        body.setStatus(new Status(1));
        pack.setBody(body);

        final PackageHeader h = new PackageHeader();
        h.setMark("6767");
        h.setPid(PackageIdentifier.Heartbeat);
        h.setSequence(17);
        pack.setHeader(h);

        final byte[] bytes = new MessageWriter().writeMessage(msg);
        final MessageParser parser = new MessageParser();
        final byte[] received = parser.readMessageData(new ByteArrayInputStream(bytes));
        final EelMessage m = parser.parseMessage(received);

        //check message
        assertNotNull(m);
    }
    /**
     * @param bytes
     * @return
     */
    private PackageBody readPackage(final byte[] bytes) {
        return readIncommingPackage(new ReadBuffer(bytes)).getBody();
    }
}
