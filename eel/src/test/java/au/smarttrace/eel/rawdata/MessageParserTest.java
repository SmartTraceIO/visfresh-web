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
        final byte[] bytes = Hex.decodeHex(("454c04070c56035254407466498967671a002600015685c1800"
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
        final MessageParser parser = new MessageParser();
        final EelMessage msg = parser.parseMessage(bytes);
        assertNotNull(msg);
    }
//    @Test
//    public void testParseMessage() throws DecoderException {
//        final byte[] bytes = Hex.decodeHex(
//                ("454c008b8bc9035254407466496367671a002600015b469a0000000007"
//                + "00000002000000040000016d0000000700000002000000040000016d6"
//                + "7671a002600015b47eb800000000700000002000000040000016d0000"
//                + "000000000000000000000000000067671a002600035685c1800000002"
//                + "40000001600000019000007720000001d000000130000001400000604").toCharArray());
//        final MessageParser parser = new MessageParser();
//        final EelMessage msg = parser.parseMessage(bytes);
//        for (final EelPackage p : msg.getPackages()) {
//            System.out.println("PID: " + p.getHeader().getPid() + " (" + p.getHeader().getPidOriginValue() + ")");
//        }
//        assertNotNull(msg);
//    }
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
