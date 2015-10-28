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
     * @param email eMail.
     * @param subject subject.
     * @param message message.
     * @throws MessagingException
     */
    public void sendMessage(String email, String subject, String message) throws MessagingException;
}
