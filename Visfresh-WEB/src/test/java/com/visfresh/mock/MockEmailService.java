/**
 *
 */
package com.visfresh.mock;

import java.util.LinkedList;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.stereotype.Component;

import com.visfresh.io.email.EmailMessage;
import com.visfresh.io.email.EmailServiceHelper;
import com.visfresh.services.EmailService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockEmailService implements EmailService {
    /**
     * List of email messages.
     */
    private final List<EmailMessage> messages = new LinkedList<>();

    /**
     * @param env
     */
    public MockEmailService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#sendMessage(java.lang.String[], java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessage(final String[] emails, final String subject, final String message)
            throws MessagingException {
        final EmailMessage msg = new EmailMessage();
        msg.setEmails(emails);
        msg.setMessage(message);
        msg.setSubject(subject);

        this.messages.add(msg);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#sendMessageToSupport(java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessageToSupport(final String subject, final String message)
            throws MessagingException {
        sendMessage(new String[]{"support@smarttrace.com.au"}, subject, message);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#getHelper()
     */
    @Override
    public EmailServiceHelper getHelper() {
        return null;
    }

    /**
     * @return the messages
     */
    public List<EmailMessage> getMessages() {
        return messages;
    }
}
