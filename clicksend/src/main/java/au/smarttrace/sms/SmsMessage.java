/**
 *
 */
package au.smarttrace.sms;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SmsMessage {
    private final List<String> phones = new LinkedList<>();
    private String message;
    private String subject;

    /**
     * @return
     */
    public List<String> getPhones() {
        return phones;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }
    /**
     * @return message subject.
     */
    public String getSubject() {
        return subject;
    }
    /**
     * @param subject the subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * @param phones array of phones to set
     */
    public void setPhones(final String... phones) {
        for (final String p : phones) {
            this.phones.add(p);
        }
    }
}
