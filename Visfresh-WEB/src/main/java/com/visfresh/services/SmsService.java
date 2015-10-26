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
     * @param phone phone number.
     * @param message message.
     */
    public void sendMessage(String phone, String message);
}
