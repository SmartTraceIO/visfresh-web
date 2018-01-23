/**
 *
 */
package au.smarttrace.tt18.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import au.smarttrace.tt18.junit.FastTest;
import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(FastTest.class)
public class Tt18SessionTest {
    private AccessibleTt18Session session;

    /**
     * Default constructor.
     */
    public Tt18SessionTest() {
        super();
    }

    @Before
    public void setUp() {
        session = new AccessibleTt18Session();
    }

    @Test
    public void testResponse() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testTwoMessages() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testCorruptedMessage() {
        throw new AssertionFailedError("TODO implement");
    }
}
