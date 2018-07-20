/**
 *
 */
package au.smarttrace.eel.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
    private static final String HOST = "smarttrace.com.au";

    /**
     * Default constructor.
     */
    private EelServicePinger() {
        super();
    }

    public static void main(final String[] args) {
        try {
            sendServicePiing();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendServicePiing() throws Exception {
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

        final DatagramSocket socket = sendMessage(msg);
        final EelMessage resp = receiveResponse(socket);
        System.out.println(resp);
    }

    /**
     * @param socket
     * @return
     * @throws IOException
     * @throws IncorrectPacketLengthException
     */
    private static EelMessage receiveResponse(final DatagramSocket socket)
            throws IOException, IncorrectPacketLengthException {
        final DatagramPacket dtg = new DatagramPacket(new byte[1024], 1024,
                InetAddress.getLocalHost(), PORT);
        socket.setSoTimeout(3000);
        socket.receive(dtg);

        final MessageParser parser = new MessageParser();
        final byte[] data = parser.readMessageData(new ByteArrayInputStream(dtg.getData()));

        final ReadBuffer buff = new ReadBuffer(data);
        final EelMessage msg = parser.readMessageWithoutPackages(buff);
        final EelPackage pckg = new EelPackage();
        msg.getPackages().add(pckg);

        pckg.setHeader(parser.readPackageHeader(buff));
        pckg.setBody(parser.parseDefaultPackageResponseBody(buff));

        if (buff.hasData()) {
            throw new RuntimeException("Unexpected additional data");
        }
        return msg;
    }

    /**
     * @param msg
     * @return
     */
    private static DatagramSocket sendMessage(final EelMessage msg) throws IOException {
        final MessageWriter w = new MessageWriter();
        final byte[] buff = w.writeMessage(msg);

        final DatagramPacket p = new DatagramPacket(buff, buff.length,
                InetAddress.getByName(HOST), PORT);
        final DatagramSocket so = new DatagramSocket();
        so.send(p);
        return so;
    }
}
