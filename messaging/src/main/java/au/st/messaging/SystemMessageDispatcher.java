/**
 *
 */
package au.st.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import au.st.messaging.ExecutionContext.LoadLevel;
import au.st.messaging.dao.DispatcherIdDao;
import au.st.messaging.dao.SystemMessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageDispatcher extends AbstractDispatcher {
    private SystemMessageDao messageDao;
    private DispatcherIdDao dispatcherIdDao;
    private Map<String, MessageHandler<?>> handlers = new ConcurrentHashMap<>();
    private String id;
    private int batchSize = 30;
    private long updateActivityTimeOut = 60000l;
    private long lastActivityUpdate;
    private long maxInactiveTime = 15 * 60 * 1000l;
    private ObjectMapper parser;

    /**
     * Default constructor.
     */
    public SystemMessageDispatcher() {
        super();
    }

    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#start()
     */
    @Override
    public void start() {
        parser = createDefaultMapper();
        id = generateId();

        super.start();
    }
    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#stop()
     */
    @Override
    public void stop() throws InterruptedException {
        super.stop();
        dispatcherIdDao.delete(id);
    }

    /**
     * @return generated unique dispatcher ID.
     */
    protected String generateId() {
        String newId;
        do {
            newId = "d-v2.0.0-" + Long.toHexString(System.currentTimeMillis());
            if (dispatcherIdDao.saveHandlerId(newId, new Date(System.currentTimeMillis() + getMaxInactiveTime()))) {
                break;
            }

            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);

        return newId;
    }

    /**
     * Check dispatcher is correct initialized.
     */
    @Override
    protected void checkInitialized() {
        super.checkInitialized();

        if (messageDao == null) {
            throw new RuntimeException("SystemMessage DAO should not be null");
        }
        if (parser == null) {
            throw new RuntimeException("JSON parser should not be null");
        }
        if (getBatchSize() <= 0) {
            throw new RuntimeException("Batch size is not positive value " + getBatchSize());
        }
        if (getUpdateActivityTimeOut() <= 0) {
            throw new RuntimeException("Update inactivity time out is not positive value " + getUpdateActivityTimeOut());
        }
        if (getMaxInactiveTime() <= 0) {
            throw new RuntimeException("Max inactive time is not positive value " + getMaxInactiveTime());
        }
    }

    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#hasMessages()
     */
    @Override
    protected boolean hasDataToProcess() {
        if (handlers.size() == 0) {
            return false;
        }
        return messageDao.hasMessages(getListenTypes(), new Date());
    }
    /**
     * @return
     */
    private Set<String> getListenTypes() {
        return new HashSet<>(handlers.keySet());
    }

    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#doDispatch()
     */
    @Override
    protected void doOneIteration() {
        notifyDispatcherActive();
        super.doOneIteration();
    }
    /**
     *
     */
    private void notifyDispatcherActive() {
        final long t = System.currentTimeMillis();
        if (t - lastActivityUpdate > getUpdateActivityTimeOut()) {
            dispatcherIdDao.updateExpiedTime(id, new Date(t + getMaxInactiveTime()));
            lastActivityUpdate = t;
        }
    }

    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#executeWorker()
     */
    @Override
    protected void executeWorker(final ExecutionContext context) {
        final int batch = getBatchSize();
        final Set<String> types = getListenTypes();

        final int numLocked = types.size() == 0 ?  0 : lockReadyMessages(id, types, new Date(), batch);

        if (numLocked > 0) {
            if (numLocked < batch) {
                context.logActivity(LoadLevel.Particularly);
            } else {
                context.logActivity(LoadLevel.Full);
            }

            final List<SystemMessage> messages = getMessages(id);
            for (final SystemMessage m : messages) {
                if (context.isStopped()) {
                    break;
                }
                handleMessage(m, context);
            }
        } else {
            context.logActivity(LoadLevel.Nothing);
        }
    }

    /**
     * @param m message.
     * @param context execution context.
     */
    protected void handleMessage(final SystemMessage m, final ExecutionContext context) {
        final MessageHandler<?> h = handlers.get(m.getType());
        if (h != null) {
            try {
                handle(h, m.getPayload(), context);
                handleSuccess(m);
            } catch (final SystemMessageException sme) {
                handleRetryableException(m, sme, context, h.getMaxNumberOfRetry());
            } catch (final Exception exc) {
                handleNotRetryableException(m, exc, context);
            }
        } else {
            //not touch it, only unlock for other handlers.
            messageDao.unlock(m);
        }
    }

    /**
     * @param m message.
     * @param exc exception.
     * @param context execution context.
     */
    protected void handleNotRetryableException(final SystemMessage m, final Exception exc, final ExecutionContext context) {
        messageDao.delete(m);
    }
    /**
     * @param m message.
     * @param e exception.
     * @param context execution context.
     * @param maxRetry max number of retry.
     */
    protected void handleRetryableException(final SystemMessage m, final SystemMessageException e,
            final ExecutionContext context, final int maxRetry) {
        if (canRetry(m, e, maxRetry)) {
            messageDao.unlockAndRetryOn(m, e.getRetryOn());
        } else {
            handleNotRetryableException(m, e, context);
        }
    }

    /**
     * @param m
     * @param e
     * @param maxRetry
     * @return
     */
    protected boolean canRetry(final SystemMessage m, final SystemMessageException e, final int maxRetry) {
        return e.getRetryOn() == null || m.getNumberOfRetry() < maxRetry;
    }
    /**
     * @param m message.
     */
    protected void handleSuccess(final SystemMessage m) {
        messageDao.delete(m);
    }
    /**
     * @param h
     * @param payload
     * @param context execution context.
     */
    protected <T> void handle(final MessageHandler<T> h, final String payload, final ExecutionContext context) throws Exception {
        final T value = parser.readValue(payload, h.getMessageClass());
        h.handle(value);
    }
    /**
     * @param dispatcher dispatcher ID.
     * @return list of messages locked by given dispatcher.
     */
    protected List<SystemMessage> getMessages(final String dispatcher) {
        return messageDao.getLockedMessages(dispatcher);
    }
    /**
     * @param processor dispatcher ID.
     * @param types message types.
     * @param date ready date.
     * @param batch max number of messages to lock.
     * @return number of locked messages.
     */
    protected int lockReadyMessages(final String processor, final Set<String> types, final Date date, final int batch) {
        return messageDao.lockReadyMessages(processor, types, new Date(), batch);
    }
    /**
     * Adds message handler.
     * @param h message handler.
     */
    public void addMessageHandler(final MessageHandler<?> h) {
        if (h != null) {
            synchronized (handlers) {
                if (getListenTypes().contains(h.getMessageType())) {
                    throw new RuntimeException("Message type '" + h.getMessageType() + "' already listen");
                }

                handlers.put(h.getMessageType(), h);
            }
        }
    }
    /**
     * Removes message handler.
     * @param h message handler.
     */
    public void removeMessageHandler(final MessageHandler<?> h) {
        if (h != null) {
            synchronized (handlers) {
                handlers.remove(h.getMessageType());
            }
        }
    }

    /**
     * @return batch size.
     */
    public int getBatchSize() {
        return batchSize;
    }
    /**
     * @param batchSize the batchSize to set
     */
    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }
    /**
     * @return the updateActivityTimeOut
     */
    public long getUpdateActivityTimeOut() {
        return updateActivityTimeOut;
    }
    /**
     * @param updateActivityTimeOut the updateActivityTimeOut to set
     */
    public void setUpdateActivityTimeOut(final long updateActivityTimeOut) {
        this.updateActivityTimeOut = updateActivityTimeOut;
    }
    /**
     * @return the maxInactiveTime
     */
    public long getMaxInactiveTime() {
        return maxInactiveTime;
    }
    /**
     * @param maxInactiveTime the maxInactiveTime to set
     */
    public void setMaxInactiveTime(final long maxInactiveTime) {
        this.maxInactiveTime = maxInactiveTime;
    }
    /**
     * @param messageDao the messageDao to set
     */
    protected void setMessageDao(final SystemMessageDao messageDao) {
        this.messageDao = messageDao;
    }
    /**
     * @return the messageDao
     */
    public SystemMessageDao getMessageDao() {
        return messageDao;
    }
    /**
     * @param dispatcherIdDao the dispatcherIdDao to set
     */
    protected void setDispatcherIdDao(final DispatcherIdDao dispatcherIdDao) {
        this.dispatcherIdDao = dispatcherIdDao;
    }
    /**
     * @return the dispatcherIdDao
     */
    public DispatcherIdDao getDispatcherIdDao() {
        return dispatcherIdDao;
    }
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @return
     */
    protected ObjectMapper createDefaultMapper() {
        final ObjectMapper m = new ObjectMapper();
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        m.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        m.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        m.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        return m;
    }
}
