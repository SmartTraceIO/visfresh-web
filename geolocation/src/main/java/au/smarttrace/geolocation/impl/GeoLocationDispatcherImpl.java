/**
 *
 */
package au.smarttrace.geolocation.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import au.smarttrace.geolocation.GeoLocationDispatcher;
import au.smarttrace.geolocation.GeoLocationService;
import au.smarttrace.geolocation.GeoLocationServiceException;
import au.smarttrace.geolocation.RequestStatus;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.geolocation.impl.dao.RetryableEventDao;

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
        private final String namePrefix = "geolocation-thread-";

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
    protected RetryableEventDao dao;
    /**
     * Thread for asynchronous launch
     */
    private final Map<ServiceType, GeoLocationService> services = new HashMap<>();
    private final AtomicBoolean isStoped = new AtomicBoolean(false);
    private ExecutorService executor;

    /**
     * Default constructor..
     */
    protected GeoLocationDispatcherImpl() {
        super();
    }

    @Autowired
    public GeoLocationDispatcherImpl(final Environment env) {
        super();

        setBatchLimit(Integer.parseInt(env.getProperty("dispatcher.batchLimit", "10")));
        setInactiveTimeOut(Integer.parseInt(env.getProperty("dispatcher.inactiveTimeOut", "15000")));
        setRetryLimit(Integer.parseInt(env.getProperty("dispatcher.retryLimit", "7")));
        setRetryTimeOut(Integer.parseInt(env.getProperty("dispatcher.retryTimeOut", "300000")));
    }

    @PostConstruct
    public void start() {
        this.executor = Executors.newCachedThreadPool(threadFactory);
        startDispatcherThread();
    }

    /**
     * Starts dispatcher thread.
     */
    protected void startDispatcherThread() {
        new Thread("geo-locatino-dispatcher") {
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
                log.warn("Geo location dispatcher is interrupted");
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
        Set<ServiceType> types;
        synchronized (services) {
            types = new HashSet<>(services.keySet());
        }

        if (types.isEmpty()) {
            log.debug("Not services registered to process requests.");
            return 0;
        }

        final List<RetryableEvent> msgs = getRetryableEventsForProcess(types);

        if (msgs.size() > 0) {
            final List<Callable<String>> tasks = new LinkedList<>();
            for (final RetryableEvent r : msgs) {
                tasks.add(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return processRequest(r.getRequest().getType(), r.getRequest().getBuffer());
                    }
                });
            }

            try {
                final List<Future<String>> results = executor.invokeAll(tasks);
                final Iterator<RetryableEvent> geoIter = msgs.iterator();
                final Iterator<Future<String>> resIter = results.iterator();

                while (resIter.hasNext()) {
                    final RetryableEvent r = geoIter.next();
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
            log.debug("Process geo location request " + req);
            return s.requestLocation(req);
        } else {
            return null;
        }
    }

    /**
     * @return
     */
    protected List<RetryableEvent> getRetryableEventsForProcess(final Set<ServiceType> types) {
        final List<RetryableEvent> rs = dao.getRetryableEventsForProcess(new Date(), types);
        log.debug(rs.size() + " request selected to process");
        return rs;
    }
    /**
     * @param r
     * @param response
     */
    private void handleSuccess(final RetryableEvent r, final String response) {
        r.getRequest().setStatus(RequestStatus.success);
        r.getRequest().setBuffer(response);
        dao.save(r);
    }
    /**
     * @param msg the message.
     * @param e the exception.
     */
    protected void handleError(final RetryableEvent msg, final Throwable e) {
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
    private void stopProcessingByError(final RetryableEvent msg, final Throwable e) {
        dao.saveStatus(msg, RequestStatus.error);
    }
    /**
     * @param msg
     */
    protected void saveForRetry(final RetryableEvent msg) {
        dao.updateRetryValues(msg);
    }

    /**
     * @param msg the message.
     */
    protected void handleSuccess(final RetryableEvent msg) {
        log.debug("The request " + msg.getId() + " successfully processed");
    }

    @PreDestroy
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
