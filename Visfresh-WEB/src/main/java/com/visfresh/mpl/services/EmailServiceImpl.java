/**
 *
 */
package com.visfresh.mpl.services;

import org.springframework.stereotype.Component;

import com.visfresh.services.EmailService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EmailServiceImpl implements EmailService {

    /**
     *
     */
    public EmailServiceImpl() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#sendMessage(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessage(final String email, final String subject, final String message) {
        System.out.println("Email to " + email + " by subject " + subject + " and body "
                + message + " will sent in future");
    }
}
