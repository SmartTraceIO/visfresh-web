/**
 *
 */
package au.st.messaging;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
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
    private int corePoolSize = 10;
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
        checkInitialized();

        context = createContext();

        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        final AtomicInteger num = new AtomicInteger();
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable r) {
                    final Thread t = new Thread(group, r, "Dispatcher worker-" + num.incrementAndGet());
                    t.setDaemon(false);
                    return t;
                }
            });

        //start main thread
        final Thread t = new Thread(() -> runDispatcherThread(), "Dispatcher Thread");
        t.start();
    }
    /**
     * Check correct initialized.
     */
    protected void checkInitialized() {
        if (getCorePoolSize() < 1) {
            throw new RuntimeException("Invalid core pool size " + getCorePoolSize());
        }
        if (getMaxPoolSize() < getCorePoolSize()) {
            throw new RuntimeException("Invalid max pool size " + getMaxPoolSize());
        }
        if (getKeepAliveTime() < 0l) {
            throw new RuntimeException("Invalid keep alive time out " + getKeepAliveTime());
        }
        if (getMainThreadTimeOut() < 1l) {
            throw new RuntimeException("Invalid main thread time out " + getMainThreadTimeOut());
        }
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
        if (context == null || executor.isShutdown()) {
            //not started
            return;
        }
        context.setStopped(true);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }

    private void runDispatcherThread() {
        while (!context.isStopped()) {
            doOneIteration();
        }
    }
    /**
     *
     */
    protected void doOneIteration() {
        if (hasDataToProcessInternal()) {
            if (numThreads.incrementAndGet() < maxPoolSize) {
                //launch executor
                try {
                    executor.execute(()-> {
                        try {
                            if (!context.isStopped()) {
                                executeWorker(new ExecutionContextImpl(context));
                            }
                        } finally {
                            numThreads.decrementAndGet();
                        }
                    });
                } catch (final RuntimeException e) {
                    //can be rejected because stoped
                    numThreads.decrementAndGet();
                    if (!context.isStopped()) {
                        throw e;
                    }
                }
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
    private boolean hasDataToProcessInternal() {
        if (context.getCurrentLoadLevel() != LoadLevel.Nothing) {
            return true;
        }
        return hasDataToProcess();
    }

    /**
     * @return true if has data to process.
     */
    protected abstract boolean hasDataToProcess();
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
