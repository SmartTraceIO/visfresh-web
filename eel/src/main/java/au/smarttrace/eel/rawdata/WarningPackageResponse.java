/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class WarningPackageResponse implements PackageBody {
    private String content;

    /**
     * Default constructor.
     */
    public WarningPackageResponse() {
        super();
    }

    /**
     * @return warning content.
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
