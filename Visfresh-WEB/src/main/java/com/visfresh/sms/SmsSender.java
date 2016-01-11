/**
 *
 */
package com.visfresh.sms;

import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SmsSender {
    /**
     * @param messages SMS message to send.
     * @throws SmsMessagingException
     */
    public void sendSms(final List<SmsMessage> messages) throws SmsMessagingException;
}
