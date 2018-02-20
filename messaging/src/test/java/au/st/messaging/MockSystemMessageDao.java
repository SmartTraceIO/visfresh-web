/**
 *
 */
package au.st.messaging;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import au.st.messaging.dao.SystemMessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MockSystemMessageDao implements SystemMessageDao {
    private final List<SystemMessage> messages;
    private final Map<Long, String> locks;

    /**
     * Default constructor.
     */
    public MockSystemMessageDao() {
        this(new LinkedList<>(), new HashMap<Long, String>());
    }
    /**
     * @param messages messages.
     * @param locks locks.
     */
    public MockSystemMessageDao(final List<SystemMessage> messages, final Map<Long, String> locks) {
        super();
        this.messages = messages;
        this.locks = locks;
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#hasMessages(java.util.Set)
     */
    @Override
    public synchronized boolean hasMessages(final Set<String> listenTypes, final Date date) {
        for (final SystemMessage m : messages) {
            if (!locks.containsKey(m.getId()) && listenTypes.contains(m.getType())
                    && !m.getRetryOn().after(date)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#lockReadyMessages(java.lang.String, java.util.Set, java.util.Date, int)
     */
    @Override
    public synchronized int lockReadyMessages(final String dispatcher, final Set<String> listenTypes, final Date date, final int batchSize) {
        int count = 0;
        for (final SystemMessage m : messages) {
            if (count >= batchSize) {
                break;
            }
            if (!locks.containsKey(m.getId()) && listenTypes.contains(m.getType())
                    && !m.getRetryOn().after(date)) {
                locks.put(m.getId(), dispatcher);

                count++;
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#getMessages(java.lang.String)
     */
    @Override
    public synchronized List<SystemMessage> getLockedMessages(final String dispatcher) {
        final List<SystemMessage> selected = new LinkedList<>();
        for (final SystemMessage m : messages) {
            if (dispatcher.equals(locks.get(m.getId()))) {
                selected.add(m);
            }
        }
        return selected;
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#unlock(au.st.messaging.SystemMessage)
     */
    @Override
    public synchronized void unlock(final SystemMessage m) {
        locks.remove(m.getId());
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#delete(au.st.messaging.SystemMessage)
     */
    @Override
    public synchronized void delete(final SystemMessage m) {
        locks.remove(m.getId());
        final Iterator<SystemMessage> iter = messages.iterator();
        while (iter.hasNext()) {
            if (iter.next().getId().equals(m.getId())) {
                iter.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#unlockAndRetryOn(au.st.messaging.SystemMessage, java.util.Date)
     */
    @Override
    public synchronized void unlockAndRetryOn(final SystemMessage m, final Date retryOn) {
        locks.remove(m.getId());
        final Iterator<SystemMessage> iter = messages.iterator();
        while (iter.hasNext()) {
            final SystemMessage next = iter.next();
            if (next.getId().equals(m.getId())) {
                next.setNumberOfRetry(m.getNumberOfRetry() + 1);
                next.setRetryOn(retryOn);
            }
        }
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#lockGroupMessages(java.lang.String, java.lang.String, java.util.Date, int)
     */
    @Override
    public synchronized int lockGroupMessages(final String processor, final String group, final Date readyOn, final int batch) {
        int count = 0;
        final Iterator<SystemMessage> iter = messages.iterator();
        while (iter.hasNext()) {
            if (count == batch) {
                break;
            }
            final SystemMessage next = iter.next();
            if (Objects.equals(group, next.getGroup()) && !locks.containsKey(next.getId())) {
                count++;
                locks.put(next.getId(), processor);
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#getReadyUnlockingGroups(java.util.Date, int)
     */
    @Override
    public synchronized Set<String> getReadyUnlockedGroups(final Date date, final int maxGroupCount) {
        final Set<String> groups = new HashSet<>();
        synchronized (messages) {
            for (final SystemMessage m : messages) {
                if (groups.size() >= maxGroupCount) {
                    break;
                }
                if (!locks.containsKey(m.getId()) && !m.getRetryOn().after(date)) {
                    groups.add(m.getGroup());
                }
            }
        }
        return groups;
    }
    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#unlockAllByGroup(java.lang.String)
     */
    @Override
    public synchronized void unlockAllByGroup(final String group) {
        final Iterator<SystemMessage> iter = messages.iterator();
        while (iter.hasNext()) {
            final SystemMessage next = iter.next();
            if (group.equals(next.getGroup())) {
                locks.remove(next.getId());
            }
        }
    }
    /* (non-Javadoc)
     * @see au.st.messaging.dao.SystemMessageDao#unlockAllForLock(java.lang.String)
     */
    @Override
    public synchronized void unlockAllForLock(final String dispatcher) {
        final Iterator<Map.Entry<Long, String>> iter = locks.entrySet().iterator();
        while (iter.hasNext()) {
            if (dispatcher.equals(iter.next().getValue())) {
                iter.remove();;
            }
        }
    }
    /**
     * @return the locks
     */
    public Map<Long, String> getLocks() {
        return locks;
    }
}
