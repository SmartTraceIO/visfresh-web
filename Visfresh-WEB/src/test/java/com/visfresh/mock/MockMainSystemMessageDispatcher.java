/**
 *
 */
package com.visfresh.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.mpl.services.MainSystemMessageDispatcher;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockMainSystemMessageDispatcher extends
        MainSystemMessageDispatcher {
    /**
     * @param env
     */
    @Autowired
    public MockMainSystemMessageDispatcher(final Environment env) {
        super(env);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#start()
     */
    @Override
    public void start() {
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#stop()
     */
    @Override
    public void stop() {
    }
}
