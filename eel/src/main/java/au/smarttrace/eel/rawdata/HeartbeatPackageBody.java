/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class HeartbeatPackageBody implements PackageBody {
    private Status status;

    /**
     * Default constructor.
     */
    public HeartbeatPackageBody() {
        super();
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(final Status status) {
        this.status = status;
    }
}
