/**
 *
 */
package com.visfresh.services;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface EmailService {
    /**
     * @param email eMail.
     * @param subject subject.
     * @param message message.
     */
    public void sendMessage(String email, String subject, String message);
}
