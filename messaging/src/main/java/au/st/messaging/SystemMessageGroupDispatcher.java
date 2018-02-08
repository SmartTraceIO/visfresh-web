/**
 *
 */
package au.st.messaging;

import java.util.Date;
import java.util.Set;

import au.st.messaging.dao.GroupLockDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageGroupDispatcher extends SystemMessageDispatcher {
    private static final String PAUSE_GROUP_FOR = "PAUSE_GROUP_FOR";
    private int numGroupsToLock = 10;
    private GroupLockDao groupLockDao;
    private long maxGroupLockTime = 3 * 60 * 1000l;

    /**
     * Default constructor.
     */
    public SystemMessageGroupDispatcher() {
        super();
    }

    /* (non-Javadoc)
     * @see au.st.messaging.SystemMessageDispatcher#checkInitialized()
     */
    @Override
    protected void checkInitialized() {
        super.checkInitialized();
        if (groupLockDao == null) {
            throw new RuntimeException("Group locking DAO is null");
        }
        if (getNumGroupsToLock() <= 0) {
            throw new RuntimeException("Number of groups to lock is not positive value " + getNumGroupsToLock());
        }
        if (getMaxGroupLockTime() <= 0) {
            throw new RuntimeException("Maximum group lock time is not positive value " + getMaxGroupLockTime());
        }
    }

    /* (non-Javadoc)
     * @see au.st.messaging.SystemMessageDispatcher#lockReadyMessages(java.lang.String, java.util.Set, java.util.Date, int)
     */
    @Override
    protected int lockReadyMessages(final String processor, final Set<String> types, final Date date, final int batch) {
        final Set<String> groups = getMessageDao().getReadyUnlockingGroups(new Date(), getNumGroupsToLock());
        for (final String g : groups) {
            if (groupLockDao.lockGroup(processor, g, new Date(System.currentTimeMillis() + getMaxGroupLockTime()))) {
                return getMessageDao().lockGroupMessages(processor, g, date, batch);
            }
        }

        return 0;
    }

    /* (non-Javadoc)
     * @see au.st.messaging.SystemMessageDispatcher#handleRetryableException(au.st.messaging.SystemMessage, au.st.messaging.SystemMessageException, au.st.messaging.ExecutionContext)
     */
    @Override
    protected void handleRetryableException(final SystemMessage m, final SystemMessageException e, final ExecutionContext context) {
        if (e.getRetryOn() != null && e instanceof SystemMessageGroupException) {
            final SystemMessageGroupException mge = (SystemMessageGroupException) e;
            if (mge.isShouldPauseGroup()) {
                context.setAttribute(PAUSE_GROUP_FOR, e.getRetryOn());
                groupLockDao.setUnlockTime(m.getGroup(), e.getRetryOn());
                return;
            }
        }

        super.handleRetryableException(m, e, context);
    }
    /* (non-Javadoc)
     * @see au.st.messaging.SystemMessageDispatcher#handle(au.st.messaging.MessageHandler, java.lang.String, au.st.messaging.ExecutionContext)
     */
    @Override
    protected <T> void handle(final MessageHandler<T> h, final String payload, final ExecutionContext context) throws Exception {
        if (context.getAttribute(PAUSE_GROUP_FOR) == null) {
            super.handle(h, payload, context);
        }
    }
    /* (non-Javadoc)
     * @see au.st.messaging.SystemMessageDispatcher#executeWorker()
     */
    @Override
    protected void executeWorker(final ExecutionContext context) {
        super.executeWorker(context);
        if (context.getAttribute(PAUSE_GROUP_FOR) == null) {
            groupLockDao.unlockGroups(getId());
        }
    }

    /**
     * @return the numGroupsToLock
     */
    public int getNumGroupsToLock() {
        return numGroupsToLock;
    }
    /**
     * @param numGroupsToLock the numGroupsToLock to set
     */
    public void setNumGroupsToLock(final int numGroupsToLock) {
        this.numGroupsToLock = numGroupsToLock;
    }
    /**
     * @return the groupLockDao
     */
    public GroupLockDao getGroupLockDao() {
        return groupLockDao;
    }
    /**
     * @param groupLockDao the groupLockDao to set
     */
    public void setGroupLockDao(final GroupLockDao groupLockDao) {
        this.groupLockDao = groupLockDao;
    }
    /**
     * @return max group locking time.
     */
    public long getMaxGroupLockTime() {
        return maxGroupLockTime;
    }
    /**
     * @param maxGroupLockTime the maxGroupLockTime to set
     */
    public void setMaxGroupLockTime(final long maxGroupLockTime) {
        this.maxGroupLockTime = maxGroupLockTime;
    }
}
