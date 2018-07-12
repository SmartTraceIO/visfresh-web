/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class HeartbeatPackage extends AbstractPackage {
    private Status status;

    /**
     * Default constructor.
     */
    public HeartbeatPackage() {
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
