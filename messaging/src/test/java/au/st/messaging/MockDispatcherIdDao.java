/**
 *
 */
package au.st.messaging;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import au.st.messaging.dao.DispatcherIdDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MockDispatcherIdDao implements DispatcherIdDao {
    private final Map<String, Date> activities;

    /**
     * @param activities activity map.
     */
    public MockDispatcherIdDao(final Map<String, Date> activities) {
        super();
        this.activities = activities;
    }

    /* (non-Javadoc)
     * @see au.st.messaging.dao.DispatcherIdDao#saveHandlerId(java.lang.String)
     */
    @Override
    public synchronized boolean saveHandlerId(final String newId, final Date expiredOn) {
        if (!activities.containsKey(newId)) {
            activities.put(newId, expiredOn);
            return true;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see au.st.messaging.dao.DispatcherIdDao#updateHandlerActivity(java.lang.String, java.util.Date)
     */
    @Override
    public synchronized void updateExpiedTime(final String id, final Date expiredTime) {
        activities.put(id, expiredTime);
    }
    /* (non-Javadoc)
     * @see au.st.messaging.dao.DispatcherIdDao#delete(java.lang.String)
     */
    @Override
    public synchronized void delete(final String id) {
        activities.remove(id);
    }
    /* (non-Javadoc)
     * @see au.st.messaging.dao.DispatcherIdDao#getExpiredDispatcherIds(java.util.Date)
     */
    @Override
    public synchronized Set<String> getExpiredDispatcherIds(final Date date) {
        final Set<String> ids = new HashSet<>();

        final Iterator<Map.Entry<String, Date>> iter = activities.entrySet().iterator();
        while (iter.hasNext()) {
            final Entry<String, Date> next = iter.next();
            if (!date.after(next.getValue())) {
                ids.add(next.getKey());
            }
        }

        return ids;
    }
}
