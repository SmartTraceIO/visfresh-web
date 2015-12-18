/**
 *
 */
package com.visfresh.io.email;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 * insert into systemmessages(type, time, retryon, message) values ('Email', now(), now(), '{"message":"Test","subject":"Test","emails":["vyacheslav.soldatov@inbox.ru"]}');
 */
public class EmailMessage {
    private String[] emails;
    private String subject;
    private String message;

    /**
     * Default constructor.
     */
    public EmailMessage() {
        super();
    }

    /**
     * @return the email
     */
    public String[] getEmails() {
        return emails;
    }

    /**
     * @param email the email to set
     */
    public void setEmails(final String[] email) {
        this.emails = email;
    }
    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }
    /**
     * @param subject the subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }
}
