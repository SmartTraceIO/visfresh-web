/**
 *
 */
package au.st.messaging;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import au.st.messaging.ExecutionContext.LoadLevel;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractDispatcher {
    private ThreadPoolExecutor executor;
    private int corePoolSize = 0;
    private int maxPoolSize = 10;
    private long keepAliveTime = 15000l;
    private long mainThreadTimeOut = 1000l;
    private DispatcherContext context;
    private AtomicInteger numThreads = new AtomicInteger();

    /**
     * Default constructor.
     */
    public AbstractDispatcher() {
        super();
    }

    /**
     * Starts dispatcher.
     */
    public void start() {
        context = createContext();
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        //start main thread
        final Thread t = new Thread(() -> runDispatcherThread(), "Dispatcher Thread");
        t.start();
    }

    /**
     * @return dispatcher context.
     */
    protected DispatcherContext createContext() {
        return new DispatcherContext();
    }

    /**
     * Stops dispatcher.
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        context.setStopped(true);
        executor.shutdown();

        //wait for finishing all workers
        while (numThreads.get() > 0) {
            Thread.sleep(500l);
        }
    }

    private void runDispatcherThread() {
        while (!context.isStopped()) {
            doDispatch();
        }
    }
    /**
     *
     */
    protected void doDispatch() {
        if (hasMessagesInternal()) {
            if (numThreads.incrementAndGet() <= maxPoolSize) {
                //launch executor
                executor.execute(()-> {
                    try {
                        executeWorker(new ExecutionContextImpl(context));
                    } finally {
                        numThreads.decrementAndGet();
                    }
                });
            } else {
                numThreads.decrementAndGet();
            }
        } else {
            try {
                Thread.sleep(mainThreadTimeOut);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return true if has messages.
     */
    private boolean hasMessagesInternal() {
        if (context.getCurrentLoadLevel() != LoadLevel.Nothing) {
            return true;
        }
        return hasMessages();
    }

    /**
     * @return true if has messags.
     */
    protected abstract boolean hasMessages();
    /**
     * @param context TODO
     *
     */
    protected abstract void executeWorker(ExecutionContext context);

    /**
     * @return the corePoolSize
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }
    /**
     * @param corePoolSize the corePoolSize to set
     */
    public void setCorePoolSize(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }
    /**
     * @return the maxPoolSize
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    /**
     * @param maxPoolSize the maxPoolSize to set
     */
    public void setMaxPoolSize(final int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    /**
     * @return the keepAliveTime
     */
    public long getKeepAliveTime() {
        return keepAliveTime;
    }
    /**
     * @param keepAliveTime the keepAliveTime to set
     */
    public void setKeepAliveTime(final long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
    /**
     * @return the mainThreadTimeOut
     */
    public long getMainThreadTimeOut() {
        return mainThreadTimeOut;
    }
    /**
     * @param mainThreadTimeOut the mainThreadTimeOut to set
     */
    public void setMainThreadTimeOut(final long mainThreadTimeOut) {
        this.mainThreadTimeOut = mainThreadTimeOut;
    }
}
