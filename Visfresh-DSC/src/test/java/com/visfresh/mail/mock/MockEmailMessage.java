/**
 *
 */
package com.visfresh.mail.mock;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MockEmailMessage {
    private String subject;
    private String message;
    private String[] addresses;
    /**
     * Default constructor.
     */
    public MockEmailMessage() {
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
     * @return the addresses
     */
    public String[] getAddresses() {
        return addresses;
    }
    /**
     * @param addresses the addresses to set
     */
    public void setAddresses(final String[] addresses) {
        this.addresses = addresses;
    }
}
