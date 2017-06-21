/**
 *
 */
package com.visfresh.mock;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.entities.SystemMessageType;
import com.visfresh.impl.services.MainSystemMessageDispatcher;

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
     * @see com.visfresh.services.AbstractSystemMessageDispatcher#sendSystemMessage(java.lang.String, com.visfresh.entities.SystemMessageType, java.util.Date)
     */
    @Override
    public void sendSystemMessage(final String messagePayload,
            final SystemMessageType type, final Date retryOn) {
        super.sendSystemMessage(messagePayload, type, retryOn);
        processMessages("junit-main-dispatcher");
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
