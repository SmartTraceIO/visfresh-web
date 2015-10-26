/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.services.EmailService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockEmailService implements EmailService {

    /**
     *
     */
    public MockEmailService() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.EmailService#sendMessage(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessage(final String email, final String subject, final String message) {
        // TODO Auto-generated method stub

    }

}
