/**
 *
 */
package com.visfresh.mock;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.stereotype.Component;

import com.visfresh.io.email.EmailMessage;
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
     * List of email messages.
     */
    private final List<List<File>> attachments = new LinkedList<>();

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
        this.attachments.add(null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#sendMessage(java.lang.String[], java.lang.String, java.lang.String, java.io.File[])
     */
    @Override
    public void sendMessage(final String[] emails, final String subject, final String text,
            final File... file) throws MessagingException, IOException {
        final EmailMessage msg = new EmailMessage();
        msg.setEmails(emails);
        msg.setMessage(text);
        msg.setSubject(subject);

        this.messages.add(msg);
        this.attachments.add(Arrays.asList(file));
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#sendMessageToSupport(java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessageToSupport(final String subject, final String message)
            throws MessagingException {
        sendMessage(new String[]{"support@smarttrace.com.au"}, subject, message);
    }

    /**
     * @return the messages
     */
    public List<EmailMessage> getMessages() {
        return messages;
    }
    /**
     * @return the attachments
     */
    public List<List<File>> getAttachments() {
        return attachments;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#getSupportAddress()
     */
    @Override
    public String getSupportAddress() {
        return "support@smarttrace.com.au";
    }
    public void clear() {
        messages.clear();
        attachments.clear();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#sendImediatelly(java.lang.String[], java.lang.String, java.lang.String)
     */
    @Override
    public void sendImediatelly(final String[] emails, final String subject, final String text) throws MessagingException {
        sendMessage(emails, subject, text);
    }
}
