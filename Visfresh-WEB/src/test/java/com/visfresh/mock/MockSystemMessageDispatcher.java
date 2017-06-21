/**
 *
 */
package com.visfresh.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.impl.services.TrackerMessageDispatcher;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSystemMessageDispatcher extends TrackerMessageDispatcher {
    /**
     * @param env environment.
     */
    @Autowired
    public MockSystemMessageDispatcher(final Environment env) {
        super(env);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#start()
     */
    @Override
    public void start() {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }
}
