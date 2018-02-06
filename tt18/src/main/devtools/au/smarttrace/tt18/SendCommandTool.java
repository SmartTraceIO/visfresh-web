/**
 *
 */
package au.smarttrace.tt18;

import au.smarttrace.sms.ClickSendSmsAdapter;
import au.smarttrace.sms.SmsMessage;
import au.smarttrace.sms.SmsMessagingException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SendCommandTool extends ClickSendSmsAdapter {
    /**
     * @param userName
     * @param password
     */
    public SendCommandTool(final String userName, final String password) {
        super(userName, password);
    }

    public static void main(final String[] args) {
        final ClickSendSmsAdapter a= new SendCommandTool("vyacheslav", "8AE5FFC5-68B0-F096-35B6-103220F7E893");

        try {
            a.sendSms(createCommand("", ""));
        } catch (final SmsMessagingException e) {
            e.printStackTrace();
        }
    }
    /**
     * @param phone phone number.
     * @param command command.
     * @return SMS message.
     */
    private static SmsMessage createCommand(final String phone, final String command) {
        final SmsMessage message = new SmsMessage();
        message.setMessage(command);
        message.setSubject("Command");
        message.setPhones(new String[] {phone});
        return message;
    }
}
