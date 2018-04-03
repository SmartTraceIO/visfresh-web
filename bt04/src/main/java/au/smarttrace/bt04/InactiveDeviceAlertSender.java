/**
 *
 */
package au.smarttrace.bt04;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.smarttrace.email.EmailSender;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class InactiveDeviceAlertSender {
    private static final Logger log = LoggerFactory.getLogger(InactiveDeviceAlertSender.class);

    @Autowired
    private EmailSender sender;

    /**
     * Default constructor.
     */
    public InactiveDeviceAlertSender() {
        super();
    }

    /**
     * @param additionalEmails array of additional emails.
     * @param subject message subject.
     * @param message message body.
     */
    public void sendAlert(final String[] additionalEmails, final String subject, final String message) {
        final String[] defaultEmails = sender.getAddresses();

        final String[] emails = new String[defaultEmails.length + additionalEmails.length];
        System.arraycopy(defaultEmails, 0, emails, 0, defaultEmails.length);
        System.arraycopy(additionalEmails, 0, emails, defaultEmails.length, additionalEmails.length);

        try {
            sender.sendMessage(subject, message, emails);
        } catch (final Throwable e) {
            log.error("Failed to send email notification", e);
        }
    }
}
