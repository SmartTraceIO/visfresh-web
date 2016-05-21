/**
 *
 */
package com.visfresh.dispatcher.mock;

import org.springframework.stereotype.Component;

import com.visfresh.DeviceMessage;
import com.visfresh.dispatcher.AbstractDispatcher;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class JUnitDispatcher extends AbstractDispatcher {
    /**
     * Default constructor.
     */
    public JUnitDispatcher() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#processMessages()
     */
    @Override
    public int processMessages() {
        return 0;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#handleError(com.visfresh.DeviceMessageBase, java.lang.Throwable)
     */
    @Override
    public void handleError(final DeviceMessage msg, final Throwable e) {
        super.handleError(msg, e);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#handleSuccess(com.visfresh.DeviceMessageBase)
     */
    @Override
    public void handleSuccess(final DeviceMessage msg) {
        super.handleSuccess(msg);
    }
}
