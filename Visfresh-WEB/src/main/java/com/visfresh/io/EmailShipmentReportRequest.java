/**
 *
 */
package com.visfresh.io;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EmailShipmentReportRequest {
    private String sn;
    private int trip;
    private String subject;
    private String messageBody;
    private final List<Long> users = new LinkedList<>();
    private final List<String> emails = new LinkedList<>();

    /**
     * Default constructor.
     */
    public EmailShipmentReportRequest() {
        super();
    }

    public String getSn() {
        return sn;
    }
    public void setSn(final String sn) {
        this.sn = sn;
    }
    public int getTrip() {
        return trip;
    }
    public void setTrip(final int trip) {
        this.trip = trip;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(final String subject) {
        this.subject = subject;
    }
    public String getMessageBody() {
        return messageBody;
    }
    public void setMessageBody(final String messageBody) {
        this.messageBody = messageBody;
    }
    public List<Long> getUsers() {
        return users;
    }
    public List<String> getEmails() {
        return emails;
    }
}
