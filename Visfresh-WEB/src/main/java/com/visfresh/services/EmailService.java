/**
 *
 */
package com.visfresh.services;

import javax.mail.MessagingException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface EmailService {
    /**
     * @param emails eMail.
     * @param subject subject.
     * @param message message.
     * @throws MessagingException
     */
    public void sendMessage(String[] emails, String subject, String message) throws MessagingException;
    /**
     * @param subject message subject.
     * @param message message body.
     * @throws MessagingException
     */
    void sendMessageToSupport(String subject, String message) throws MessagingException;
}
