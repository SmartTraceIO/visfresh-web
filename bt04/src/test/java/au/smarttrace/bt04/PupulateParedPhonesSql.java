/**
 *
 */
package au.smarttrace.bt04;

import java.io.PrintStream;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class PupulateParedPhonesSql {
    /**
     * Default constructor.
     */
    private PupulateParedPhonesSql() {
        super();
    }

    public static void main(final String[] args) {
        final PrintStream out = System.out;

        printOnePair(out, "358748083343675", "bt04-11181962x");
    }

    /**
     * @param out
     * @param gateway
     * @param beacon
     */
    private static void printOnePair(final PrintStream out, final String gateway, final String beacon) {
        final String sql = "insert into pairedphones (active, beaconid, imei, company, description)"
                + " values(true, '" + beacon + "', '" + gateway + "', 3, 'Primo as of 12Apr2018');";
        System.out.println(sql);
    }
}
