/**
 *
 */
package com.visfresh.mpl.services;

import org.springframework.stereotype.Component;

import com.visfresh.services.SmsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SmsServiceImpl implements SmsService {
    /**
     *
     */
    public SmsServiceImpl() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SmsService#sendMessage(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessage(final String phone, final String subject, final String message) {
        System.out.println("SMS to " + phone + " by subject " + subject + " and body "
                + message + " will sent in future");
    }
}
