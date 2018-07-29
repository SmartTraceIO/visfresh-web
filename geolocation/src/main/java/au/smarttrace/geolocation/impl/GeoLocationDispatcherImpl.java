/**
 *
 */
package au.smarttrace.geolocation.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import au.smarttrace.geolocation.GeoLocationDispatcher;
import au.smarttrace.geolocation.GeoLocationService;
import au.smarttrace.geolocation.GeoLocationServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class GeoLocationDispatcherImpl implements GeoLocationDispatcher {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(GeoLocationDispatcherImpl.class);

    private ThreadFactory threadFactory = new ThreadFactory() {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "eel-msg-thread-";

        {
            final SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(false);
            t.setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2);
            return t;
        }
    };

    /**
     * Processor ID.
     */
    private String processorId;
    /**
     * Select messages limit.
     */
    private int batchLimit;
    /**
     * Maximal inactivity time.
     */
    private long inactiveTimeOut;
    /**
     * The limit of message retrying.
     */
    private int retryLimit;
    /**
     * The time out before retry message.
     */
    private long retryTimeOut;

    /**
     * Message DAO.
     */
    @Autowired
    protected MessageDao dao;
    /**
     * Thread for asynchronous launch
     */
    private final Map<ServiceType, GeoLocationService> services = new HashMap<>();
    private final AtomicBoolean isStoped = new AtomicBoolean(false);
    private ExecutorService executor;

    /**
     * Default constructor..
     */
    public GeoLocationDispatcherImpl() {
        super();
    }

    @Autowired
    public GeoLocationDispatcherImpl(final Environment env) {
        super();

        setBatchLimit(Integer.parseInt(env.getProperty("dispatcher.batchLimit", "10")));
        setInactiveTimeOut(Integer.parseInt(env.getProperty("dispatcher.inactiveTimeOut", "15000")));
        setProcessorId(env.getProperty("dispatcher.processorId", "device-msg"));
        setRetryLimit(Integer.parseInt(env.getProperty("dispatcher.retryLimit", "7")));
        setRetryTimeOut(Integer.parseInt(env.getProperty("dispatcher.retryTimeOut", "300000")));
    }

    public void start() {
        this.executor = Executors.newCachedThreadPool(threadFactory);

        new Thread("processor-" + getProcessorId()) {
            /*
             * (non-Javadoc)
             *
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {
                runDispatcherCycle();
            }
        }.start();
    }

    /**
     *
     */
    protected void runDispatcherCycle() {
        while (!isStoped()) {
            try {
                final int numProcessed = processMessages();
                if (numProcessed == 0 && getInactiveTimeOut() > 0) {
                    synchronized (services) {
                        if (!isStoped()) {
                            services.wait(getInactiveTimeOut());
                        }
                    }
                }
            } catch (final InterruptedException e) {
                log.warn("Dispatcher " + getProcessorId() + " thread is interrupted");
                break;
            } catch (final Throwable e) {
                log.error("Global exception during dispatch of messaegs", e);
            }
        }
    }

    /**
     * @return the inactiveTimeOut
     */
    public long getInactiveTimeOut() {
        return inactiveTimeOut;
    }
    /**
     * @param inactiveTimeOut
     *            the inactiveTimeOut to set
     */
    public void setInactiveTimeOut(final long inactiveTimeOut) {
        this.inactiveTimeOut = inactiveTimeOut;
    }
    /**
     * @param id
     * @return
     */
    public String setProcessorId(final String id) {
        return this.processorId = id;
    }
    /**
     * @return the processorId
     */
    public String getProcessorId() {
        return processorId;
    }
    /**
     * @return the limit
     */
    public int getBatchLimit() {
        return batchLimit;
    }
    /**
     * @param limit
     *            the limit to set
     */
    public void setBatchLimit(final int limit) {
        this.batchLimit = limit;
    }
    /**
     * @return the retryLimit
     */
    public int getRetryLimit() {
        return retryLimit;
    }
    /**
     * @param retryLimit
     *            the retryLimit to set
     */
    public void setRetryLimit(final int retryLimit) {
        this.retryLimit = retryLimit;
    }
    /**
     * @return the retryTimeOut
     */
    public long getRetryTimeOut() {
        return retryTimeOut;
    }
    /**
     * @param retryTimeOut
     *            the retryTimeOut to set
     */
    public void setRetryTimeOut(final long retryTimeOut) {
        this.retryTimeOut = retryTimeOut;
    }

    /**
     * @return number of processed messages.
     */
    public int processMessages() {
        final List<GeoLocationRequest> msgs = getGeoLocationRequestsForProcess();

        if (msgs.size() > 0) {
            final List<Callable<String>> tasks = new LinkedList<>();
            for (final GeoLocationRequest r : msgs) {
                tasks.add(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return processRequest(r.getType(), r.getBuffer());
                    }
                });
            }

            try {
                final List<Future<String>> results = executor.invokeAll(tasks);
                final Iterator<GeoLocationRequest> geoIter = msgs.iterator();
                final Iterator<Future<String>> resIter = results.iterator();

                while (resIter.hasNext()) {
                    final GeoLocationRequest r = geoIter.next();
                    final Future<String> f = resIter.next();

                    try {
                        handleSuccess(r, f.get());
                    } catch (final ExecutionException execExc) {
                        handleError(r, execExc.getCause());
                    } catch (final Throwable e) {
                        handleError(r, e);
                    }
                }
            } catch (final InterruptedException e1) {
                log.error("Task execution has interrupted", e1);
            }
        }

        return msgs.size();
    }

    /**
     * @param r
     * @return
     * @throws GeoLocationServiceException
     */
    protected String processRequest(final ServiceType t, final String req) throws GeoLocationServiceException {
        GeoLocationService s;
        synchronized (services) {
            s = services.get(t);
        }

        if (s != null) {
            return s.requestLocation(req);
        } else {

            return null;
        }
    }

    /**
     * @return
     */
    protected List<GeoLocationRequest> getGeoLocationRequestsForProcess() {
        return dao.getGeoLocationRequestsForProcess(new Date());
    }
    /**
     * @param r
     * @param response
     */
    private void handleSuccess(final GeoLocationRequest r, final String response) {
        r.setStatus(RequestStatus.success);
        r.setBuffer(response);
        dao.save(r);
    }
    /**
     * @param msg the message.
     * @param e the exception.
     */
    protected void handleError(final GeoLocationRequest msg, final Throwable e) {
        if (e instanceof GeoLocationServiceException && ((GeoLocationServiceException) e).canRetry()) {
            final GeoLocationServiceException re = (GeoLocationServiceException) e;
            final int retryLimit = re.getNumberOfRetry() > -1 ? re.getNumberOfRetry() : getRetryLimit();

            if (msg.getNumberOfRetry() < retryLimit) {
                log.error("Retryable exception has occured for message " + msg
                        + ", will retry later", e);
                msg.setRetryOn(new Date(msg.getRetryOn().getTime() + getRetryTimeOut()));
                msg.setNumberOfRetry(msg.getNumberOfRetry() + 1);
                saveForRetry(msg);
            } else {
                log.error("Retry limit has exceed for message " + msg + ", will deleted", e);
                stopProcessingByError(msg, e);
            }
        } else {
            log.error("Not retryable exception has occured for message " + msg.getId()
                + ", will deleted", e);
            stopProcessingByError(msg, e);
        }
    }

    /**
     * @param msg
     * @param e
     */
    private void stopProcessingByError(final GeoLocationRequest msg, final Throwable e) {
        dao.saveStatus(msg, RequestStatus.error);
    }
    /**
     * @param msg
     */
    protected void saveForRetry(final GeoLocationRequest msg) {
        dao.saveForRetry(msg);
    }

    /**
     * @param msg the message.
     */
    protected void handleSuccess(final GeoLocationRequest msg) {
        log.debug("The request " + msg.getId() + " successfully processed by "
                + getProcessorId());
    }

    public void stop() {
        isStoped.set(true);

        synchronized (services) {
            services.notifyAll();
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            log.error("Wating of shoot down thread pool is failed", e);
        }

        log.debug("Dispatcher has stoped");
    }

    /**
     * @return
     */
    protected boolean isStoped() {
        return isStoped.get();
    }

    /* (non-Javadoc)
     * @see au.smarttrace.geolocation.GeoLocationDispatcher#addGeoLocationService(au.smarttrace.geolocation.GeoLocationService)
     */
    @Override
    public void setGeoLocationService(final ServiceType type, final GeoLocationService s) {
        synchronized (services) {
            if (s == null) {
                services.remove(type);
            } else {
                services.put(type, s);
            }
        }
    }
}
