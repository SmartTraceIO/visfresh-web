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
     *
     */
    private static final String DEFAULT_SUPPORT_ADDRESS = "support@smarttrace.com.au";

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final EmailServiceHelper helper;
    private final EmailSerializer serializer = new EmailSerializer();

    @Autowired
    private EmailMessageDispatcher dispatcher;
    private String supportAddress = DEFAULT_SUPPORT_ADDRESS;

    /**
     * @param env spring environment.
     */
    @Autowired
    public EmailServiceImpl(final Environment env) {
        this(createHelper(env));
        supportAddress = env.getProperty("mail.support.address", DEFAULT_SUPPORT_ADDRESS);
    }
    /**
     *
     */
    public EmailServiceImpl(final EmailServiceHelper helper) {
        super();
        this.helper = helper;
    }
    /**
     * @param env
     * @return
     */
    public static EmailServiceHelper createHelper(final Environment env) {
        final boolean useSsl = "true".equalsIgnoreCase(env.getProperty("mail.smtp.useSsl", "false"));
        final EmailServiceHelper helper = new EmailServiceHelper();
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

        return helper;
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
        sendSystemMessage(payload, SystemMessageType.Email);
    }
    @Override
    public void sendMessageToSupport(final String subject, final String message) throws MessagingException {
        sendMessage(new String[]{supportAddress}, subject, message);
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
     * @param payload
     * @param type
     */
    protected void sendSystemMessage(final String payload, final SystemMessageType type) {
        dispatcher.sendSystemMessage(payload, type);
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
    /**
     * @return the helper
     */
    @Override
    public EmailServiceHelper getHelper() {
        return helper;
    }
}
