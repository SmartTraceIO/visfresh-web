/**
 *
 */
package com.visfresh.mpl.services;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.io.email.EmailMessage;
import com.visfresh.io.email.EmailServiceHelper;
import com.visfresh.io.json.EmailSerializer;
import com.visfresh.services.EmailService;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EmailServiceImpl implements EmailService, SystemMessageHandler {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailServiceHelper helper = new EmailServiceHelper();
    private final EmailSerializer serializer = new EmailSerializer();

    @Autowired
    private EmailMessageDispatcher dispatcher;

    /**
     * @param env spring environment.
     */
    @Autowired
    public EmailServiceImpl(final Environment env) {
        super();

        final boolean useSsl = "true".equalsIgnoreCase(env.getProperty("mail.smtp.useSsl", "false"));
        helper.setUseSsl(useSsl);

        final String sender = env.getProperty("mail.smtp.sender");
        if (sender == null) {
            throw new RuntimeException("Email sender not specified");
        }
        helper.setSender(sender);

        final String smtpHost = env.getProperty("mail.smtp.host");
        if (smtpHost == null) {
            throw new RuntimeException("SMTP host not specified");
        }
        helper.setSmtpHost(smtpHost);

        helper.setSmtpPort(Integer.parseInt(env.getProperty("mail.smtp.port")));
        helper.setUser(env.getProperty("mail.smtp.user"));
        helper.setPassword(env.getProperty("mail.smtp.password"));
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#sendMessage(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessage(final String[] emails, final String subject, final String message)
            throws MessagingException {
        final EmailMessage msg = new EmailMessage();
        msg.setEmails(emails);
        msg.setMessage(message);
        msg.setSubject(subject);

        final String payload = serializer.toJson(msg).toString();
        dispatcher.sendSystemMessage(payload, SystemMessageType.Email);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final EmailMessage m = serializer.parseEmailMessage(
                SerializerUtils.parseJson(msg.getMessageInfo()));
        log.debug("Email message dequeued: " + msg.getMessageInfo());
        sendImediatelly(m.getEmails(), m.getSubject(), m.getMessage());
    }
    /**
     * @param emails
     * @param subject
     * @param text
     * @throws RetryableException
     */
    public void sendImediatelly(final String[] emails,
            final String subject, final String text) throws RetryableException {
        try {
            helper.sendMessage(emails, subject, text);
        } catch (final MessagingException e) {
            log.error("Failed to send email", e);
            throw new RetryableException("Failed to send email", e);
        }
    }

    @PreDestroy
    public void stop() {
        dispatcher.setEmailHandler(null);
    }
    @PostConstruct
    public void start() {
        dispatcher.setEmailHandler(this);
    }
}
