/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BroadcastPackage extends AbstractPackage {
    public enum MessageType {
        SendToRecipient(0x01),//0x01: Indicate that instruction content is a device command
        Other(-1);//Other: Reserved

        private final int value;

        /**
         * @param value numeric value.
         */
        private MessageType(final int value) {
            this.value = value;
        }

        public static MessageType valueOf(final int value) {
            for (final MessageType pid : values()) {
                if (pid.value == value) {
                    return pid;
                }
            }
            return Other;
        }
        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    private MessageType type;
    private String phoneNumber;
    private String content;

    /**
     * Default constructor.
     */
    public BroadcastPackage() {
        super();
    }

    /**
     * @param type
     */
    public void setType(final MessageType type) {
        this.type = type;
    }
    /**
     * @return the type
     */
    public MessageType getType() {
        return type;
    }
    /**
     * @param num
     */
    public void setPhoneNumber(final String num) {
        this.phoneNumber = num;
    }
    /**
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    /**
     * @param str
     */
    public void setContent(final String str) {
        this.content = str;
    }
    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }
}
