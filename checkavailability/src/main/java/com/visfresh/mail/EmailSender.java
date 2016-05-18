/**
 *
 */
package com.visfresh.mail;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;
import com.visfresh.cfg.Config;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EmailSender {
    private boolean useSsl;
    private String sender;
    private String smtpHost;
    private int smtpPort;
    private String user;
    private String password;
    private final String[] addresses;

    /**
     * @param env spring environment.
     */
    public EmailSender() {
        super();
        //load configuration
        final boolean useSsl = "true".equalsIgnoreCase(Config.getProperty("mail.smtp.useSsl", "false"));
        setUseSsl(useSsl);

        final String sender = Config.getProperty("mail.smtp.sender");
        if (sender == null) {
            throw new RuntimeException("Email sender not specified");
        }
        setSender(sender);

        final String smtpHost = Config.getProperty("mail.smtp.host");
        if (smtpHost == null) {
            throw new RuntimeException("SMTP host not specified");
        }
        setSmtpHost(smtpHost);

        setSmtpPort(Integer.parseInt(Config.getProperty("mail.smtp.port")));
        setUser(Config.getProperty("mail.smtp.user"));
        setPassword(Config.getProperty("mail.smtp.password"));

        //load addresses
        final List<String> list = new LinkedList<>();
        int i = 1;
        while (true) {
            final String address = Config.getProperty("mail.address." + i);
            if (address == null) {
                break;
            }
            list.add(address);
            i++;
        }

        addresses = list.toArray(new String[list.size()]);
    }

    public void sendMessage(final String subject, final String message)
            throws MessagingException {
        if (addresses.length == 0) {
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
        for (final String receipt : addresses) {
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
        final EmailSender h = new EmailSender();

//        h.sendMessage(new String[] {"james@smarttrace.com.au", "vyacheslav.soldatov@inbox.ru"},
//                "Test Message", "Test message for service availability checker");
        h.sendMessage("Test Message",
                "Test message from service availability checker");
    }
}
