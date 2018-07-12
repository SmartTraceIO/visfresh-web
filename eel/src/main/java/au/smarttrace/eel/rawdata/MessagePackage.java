/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessagePackage extends AbstractPackage {
    private DevicePosition location;
    private String phoneNumber;
    private String message;

    /**
     * Default constructor.
     */
    public MessagePackage() {
        super();
    }

    /**
     * @param pos
     */
    public void setLocation(final DevicePosition pos) {
        this.location = pos;
    }
    /**
     * @return the location
     */
    public DevicePosition getLocation() {
        return location;
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
     * @param msg
     */
    public void setMessage(final String msg) {
        this.message = msg;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
