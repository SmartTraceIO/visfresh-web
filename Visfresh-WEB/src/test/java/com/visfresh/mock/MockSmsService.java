/**
 *
 */
package com.visfresh.mock;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.services.SmsService;
import com.visfresh.sms.SmsMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSmsService implements SmsService {
    private List<SmsMessage> messages = new LinkedList<SmsMessage>();

    /**
     * Default constructor.
     */
    public MockSmsService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SmsService#sendMessage(java.lang.String[], java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessage(final String[] phones, final String subject, final String message) {
        final SmsMessage sms = new SmsMessage();
        sms.setPhones(phones);
        sms.setMessage(message);
        sms.setSubject(subject);

        messages.add(sms);
    }
    /**
     * @return the messages
     */
    public List<SmsMessage> getMessages() {
        return messages;
    }
}
