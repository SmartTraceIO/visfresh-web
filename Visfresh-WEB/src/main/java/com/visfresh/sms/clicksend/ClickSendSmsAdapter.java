/**
 *
 */
package com.visfresh.sms.clicksend;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.clicksend.sdk.ClickSendSmsClient;
import com.clicksend.sdk.request.Message;
import com.clicksend.sdk.request.TextMessage;
import com.visfresh.sms.SmsMessage;
import com.visfresh.sms.SmsMessagingException;
import com.visfresh.sms.SmsSender;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ClickSendSmsAdapter implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(ClickSendSmsAdapter.class);

    private static final String DEFAULT_SENDER = "SmartTrace";

    private String sender = DEFAULT_SENDER;
    private boolean isEnabled = true;
    private final ClickSendSmsClient client;

    /**
     * Default constructor.
     */
    @Autowired
    public ClickSendSmsAdapter(final Environment env) {
        this(env.getProperty("sms.clicksend.user"), env.getProperty("sms.clicksend.key"));

        sender = env.getProperty("sms.clicksend.sender", DEFAULT_SENDER);
        isEnabled = Boolean.valueOf(env.getProperty("sms.clicksend.enabled", "true"));
    }
    /**
     * @param userName
     *            user name.
     * @param password
     *            password.
     */
    protected ClickSendSmsAdapter(final String userName, final String password) {
        super();

        if (password == null) {
            throw new RuntimeException("ClickSend API key is null");
        }
        if (userName == null) {
            throw new RuntimeException("ClickSend API user name is null");
        }
        try {
            client = new ClickSendSmsClient(userName, password);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.sms.SmsSender#sendSms(java.util.List)
     */
    @Override
    public void sendSms(final List<SmsMessage> messages)
            throws SmsMessagingException {
        if (!isEnabled) {
            throw new SmsMessagingException("SlickSend SMS sender is disabled"
                    + " by flag sms.clicksend.enabled for now");
        }

        for (final SmsMessage m : messages) {
            for (final String phone : m.getPhones()) {
                try {
                    client.sendSms(createMessage(phone, m.getSubject(), m.getMessage()));
                } catch (final Exception e) {
                    log.error("Failed to send SMS message to " + phone, e);
                }
            }
        }
    }

    /**
     * @param phone
     * @param subject
     * @param message
     * @return
     */
    private Message createMessage(final String phone, final String subject, final String message) {
        return new TextMessage(phone, message, sender, null, subject, null);
    }

    public static void main(final String[] args) {
//        sms.clicksend.key=8AE5FFC5-68B0-F096-35B6-103220F7E893
//        sms.clicksend.user=vyacheslav
        final ClickSendSmsAdapter a= new ClickSendSmsAdapter(
                "vyacheslav", "8AE5FFC5-68B0-F096-35B6-103220F7E893");

        final SmsMessage message = new SmsMessage();
        message.setMessage("Test message body");
        message.setSubject("Test message subject");
        message.setPhones(new String[] {"+61411111111"});

        try {
            a.sendSms(Arrays.asList(message));
        } catch (final SmsMessagingException e) {
            e.printStackTrace();
        }
    }
}
