/**
 *
 */
package au.smarttrace.sms;

import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SmsSender {
    /**
     * @param message list of messages to send.
     * @throws SmsMessagingException
     */
    void sendSms(SmsMessage message) throws SmsMessagingException;
    /**
     * @return
     * @throws SmsMessagingException
     */
    List<SmsMessage> getRecipes() throws SmsMessagingException;
}
