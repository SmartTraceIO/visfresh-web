/**
 *
 */
package au.st.messaging;

import au.st.messaging.dao.DispatcherIdDao;
import au.st.messaging.dao.SystemMessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MockSystemMessageDispatcher extends SystemMessageDispatcher {
    /**
     * Default constructor.
     */
    public MockSystemMessageDispatcher() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#startThreads()
     */
    @Override
    protected void startThreads() {
        // not start
    }
    /* (non-Javadoc)
     * @see au.st.messaging.SystemMessageDispatcher#stop()
     */
    /* (non-Javadoc)
     * @see au.st.messaging.AbstractDispatcher#stopThreads()
     */
    @Override
    protected void stopThreads() throws InterruptedException {
        // not stop
    }
    /**
     * Execute context synchronously.
     */
    public void executeWorker() {
        while (!context.isStopped() && hasDataToProcess()) {
            executeWorker(new ExecutionContextImpl(context));
        }
    }
    /* (non-Javadoc)
     * @see au.st.messaging.SystemMessageDispatcher#setDispatcherIdDao(au.st.messaging.dao.DispatcherIdDao)
     */
    @Override
    public void setDispatcherIdDao(final DispatcherIdDao dispatcherIdDao) {
        super.setDispatcherIdDao(dispatcherIdDao);
    }
    /* (non-Javadoc)
     * @see au.st.messaging.SystemMessageDispatcher#setMessageDao(au.st.messaging.dao.SystemMessageDao)
     */
    @Override
    public void setMessageDao(final SystemMessageDao messageDao) {
        super.setMessageDao(messageDao);
    }
}
