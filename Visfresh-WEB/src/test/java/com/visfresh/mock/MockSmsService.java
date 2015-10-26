/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.services.SmsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSmsService implements SmsService {
    /**
     * Default constructor.
     */
    public MockSmsService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SmsService#sendMessage(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessage(final String phone, final String subject, final String message) {
        // TODO Auto-generated method stub

    }

}
