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

import org.apache.commons.io.IOUtils;

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
                int b;
                while ((b = in.read()) > -1) {
                    System.out.print("" + ((char) b));
                    System.out.flush();
                }
            }
        } finally {
            s.close();
        }
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
    /**
     * @return
     * @throws IOException
     */
    private static byte[] readTestMessage() throws IOException {
        return decode(IOUtils.toString(TestClient.class.getResource("msg.txt")).split(" +"));
    }

    public static void main(final String[] args) throws IOException {
        final TestClient client = new TestClient("gateway.gotracking.net", 54929);

        final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
        String line;
        while((line = lnr.readLine()) != null) {
            final String trimed = line.trim();
            if ("stop".equalsIgnoreCase(trimed)) {
                break;
            }

            client.send(readTestMessage());
        }
    }
}
