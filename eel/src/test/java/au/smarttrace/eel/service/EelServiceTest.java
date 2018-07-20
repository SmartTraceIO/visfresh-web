/**
 *
 */
package au.smarttrace.eel.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
public class EelServiceTest {
    private EelService service;

    /**
     * Default constructor.
     */
    public EelServiceTest() {
        super();
    }

    @Before
    public void setUp() {
        int port;
        try {
            final ServerSocket so = new ServerSocket(0);
            port = so.getLocalPort();
            so.close();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to allocate port", e);
        }

        service = new EelService(port);
        service.start();
    }

    @Test
    public void testSendAndHandleMessage() throws IOException,
            InterruptedException, IncorrectPacketLengthException {
        final List<EelMessage> messages = new LinkedList<>();

        service.setImmediatelyResponder(new ImmediatelyResponderImpl());
        service.setHandler(new EelMessageHandler() {
            @Override
            public void handleMessage(final EelMessage msg) {
                synchronized (messages) {
                    messages.add(msg);
                    messages.notify();
                }
            }
        });

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

        EelMessage resp;
        final DatagramSocket socket;

        synchronized (messages) {
            socket = sendMessage(msg);
            messages.wait(5000);
        }
        resp = receiveResponse(socket);

        assertNotNull(resp);
        assertEquals(1, messages.size());

        //check message
        final EelMessage m = messages.get(0);
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
    public void testImmediatelyReceiveResponse() throws Exception {
        final List<EelMessage> messages = new LinkedList<>();

        service.setImmediatelyResponder(new ImmediatelyResponderImpl() {
            @Override
            public EelMessage respond(final EelMessage msg) {
                synchronized (messages) {
                    messages.add(msg);
                    messages.notify();
                }
                return super.respond(msg);
            }
        });

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

        EelMessage resp;
        final DatagramSocket socket;

        synchronized (messages) {
            socket = sendMessage(msg);
            messages.wait(5000);
        }
        resp = receiveResponse(socket);

        assertNotNull(resp);
        assertEquals(1, messages.size());

        //check message
        final EelMessage m = messages.get(0);
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

    /**
     * @param socket
     * @return
     * @throws IOException
     * @throws IncorrectPacketLengthException
     */
    private EelMessage receiveResponse(final DatagramSocket socket)
            throws IOException, IncorrectPacketLengthException {
        final DatagramPacket dtg = new DatagramPacket(new byte[1024], 1024,
                InetAddress.getLocalHost(), service.getPort());
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
    private DatagramSocket sendMessage(final EelMessage msg) throws IOException {
        final MessageWriter w = new MessageWriter();
        final byte[] buff = w.writeMessage(msg);

        final DatagramPacket p = new DatagramPacket(buff, buff.length,
                InetAddress.getLocalHost(), service.getPort());
        final DatagramSocket so = new DatagramSocket();
        so.send(p);
        return so;
    }

    @After
    public void tearDown() {
        service.stop();
    }
}
