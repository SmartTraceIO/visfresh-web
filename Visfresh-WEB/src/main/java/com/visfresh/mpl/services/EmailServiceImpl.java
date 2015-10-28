/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.services.EmailService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EmailServiceImpl implements EmailService {
    private final boolean useSsl;
    private final String sender;
    private final String smtpHost;
    private final int smtpPort;
    private final String user;
    private final String password;

    /**
     * @param env spring environment.
     */
    @Autowired
    public EmailServiceImpl(final Environment env) {
        super();
        useSsl = "true".equalsIgnoreCase(env.getProperty("mail.smtp.useSsl", "false"));
        sender = env.getProperty("mail.smtp.sender");
        if (sender == null) {
            throw new RuntimeException("Email sender not specified");
        }
        smtpHost = env.getProperty("mail.smtp.host");
        if (smtpHost == null) {
            throw new RuntimeException("SMTP host not specified");
        }
        smtpPort = Integer.parseInt(env.getProperty("mail.smtp.port"));
        user = env.getProperty("mail.smtp.user");
        password = env.getProperty("mail.smtp.password");
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#sendMessage(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessage(final String email, final String subject, final String message) throws MessagingException {
        final Properties props = new Properties();
        Authenticator auth = null;
        if (user != null && password != null) {
            props.put("mail.smtp.auth", "true");
            auth = new Authenticator() {
                /* (non-Javadoc)
                 * @see javax.mail.Authenticator#getPasswordAuthentication()
                 */
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
            };
        } else {
            props.put("mail.smtp.auth", "false");
        }

        if (!useSsl) {
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);

            sendMessage(props, auth, email, sender, subject, message);
            return;
        } else {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.socketFactory.port", smtpPort);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");

            sendMessage(props, auth, email, sender, subject, message);
        }
    }

    private void sendMessage(final Properties props,
            final Authenticator auth,
            final String to,
            final String from,
            final String subject,
            final String textBody) throws MessagingException {
        // create some properties and get the default Session
        final Session session = Session.getInstance(props, auth);
        session.setDebug(true);

        // create a message
        final Message msg = new MimeMessage(session);

        // set the from and to address
        final InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        final InternetAddress addressTo = new InternetAddress(to);
        msg.addRecipient(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setText(textBody);

        Transport.send(msg);
    }
}
