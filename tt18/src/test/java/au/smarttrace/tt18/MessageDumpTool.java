/**
 *
 */
package au.smarttrace.tt18;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class MessageDumpTool {
    /**
     * Default constructor.
     */
    private MessageDumpTool() {
        super();
    }

    public static void main(final String[] args) {
        final String str = "54 5a 00 2f 24 24 04 03 01 0f 00 00 08 67 79 30 32 03 72 61 09 01 05 0c 06 0b 00 08 07 eb 59 75 05 05 00 03 00 09 aa 10 18 37 01 8f 0b 00 38 00 12 4c 07 0d 0a";
        final byte[] msg = MessageParserTest.decodeMessage(str);

        final RawMessage m = new MessageParser().parseMessage(msg);
        System.out.println("IMEI: " + m.getImei());
        System.out.println("Cell ID: " + (m.getCellId()));
        System.out.println("Lac: " + (m.getLac()));
        System.out.println("Mmc: " + (m.getMcc()));
        System.out.println("Mcc: " + (m.getMnc()));
//        System.out.println(new String(msg));
//        System.out.println(m);
        System.out.println("Packet index: " + m.getPacketIndex());
    }
}
