/**
 *
 */
package com.visfresh.mail.mock;

import java.util.LinkedList;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.mail.EmailSender;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockEmailSender extends EmailSender {
    private List<MockEmailMessage> messages = new LinkedList<>();

    /**
     * Default constructor.
     */
    @Autowired
    public MockEmailSender(final Environment env) {
        super(env);
    }

    /* (non-Javadoc)
     * @see com.visfresh.mail.EmailSender#sendMessage(java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public void sendMessage(final String subject, final String message, final String... addresses)
            throws MessagingException {
        final MockEmailMessage msg = new MockEmailMessage();
        msg.setSubject(subject);
        msg.setMessage(message);
        msg.setAddresses(addresses);
        messages.add(msg);
    }
    /**
     * @return the messages
     */
    public List<MockEmailMessage> getMessages() {
        return messages;
    }
}
