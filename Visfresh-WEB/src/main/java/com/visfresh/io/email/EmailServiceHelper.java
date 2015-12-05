/**
 *
 */
package com.visfresh.io.email;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EmailServiceHelper {
    private boolean useSsl;
    private String sender;
    private String smtpHost;
    private int smtpPort;
    private String user;
    private String password;

    /**
     * @param env spring environment.
     */
    public EmailServiceHelper() {
        super();
    }

    public void sendMessage(final String[] email, final String subject, final String message)
            throws MessagingException {
        if (email.length == 0) {
            return;
        }

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
            final String[] to,
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

        //add receipts
        for (final String receipt : to) {
            final InternetAddress addressTo = new InternetAddress(receipt);
            msg.addRecipient(Message.RecipientType.TO, addressTo);
        }

        // Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setText(textBody);

        Transport.send(msg);
    }

    /**
     * @return the useSsl
     */
    public boolean isUseSsl() {
        return useSsl;
    }
    /**
     * @param useSsl the useSsl to set
     */
    public void setUseSsl(final boolean useSsl) {
        this.useSsl = useSsl;
    }
    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }
    /**
     * @param sender the sender to set
     */
    public void setSender(final String sender) {
        this.sender = sender;
    }
    /**
     * @return the smtpHost
     */
    public String getSmtpHost() {
        return smtpHost;
    }
    /**
     * @param smtpHost the smtpHost to set
     */
    public void setSmtpHost(final String smtpHost) {
        this.smtpHost = smtpHost;
    }
    /**
     * @return the smtpPort
     */
    public int getSmtpPort() {
        return smtpPort;
    }
    /**
     * @param smtpPort the smtpPort to set
     */
    public void setSmtpPort(final int smtpPort) {
        this.smtpPort = smtpPort;
    }
    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }
    /**
     * @param user the user to set
     */
    public void setUser(final String user) {
        this.user = user;
    }
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    /**
     * @param password the password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }
}
