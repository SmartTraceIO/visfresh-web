/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.entities.SystemMessageType;
import com.visfresh.services.SystemMessageDispatcher;
import com.visfresh.services.SystemMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSystemMessageDispatcher implements SystemMessageDispatcher {
    /**
     * Default constructor.
     */
    public MockSystemMessageDispatcher() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageDispatcher#processMessages(java.lang.String)
     */
    @Override
    public int processMessages(final String processor) {
        return 0;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageDispatcher#setSystemMessageHandler(com.visfresh.entities.SystemMessageType, com.visfresh.services.SystemMessageHandler)
     */
    @Override
    public void setSystemMessageHandler(final SystemMessageType type,
            final SystemMessageHandler h) {
    }
}
