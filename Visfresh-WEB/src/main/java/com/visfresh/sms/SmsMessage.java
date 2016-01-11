/**
 *
 */
package com.visfresh.sms;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SmsMessage {
    private String[] phones;
    private String subject;
    private String message;

    /**
     * Default constructor.
     */
    public SmsMessage() {
        super();
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
    /**
     * @return the phone
     */
    public String[] getPhones() {
        return phones;
    }
    /**
     * @param phones the phone to set
     */
    public void setPhones(final String[] phones) {
        this.phones = phones;
    }
}
