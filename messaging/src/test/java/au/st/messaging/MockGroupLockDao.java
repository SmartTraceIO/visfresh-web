/**
 *
 */
package au.st.messaging;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import au.st.messaging.dao.GroupLockDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MockGroupLockDao implements GroupLockDao {
    private final Map<String, Lock> locks = new HashMap<>();

    public static class Lock {
        String processor;
        Date unlockTime;

        /**
         * @param processor processor.
         * @param unlockTime unlock time.
         */
        public Lock(final String processor, final Date unlockTime) {
            super();
            Objects.requireNonNull(processor);
            this.processor = processor;
            Objects.requireNonNull(unlockTime);
            this.unlockTime = unlockTime;
        }
        /**
         * @return the unlockTime
         */
        public Date getUnlockTime() {
            return unlockTime;
        }
    }

    /**
     * Default constructor.
     */
    public MockGroupLockDao() {
        super();
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.GroupLockDao#lockGroup(java.lang.String, java.lang.String, java.util.Date)
     */
    @Override
    public synchronized boolean lockGroup(final String processor, final String group, final Date unlockOn) {
        if (!locks.containsKey(group)) {
            locks.put(group, new Lock(processor, unlockOn));
            return true;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see au.st.messaging.dao.GroupLockDao#getGroupsWithExpiredLock(java.util.Date)
     */
    @Override
    public synchronized Set<String> getGroupsWithExpiredLock(final Date date) {
        final Set<String> groups = new HashSet<>();
        final Iterator<Map.Entry<String, Lock>> iter = locks.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<String, Lock> next = iter.next();
            if (!next.getValue().getUnlockTime().after(date)) {
                groups.add(next.getKey());
            }
        }
        return groups;
    }
    /* (non-Javadoc)
     * @see au.st.messaging.dao.GroupLockDao#unlockGroup(java.lang.String)
     */
    @Override
    public synchronized void unlockGroup(final String group) {
        locks.remove(group);
    }
    /* (non-Javadoc)
     * @see au.st.messaging.dao.GroupLockDao#unlockAllForLock(java.lang.String)
     */
    @Override
    public synchronized void unlockAllForLock(final String dispatcher) {
        final Iterator<Lock> iter = locks.values().iterator();
        while (iter.hasNext()) {
            if (iter.next().processor.equals(dispatcher)) {
                iter.remove();
            }
        }
    }
}
