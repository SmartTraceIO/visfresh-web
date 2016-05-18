/**
 *
 */
package com.visfresh.sms;

import java.util.LinkedList;
import java.util.List;

import com.clicksend.sdk.ClickSendSmsClient;
import com.clicksend.sdk.request.Message;
import com.clicksend.sdk.request.TextMessage;
import com.visfresh.cfg.Config;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SmsSender {
    private static final String DEFAULT_SENDER = "SmartTrace";

    private String sender = DEFAULT_SENDER;
    private final ClickSendSmsClient client;
    private final String[] phones;

    /**
     * Default constructor.
     */
//    public ClickSendSmsAdapter(final Environment env) {
//        this(env.getProperty("sms.clicksend.user"), env.getProperty("sms.clicksend.key"));
//
//        sender = env.getProperty("sms.clicksend.sender", DEFAULT_SENDER);
//        isEnabled = Boolean.valueOf(env.getProperty("sms.clicksend.enabled", "true"));
//    }
    /**
     *
     */
    public SmsSender() {
        super();
        final String password = Config.getProperty("sms.clicksend.key");
        if (password == null) {
            throw new RuntimeException("ClickSend API key is null");
        }
        final String userName = Config.getProperty("sms.clicksend.user");
        if (userName == null) {
            throw new RuntimeException("ClickSend API user name is null");
        }
        try {
            client = new ClickSendSmsClient(userName, password);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final String s = Config.getProperty("sms.clicksend.sender");
        if (s != null) {
            sender = s;
        }

        //load addresses
        final List<String> list = new LinkedList<>();
        int i = 1;
        while (true) {
            final String address = Config.getProperty("sms.address." + i);
            if (address == null) {
                break;
            }
            list.add(address);
            i++;
        }

        phones = list.toArray(new String[list.size()]);
    }
    /* (non-Javadoc)
     * @see com.visfresh.sms.SmsSender#sendSms(java.util.List)
     */
    public void sendSms(final String subject, final String message) throws Exception {
        for (final String phone : phones) {
            client.sendSms(createMessage(phone, subject, message));
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

    public static void main(final String[] args) throws Exception {
//        sms.clicksend.key=8AE5FFC5-68B0-F096-35B6-103220F7E893
//        sms.clicksend.user=vyacheslav
        final SmsSender a = new SmsSender();
        a.sendSms("Test message", "Test message from service availability checker");
    }
}
