/**
 *
 */
package com.visfresh.io.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SmsServiceHelper {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SmsServiceHelper.class);

    /**
     * @param phones phone array.
     * @param subject message subject.
     * @param message message payload.
     */
    public void sendMessage(final String[] phones, final String subject, final String message)
            throws SmsMessagingException {
        log.debug("Message '" + subject + "' sending will implemented later ");
    }
}
