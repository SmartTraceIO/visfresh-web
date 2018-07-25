/**
 *
 */
package au.smarttrace.eel.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.codec.binary.Hex;

import au.smarttrace.eel.IncorrectPacketLengthException;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.EelPackage;
import au.smarttrace.eel.rawdata.HeartbeatPackageBody;
import au.smarttrace.eel.rawdata.MessageParser;
import au.smarttrace.eel.rawdata.MessageWriter;
import au.smarttrace.eel.rawdata.PackageHeader;
import au.smarttrace.eel.rawdata.PackageHeader.PackageIdentifier;
import au.smarttrace.eel.rawdata.ReadBuffer;
import au.smarttrace.eel.rawdata.Status;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class EelServicePinger {
    /**
     *
     */
    private static final int PORT = 3434;
    private static final String HOST = "www.stf.smarttrace.com.au";

    /**
     * Default constructor.
     */
    private EelServicePinger() {
        super();
    }

    public static void main(final String[] args) {
        try {
//            sendServicePing();
            sendRawDataPing();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendServicePing() throws Exception {
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

        final DatagramSocket so = new DatagramSocket();
        final DatagramPacket packet = sendMessage(so, msg);
        final EelMessage resp = receiveResponse(so, packet);
        System.out.println(resp);
    }
    private static void sendRawDataPing() throws Exception {
        final byte[] data = Hex.decodeHex(("454c03e0e062035254407466497167671a002600013a22f5800000"
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
        final DatagramSocket so = new DatagramSocket();
        final DatagramPacket packet = sendMessage(so, data);
        final EelMessage resp = receiveResponse(so, packet);
        System.out.println(resp);
    }

    /**
     * @param socket
     * @return
     * @throws IOException
     * @throws IncorrectPacketLengthException
     */
    private static EelMessage receiveResponse(final DatagramSocket socket, final DatagramPacket request)
            throws IOException, IncorrectPacketLengthException {
        request.setData(new byte[1024]);
        socket.setSoTimeout(3000);
        socket.receive(request);

        final MessageParser parser = new MessageParser();
        final byte[] data = parser.readMessageData(new ByteArrayInputStream(request.getData()));
        System.out.println("Response:\n" + new String(Hex.encodeHexString(data)));

        final ReadBuffer buff = new ReadBuffer(data);
        final EelMessage msg = parser.readMessageWithoutPackages(buff);
        while (buff.hasData()) {
            final EelPackage pckg = new EelPackage();
            pckg.setHeader(parser.readPackageHeader(buff));
            pckg.setBody(parser.parseDefaultPackageResponseBody(buff));

            msg.getPackages().add(pckg);
        }

        System.out.println("Received packages for device "
                + msg.getImei()
                + ": " + msg.getPackages());

        if (buff.hasData()) {
            throw new RuntimeException("Unexpected additional data");
        }
        return msg;
    }

    /**
     * @param msg
     * @return
     */
    private static DatagramPacket sendMessage(final DatagramSocket so, final EelMessage msg) throws IOException {
        final MessageWriter w = new MessageWriter();
        final byte[] buff = w.writeMessage(msg);

        return sendMessage(so, buff);
    }

    /**
     * @param buff
     * @return
     * @throws UnknownHostException
     * @throws SocketException
     * @throws IOException
     */
    protected static DatagramPacket sendMessage(final DatagramSocket so, final byte[] buff)
            throws UnknownHostException, SocketException, IOException {
        System.out.println("Request:\n" + Hex.encodeHexString(buff));
        final DatagramPacket p = new DatagramPacket(buff, buff.length,
                InetAddress.getByName(HOST), PORT);
        so.send(p);
        return p;
    }
}
