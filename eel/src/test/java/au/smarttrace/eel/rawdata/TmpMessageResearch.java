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
                ("454c03e033d2035254407466497167671a002600013a22f58000000000000000000000000000"
                        + "0000000000000000000000000000000000000067671a002600035685c1800000003"
                        + "2000000230000002300000a6700000032000000230000002300000a6767671a0026"
                        + "00093a2447000000004a000000320000003400000f5f000000180000000f0000001"
                        + "0000004f867671a002600075b5277800000004a000000320000003400000f5f0000"
                        + "0000000000000000000000000000676712008200745b54006d0201f90001fffe07f"
                        + "1090c0f00880f920000000011c001dc0000324000000000ffffffff0002070171ed"
                        + "68da5efdf2ef0fc711ac898af1533ef9f2e80f0711c80484ea7af3d6f2d60faf115"
                        + "440eef1f5c5e0b1ce0d260fd00733e35535feb1c70d0f0fd0ddc94e34d7d5b1c50d"
                        + "200fc0ceb7222fb2e4b1c50d1c0fe0676712008200755b5401dd0201f90001fffe0"
                        + "7f1090c0f00880f920000000011c701dc0000351800000000ffffffff0001070171"
                        + "ed68da5efdf2ee0fc511b7898af1533ef9f2e80f0711cc0484ea7af3d6f2d60fac1"
                        + "17840eef1f5c5e0b1d20d260fe00733e35535feb1c70d130fd0ddc94e34d7d5b1c4"
                        + "0d220fc0ceb7222fb2e4b1c20d180fe0676712008200765b5403530201f90001fff"
                        + "e07f1090c0f00880f910000000011ca01dc000037e000000000ffffffff00010701"
                        + "71ed68da5efdf2ef0fc311bb898af1533ef9f2e80f0511d00484ea7af3d6f2d30fa"
                        + "c11990733e35535feb1c70d0f0fe0ddc94e34d7d5b1c50d200fc040eef1f5c5e0b1"
                        + "c30d250fe0ceb7222fb2e4b1c20d1a0fe0676712008200775b5404c50201f90001f"
                        + "ffe07f1090c0f00880f920000000011ce01db0000373000000000ffffffff000107"
                        + "0171ed68da5efdf2ee0fc311bb898af1533ef9f2e90f0711da0484ea7af3d6f2d40"
                        + "fab11b20733e35535feb1c70d100fe0ddc94e34d7d5b1c40d1d0fd040eef1f5c5e0"
                        + "b1c30d240fe0ceb7222fb2e4b1c20d1d0ff0676712008200785b5406380201f9000"
                        + "1fffe07f1090c0f00880f910000000011d901db0000319000000000ffffffff0002"
                        + "070171ed68da5efdf2ee0fc311c9898af1533ef9f2e80f0511e20484ea7af3d6f2d"
                        + "50fac11ab40eef1f5c5e0b1c80d250fe00733e35535feb1c70d0f0fe0ddc94e34d7"
                        + "d5b1c50d200fd0ceb7222fb2e4b1bf0d1d0ff0676712008200795b5407a70201f90"
                        + "001fffe07f1090c0f00880f920000000011d901da0000291800000000ffffffff00"
                        + "01070171ed68da5efdf2ef0fc311cd898af1533ef9f2e90f0611e20484ea7af3d6f"
                        + "2d60fac11ab0733e35535feb1c70d0c0fe040eef1f5c5e0b1c40d270fe0ddc94e34"
                        + "d7d5b1c40d200fd0ceb7222fb2e4b1c30d1d0ff0").toCharArray());
        final MessageParser parser = new MessageParser();
        final EelMessage msg = parser.parseMessage(bytes);
        for (final EelPackage p : msg.getPackages()) {
            final PackageIdentifier pid = p.getHeader().getPid();
            System.out.println("Pid: " + pid);
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
