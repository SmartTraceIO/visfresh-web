/**
 *
 */
package au.smarttrace.tt18;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TestClient {
    private final String host;
    private final int port;

    /**
     * Default constructor.
     */
    public TestClient() {
        this("smarttrace.com.au", 3232);
    }
    /**
     * @param host service host.
     * @param port service port.
     */
    public TestClient(final String host, final int port) {
        super();
        this.host = host;
        this.port = port;
    }

    /**
     * @param msgs
     * @throws IOException
     */
    private void send(final byte[]... msgs) throws IOException {
        final Socket  s = new Socket(host, port);
        try {
            final OutputStream out = s.getOutputStream();
            final InputStream in = s.getInputStream();

            for (final byte[] bytes : msgs) {
                out.write(bytes);
                out.flush();

                //read response
                System.out.println(readResponse(in));
            }
        } finally {
            s.close();
        }
    }
    /**
     * @param in
     * @return
     * @throws IOException
     */
    private String readResponse(final InputStream in) throws IOException {
        //Welcome to TZONE Gateway Server
        //@UTC,2018-02-09 19:31:32#Server UTC time:2018-02-09 19:31:32
        //@ACK,10#
        final String ack = "@ACK,";

        //read response
        final StringBuilder sb = new StringBuilder();
        boolean inEnd = false;
        int b;
        while ((b = in.read()) > -1) {
            sb.append((char) b);

            if (inEnd) {
                if (b == '#') {
                    break;
                }
            } else {
                final int len = sb.length();
                if (len > 4 && sb.substring(len - ack.length(), len).equals(ack)) {
                    inEnd = true;
                }
            }
        }

        return sb.toString();
    }
    /**
     * @param data the data.
     * @return decoded data
     */
    private static byte[] decode(final String[] data) {
        final byte[] bytes = new byte[data.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(data[i], 16);
        }
        return bytes;
    }

    public static void main(final String[] args) throws IOException {
        final String str = "54 5a 00 2f 24 24 04 03 01 0f 00 00 08 66 71 00 33 76 80 91 12 02 09 16 13 17 00 08 07 eb 52 78 05 05 00 03 00 09 aa 00 0e 37 01 9c 09 53 4d 00 8c 70 ae 0d 0a";
        final TestClient client = new TestClient("gateway.gotracking.net", 54929);

        final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
        String line;
        while((line = lnr.readLine()) != null) {
            final String trimed = line.trim();
            if ("stop".equalsIgnoreCase(trimed)) {
                break;
            }

            client.send(decode(str.split(" +")));
        }
    }
}
