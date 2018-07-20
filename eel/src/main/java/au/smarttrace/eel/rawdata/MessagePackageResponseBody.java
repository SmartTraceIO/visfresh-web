/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessagePackageResponseBody implements PackageBody {
    private String phoneNumber;
    private String content;

    /**
     * Default constructor.
     */
    public MessagePackageResponseBody() {
        super();
    }

    /**
     * @return
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    /**
     * @param phoneNumber the phoneNumber to set
     */
    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return
     */
    public String getContent() {
        return content;
    }
    /**
     * @param content the content to set
     */
    public void setContent(final String content) {
        this.content = content;
    }
}
