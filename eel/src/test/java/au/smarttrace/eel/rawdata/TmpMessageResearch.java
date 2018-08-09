/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.io.IOException;
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
            int numIncorrect = 0;

            final MessageParser parser = new MessageParser();
            final EelMessage msg = parser.parseMessage(bytes);
            for (final EelPackage p : msg.getPackages()) {
                final PackageIdentifier pid = p.getHeader().getPid();
                System.out.println(pid);

                if (pid == PackageIdentifier.Location) {
                    boolean hasIncorrect = false;
                    final LocationPackageBody body = (LocationPackageBody) p.getBody();
                    final DevicePosition pos = body.getLocation();

                    if (pos.getGpsData() != null) {
//                        final GpsData gps = pos.getGpsData();
//                        System.out.println("GPS data: " + gps);
                    } else if (!pos.getTowerSignals().isEmpty()) {
                        for (final GsmStationSignal gsm : pos.getTowerSignals()) {
                            if (gsm.getCid() == 0 && gsm.getLac() == 0) {
                                hasIncorrect = true;
                            }
                        }
                    }

                    if (hasIncorrect) {
                        numIncorrect++;
                    }
                }
            }

            if (numIncorrect > 0) {
                System.out.println("--------------------------");
                System.out.println("Message from " + msg.getImei() + " has " + numIncorrect
                    + " packages with incorrect tower signal:");
                System.out.println(Hex.encodeHexString(bytes));
            } else {
                System.out.println("**** Is fully ok");
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
