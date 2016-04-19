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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;

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
        //setup connection
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

        props.put("mail.smtp.from", getSender());
        props.put("mail.smtp.user", getUser());
        props.put("mail.smtp.password", getPassword());
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        if (useSsl) {
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.socketFactory.port", smtpPort);
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.socketFactory.class", WorkaroundSslSocketFactory.class.getName());
            props.put("mail.smtps.socketFactory.class", WorkaroundSslSocketFactory.class.getName());
        }
        // create some properties and get the default Session

        final Session session = Session.getInstance(props, auth);
        // create a message
        final Message msg = new MimeMessage(session);

        // set the from and to address
        msg.setFrom(new InternetAddress(sender));

        //add receipts
        for (final String receipt : email) {
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(receipt));
        }

        // Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setText(message);

        final SMTPTransport t = (SMTPTransport) session.getTransport(isUseSsl() ? "smtps" : "smtp");
        t.connect(getSmtpHost(), getSmtpPort(), getUser(), getPassword());

        try {
            t.sendMessage(msg, msg.getAllRecipients());
        } finally {
            t.close();
        }
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

    public static void main(final String[] args) throws MessagingException {
        final EmailServiceHelper h = new EmailServiceHelper();
        h.setUseSsl(true);
        h.setSender("api@smarttrace.com.au");
        h.setSmtpHost("mail.messagingengine.com");
        h.setSmtpPort(465);
        h.setUser("api@smarttrace.com.au");
        h.setPassword("figspace");

        h.sendMessage(new String[] {"james@smarttrace.com.au", "vyacheslav.soldatov@inbox.ru"},
                "Test Message", "Test message for new email service");
    }
}
