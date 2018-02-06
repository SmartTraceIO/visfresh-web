/**
 *
 */
package au.smarttrace.sms;

import java.util.LinkedList;
import java.util.List;

import com.clicksend.sdk.ClickSendSmsClient;
import com.clicksend.sdk.request.Message;
import com.clicksend.sdk.request.TextMessage;
import com.clicksend.sdk.response.ReplyResult;
import com.clicksend.sdk.response.ReplyResultItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ClickSendSmsAdapter implements SmsSender {
    private static final String DEFAULT_SENDER = "SmartTrace";

    private String sender = DEFAULT_SENDER;
    private boolean isEnabled = true;
    private final ClickSendSmsClient client;

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
    public void sendSms(final SmsMessage m) throws SmsMessagingException {
        if (!isEnabled) {
            throw new SmsMessagingException("SlickSend SMS sender is disabled"
                    + " by flag sms.clicksend.enabled for now");
        }

        try {
            client.sendSms(createMessage(m.getPhones(), m.getSubject(), m.getMessage()));
        } catch (final Exception e) {
            throw new SmsMessagingException(e);
        }
    }

    @Override
    public List<SmsMessage> getRecipes() throws SmsMessagingException {
        final List<SmsMessage> messages = new LinkedList<>();

        ReplyResult replies;
        try {
            replies = client.getReplies();
        } catch (final Exception e) {
            throw new SmsMessagingException(e);
        }

        for (final ReplyResultItem item : replies.getReplyResultItems()) {
            final SmsMessage m = new SmsMessage();
            m.setMessage(item.getMessage());
            m.getPhones().add(item.getFrom());
            m.setSubject(item.getOriginalMessageId());
            messages.add(m);
        }

        return messages;
    }
    /**
     * @param phones phones.
     * @param subject subject.
     * @param message message.
     * @return
     */
    private Message createMessage(final List<String> phones, final String subject, final String message) {
        return new TextMessage(String.join(",", phones), message, sender, null, subject, null);
    }
}
