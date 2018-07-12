/**
 *
 */
package au.smarttrace.eel;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class IncorrectPacketLengthException extends Exception {
    private static final long serialVersionUID = 1375380569789970126L;

    private final int expected;
    private final byte[] actual;

    /**
     * @param expected expected data length.
     * @param actual actual data.
     */
    public IncorrectPacketLengthException(final int expected, final byte[] actual) {
        super();
        this.expected = expected;
        this.actual = actual;
    }
    /**
     * @return the actual
     */
    public byte[] getActual() {
        return actual;
    }
    /**
     * @return the expected
     */
    public int getExpected() {
        return expected;
    }
}
