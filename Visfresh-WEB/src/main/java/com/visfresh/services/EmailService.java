/**
 *
 */
package com.visfresh.services;

import java.io.File;
import java.io.IOException;

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
    /**
     * @param emails eMail.
     * @param subject subject.
     * @param message message.
     * @throws MessagingException
     */
    public void sendMessage(final String[] emails, final String subject, final String text,
            final File... file) throws MessagingException, IOException;
    /**
     * @param emails
     * @param subject
     * @param text
     * @throws MessagingException
     */
    void sendImediatelly(String[] emails, String subject, String text) throws MessagingException;
    /**
     * @return
     */
    String getSupportAddress();
}
