/**
 *
 */
package au.st.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import au.st.junit.TestWithTimeOut;
import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(TestWithTimeOut.class)
public class AbstractDispatcherTest extends AbstractDispatcher {
    private static final String DISPATCHER_THREAD_NAME = "Dispatcher Thread";

    private final List<Runnable> tasks = new LinkedList<>();

    private static class TaskWithTimeOut implements Runnable {
        private long timeOut;
        /**
         * @param timeOut time out.
         */
        public TaskWithTimeOut(final long timeOut) {
            super();
            this.timeOut = timeOut;
        }
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            final long t0 = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - t0 > timeOut) {
                    break;
                }
                Thread.yield();
            }
        }
    }

    /**
     * Default constructor.
     */
    public AbstractDispatcherTest() {
        super();
    }
    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#checkInitialized()
     */
    @Override
    public void checkInitialized() {
        super.checkInitialized();
    }

    @Before
    public void setUp() {
        setMainThreadTimeOut(100l);
        setCorePoolSize(3);
        setMaxPoolSize(10);
    }

    /**
     * Tests thread is really started.
     */
    @Test
    public void testStarted() {
        start();
        assertNotNull(getDispatcherThread());
    }

    /**
     * Tests threads really stopped.
     * @throws InterruptedException
     */
    @Test
    public void testStoped() throws InterruptedException {
        start();
        stop();

        Thread.sleep(3000l);
        assertNull(getDispatcherThread());
    }
    /**
     * Tests waits for finish of already executing tasks
     * @throws InterruptedException
     */
    @Test
    public void testWaitForStop() throws InterruptedException {
        final long t0 = System.currentTimeMillis();
        tasks.add(new TaskWithTimeOut(4000l));

        start();
        Thread.sleep(1000l);
        stop();
        Thread.sleep(1000l);

        assertTrue(System.currentTimeMillis() - t0 >= 5000l);
    }
    /**
     * Tests not processing new tasks after stop.
     * @throws InterruptedException
     */
    @Test
    public void testStoppedProcessingOnStop() throws InterruptedException {
        start();

        for (int i = 0; i < 1000; i++) {
            tasks.add(new TaskWithTimeOut(4000l));
        }

        Thread.sleep(2000l);
        stop();

        Thread.sleep(4000l);
        assertNull(getDispatcherThread());

        assertTrue(tasks.size() < 1000);
        assertTrue(tasks.size() > 0);
    }

    @Test
    public void testIgnoreIfNowFreeThreads() throws InterruptedException {
        start();
        setCorePoolSize(3);
        setMaxPoolSize(3);

        tasks.add(new TaskWithTimeOut(4000l));
        tasks.add(new TaskWithTimeOut(4000l));
        tasks.add(new TaskWithTimeOut(4000l));
        tasks.add(new TaskWithTimeOut(4000l));

        Thread.sleep(1000l);
        stop();

        Thread.sleep(4000l);
        assertNull(getDispatcherThread());

        assertEquals(2, tasks.size());
    }

    @Test
    public void testRunningInParallel() throws InterruptedException {
        start();
        setCorePoolSize(10);
        setMaxPoolSize(10);

        final long t0 = System.currentTimeMillis();

        tasks.add(new TaskWithTimeOut(4000l));
        tasks.add(new TaskWithTimeOut(4000l));
        tasks.add(new TaskWithTimeOut(4000l));
        tasks.add(new TaskWithTimeOut(4000l));

        Thread.sleep(1000l);
        stop();

        assertTrue(System.currentTimeMillis() - t0 < 8000l);
    }
    @Test
    public void testCheckCorrectInitalized() {
        AbstractDispatcherTest d = new AbstractDispatcherTest();
        d.setCorePoolSize(-1);
        try {
            d.checkInitialized();;
            throw new AssertionFailedError("Negative corre pool size");
        } catch (final Exception e) {
            //ok
        }

        d = new AbstractDispatcherTest();
        d.setKeepAliveTime(-1);
        try {
            d.checkInitialized();;
            throw new AssertionFailedError("Negative keep alive time out");
        } catch (final Exception e) {
            //ok
        }

        d = new AbstractDispatcherTest();
        d.setMainThreadTimeOut(0);
        try {
            d.checkInitialized();
            throw new AssertionFailedError("Main thread timeout should be more then 0");
        } catch (final Exception e) {
            //ok
        }

        d = new AbstractDispatcherTest();
        d.setMaxPoolSize(d.getCorePoolSize() - 1);
        try {
            d.checkInitialized();;
            throw new AssertionFailedError("incorrect core pool size");
        } catch (final Exception e) {
            //ok
        }
    }

    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#hasMessages()
     */
    @Override
    protected boolean hasDataToProcess() {
        synchronized (tasks) {
            return tasks.size() > 0;
        }
    }

    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#executeWorker(au.st.messaging.ExecutionContext)
     */
    @Override
    protected void executeWorker(final ExecutionContext context) {
        Runnable task = null;
        synchronized (tasks) {
            if (!tasks.isEmpty()) {
                task = tasks.remove(0);
            }
        }

        if (task != null){
            task.run();
        }
    }

    /**
     * @return
     */
    private Thread getDispatcherThread() {
        //search thread by name
        final Thread[] threads = new Thread[Thread.activeCount()];

        Thread.enumerate(threads);
        for (final Thread t : threads) {
            if (t == null) {
                break;
            }

            if (DISPATCHER_THREAD_NAME.equals(t.getName())) {
                return t;
            }
        }

        return null;
    }

    @After
    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#stop()
     */
    @Override
    public void stop() throws InterruptedException {
        super.stop();
    }
}
