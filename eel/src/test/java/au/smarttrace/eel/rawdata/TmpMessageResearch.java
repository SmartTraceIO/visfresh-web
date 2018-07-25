/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.util.Date;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

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

    public static void main(final String[] args) throws DecoderException {
        researchMessage();
    }

    /**
     * @throws DecoderException
     *
     */
    private static void researchMessage() throws DecoderException {
        final byte[] bytes = Hex.decodeHex(
                ("454c038498c3035254407466496367671a002600015b469a000000000700000002000000040000016d0000000700000002000000040000016d67671a002600015b47eb800000000700000002000000040000016d0000000000000000000000000000000067671a002600035685c180000000240000001600000019000007720000001d00000013000000140000060467671a002600033a244700000000330000001f0000002400000a8d0000000f000000090000000a0000031a67671a002600145b527780000000330000001f0000002400000a8d00000000000000000000000000000000676712008200ab5b544fd00201f90001fffe07f1090d0f00880fb100000000139c01e6000010a8000000000001000000020701898af1533ef9f2ee0ef1139de33a24b3cbfcf2e70f7e13a40484ea7af3d6f2d10f8c13c6ceb7222fb2e4b1ca0d1a10f0ddc94e34d7d5b1c30d1c10c00733e35535feb1c10d1010e040eef1f5c5e0b1bd0d2a10e0676712008200ac5b5451400201f90001fffe07f1090d0f00880fb10000000013b901e8000010b8000000000000000000020701898af1533ef9f2ef0ef013b7e33a24b3cbfcf2ea0f8013bd0484ea7af3d6f2d30f8b1404ceb7222fb2e4b1ca0d1f111040eef1f5c5e0b1c30d2510f00733e35535feb1c20d1110f0ddc94e34d7d5b1b90d1e10d0676712008200ad5b5452b20201f90001fffe07f1090d0f00880fb20000000013dc01e900001108000000000001000000020701898af1533ef9f2ef0ef113d3e33a24b3cbfcf2ea0f7e13da0484ea7af3d6f2ce0f8b1436ceb7222fb2e4b1c30d1d111040eef1f5c5e0b1c10d2511000733e35535feb1c00d131100ddc94e34d7d5b1bf0d1a10e0676712008200ae5b5454250201f90001fffe07f1090d0f00880fb00000000013fd01eb000010a8000000000000000000020701898af1533ef9f2ee0ef113ede33a24b3cbfcf2e70f7d13ef0484ea7af3d6f2d00f8b146cceb7222fb2e4b1cb0d1f112040eef1f5c5e0b1c30d2911100733e35535feb1bf0d141120ddc94e34d7d5b1bf0d1b10f0676712008200af5b5455990201f90001fffe07f1090d0f00880fb100000000142101ed000010d8000000000001000000020701898af1533ef9f2ef0ef11403e33a24b3cbfcf2e90f7d1409ceb7222fb2e4b1cb0d2211400484ea7af3d6f2ca0f8b14890733e35535feb1c40d12113040eef1f5c5e0b1c30d291120ddc94e34d7d5b1c10d211100").toCharArray());
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
                    System.out.println("Tower Signals:");
                    for (final GsmStationSignal gsm : pos.getTowerSignals()) {
                        System.out.println(gsm);
                    }
                }
            }
        }
    }
}
