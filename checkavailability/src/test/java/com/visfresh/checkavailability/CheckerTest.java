/**
 *
 */
package com.visfresh.checkavailability;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CheckerTest extends Checker {
    private int numSendNotifications;
    private final List<IOException> exceptions = new LinkedList<>();
    private final List<Long> waits = new LinkedList<>();

    /**
     * Default constructor.
     */
    public CheckerTest() {
        super();
        setTimeOut(0);
    }

    /* (non-Javadoc)
     * @see com.visfresh.checkavailability.Checker#sendNotification()
     */
    @Override
    protected void sendNotification() {
        numSendNotifications++;
    }
    /* (non-Javadoc)
     * @see com.visfresh.checkavailability.Checker#sendCheckRequest()
     */
    @Override
    protected void sendCheckRequest() throws IOException {
        if (exceptions.size() > 0) {
            throw exceptions.remove(0);
        }
        if (waits.size() > 0) {
            try {
                Thread.sleep(waits.remove(0));
            } catch (final InterruptedException e) {
            }
        }
    }
    @Test
    public void testNotAvailable() {
        exceptions.add(new IOException("Not available"));
        exceptions.add(new IOException("Not available"));

        check();

        assertEquals(1, numSendNotifications);
    }
    @Test
    public void testFirstCheckHaveError() {
        exceptions.add(new IOException("Not available"));

        check();

        assertEquals(0, numSendNotifications);
    }
    @Test
    public void testNotAvailableByTimeOut() {
        setWaitForResponseTimeOut(1000);
        waits.add(1000000000l);
        waits.add(1000000000l);

        check();

        assertEquals(1, numSendNotifications);
    }
    @Test
    public void testFirstCheckTimedOut() {
        setWaitForResponseTimeOut(1000);
        waits.add(1000000000l);

        check();

        assertEquals(0, numSendNotifications);
    }
}
