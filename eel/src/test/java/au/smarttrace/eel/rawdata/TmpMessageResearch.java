/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import au.smarttrace.eel.rawdata.PackageHeader.PackageIdentifier;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class TmpMessageResearch {
    /**
     * Default constructor.
     */
    private TmpMessageResearch() {
        super();
    }

    public static void main(final String[] args) throws DecoderException, IOException {
        researchMessage();
    }

    /**
     * @throws DecoderException
     * @throws IOException
     *
     */
    private static void researchMessage() throws DecoderException, IOException {
        final byte[][] manyMessages = getData();
        for (final byte[] bytes : manyMessages) {
            System.out.println("----------------------------------------------------------");

            final MessageParser parser = new MessageParser();
            final EelMessage msg = parser.parseMessage(bytes);
            System.out.println("IMEI: " + msg.getImei());
            for (final EelPackage p : msg.getPackages()) {
                final PackageIdentifier pid = p.getHeader().getPid();
                System.out.println("Pid: " + p);
                if (pid == PackageIdentifier.Location) {
                    final LocationPackageBody body = (LocationPackageBody) p.getBody();
                    final DevicePosition pos = body.getLocation();
                    System.out.println("Date: " + new Date(pos.getTime() * 1000l));

                    if (pos.getGpsData() != null) {
                        final GpsData gps = pos.getGpsData();
                        System.out.println("GPS data: " + gps);
                    } else if (!pos.getTowerSignals().isEmpty()) {
                        final int size = pos.getTowerSignals().size();
                        System.out.println("Tower Signals ("
                                + size
                                + "):" + (size > 1 ? " *********************" : ""));
                        for (final GsmStationSignal gsm : pos.getTowerSignals()) {
                            System.out.println(gsm);
                        }
                    }
                }
            }
        }
    }

    /**
     * @return
     * @throws DecoderException
     */
    protected static byte[][] getData() throws IOException, DecoderException {
        final List<String> lines = IOUtils.readLines(TmpMessageResearch.class.getResourceAsStream("msgs.txt"));
        final byte[][] bytes = new byte[lines.size()][];
        int i = 0;
        for (final String line : lines) {
            bytes[i] = Hex.decodeHex(line.toCharArray());
            i++;
        }
        return bytes;
    }
}
