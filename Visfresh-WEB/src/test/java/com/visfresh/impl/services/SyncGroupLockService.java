/**
 *
 */
package com.visfresh.impl.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SyncGroupLockService extends GroupLockServiceImpl {
    /**
     * @param env
     */
    @Autowired
    public SyncGroupLockService(final Environment env) {
        super(env);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.GroupLockServiceImpl#unlockOlds()
     */
    @Override
    public void unlockOlds() {
        // disable scheduled invocation.
    }
}
