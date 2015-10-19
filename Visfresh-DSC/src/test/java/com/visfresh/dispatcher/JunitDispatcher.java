/**
 *
 */
package com.visfresh.dispatcher;

import org.springframework.stereotype.Component;

import com.visfresh.DeviceMessageBase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class JunitDispatcher extends AbstractDispatcher {
    /**
     * Default constructor.
     */
    public JunitDispatcher() {
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
    public void handleError(final DeviceMessageBase msg, final Throwable e) {
        super.handleError(msg, e);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#handleSuccess(com.visfresh.DeviceMessageBase)
     */
    @Override
    public void handleSuccess(final DeviceMessageBase msg) {
        super.handleSuccess(msg);
    }
}
