/**
 *
 */
package au.st.messaging;

import org.junit.Test;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageDispatcherTest extends MockSystemMessageDispatcher {

    /**
     * Default constructor.
     */
    public SystemMessageDispatcherTest() {
        super();
    }

    @Test
    public void testProcessMessagesOnlySupportedTypes() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testRetryOnError() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testDeleteNotRetryable() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testDeleteOnSuccess() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testDispatch() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testHandle() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testLockMessageOnExecution() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testUnlockMessageAfterRetryableException() {
        throw new AssertionFailedError("TODO implement");
    }
}
