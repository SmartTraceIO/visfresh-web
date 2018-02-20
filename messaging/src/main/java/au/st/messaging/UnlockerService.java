/**
 *
 */
package au.st.messaging;

import java.util.Date;
import java.util.Set;

import au.st.messaging.dao.DispatcherIdDao;
import au.st.messaging.dao.GroupLockDao;
import au.st.messaging.dao.SystemMessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UnlockerService {
    private DispatcherIdDao dispatcherIdDao;
    private SystemMessageDao systemMessageDao;
    private GroupLockDao groupLockDao;

    /**
     * Default constructor.
     */
    public UnlockerService() {
        super();
    }

    public void unlockExpiredDispatcherIds() {
        final Set<String> ids = dispatcherIdDao.getExpiredDispatcherIds(new Date());
        for (final String dispatcher : ids) {
            systemMessageDao.unlockAllForLock(dispatcher);
            groupLockDao.unlockAllForLock(dispatcher);
            dispatcherIdDao.delete(dispatcher);
        }
    }
    public void unlockExpiredGroupLocks() {
        final Set<String> groups = groupLockDao.getGroupsWithExpiredLock(new Date());
        for (final String group : groups) {
            systemMessageDao.unlockAllByGroup(group);
            groupLockDao.unlockGroup(group);
        }
    }

    /**
     * @return the dispatcherIdDao
     */
    public DispatcherIdDao getDispatcherIdDao() {
        return dispatcherIdDao;
    }
    /**
     * @param dispatcherIdDao the dispatcherIdDao to set
     */
    public void setDispatcherIdDao(final DispatcherIdDao dispatcherIdDao) {
        this.dispatcherIdDao = dispatcherIdDao;
    }
    /**
     * @return the systemMessageDao
     */
    public SystemMessageDao getSystemMessageDao() {
        return systemMessageDao;
    }
    /**
     * @param systemMessageDao the systemMessageDao to set
     */
    public void setSystemMessageDao(final SystemMessageDao systemMessageDao) {
        this.systemMessageDao = systemMessageDao;
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
}
