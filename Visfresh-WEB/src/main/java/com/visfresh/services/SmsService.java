/**
 *
 */
package com.visfresh.services;

/**
 * Possible need to add the Carrier, Country, ... as parameters.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SmsService {
    /**
     * Sends message to given phone.
     * @param phones phone number.
     * @param subject TODO
     * @param message message.
     */
    public void sendMessage(String[] phones, String subject, String message);
}
