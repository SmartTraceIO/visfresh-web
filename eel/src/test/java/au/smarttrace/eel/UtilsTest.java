/**
 *
 */
package au.smarttrace.eel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UtilsTest {

    /**
     * Default constructor.
     */
    public UtilsTest() {
        super();
    }

    @Test
    public void testReverceBytes() {
        assertEquals("gfedcba", revertBytes("abcdefg"));
        assertEquals("fedcba", revertBytes("abcdef"));
        assertEquals("", revertBytes(""));
    }

    /**
     * @param str
     * @return
     */
    private String revertBytes(final String str) {
        final byte[] bytes = str.getBytes();
        Utils.revertBytes(bytes);
        return new String(bytes);
    }
}
