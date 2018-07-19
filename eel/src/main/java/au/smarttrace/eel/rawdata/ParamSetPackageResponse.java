/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ParamSetPackageResponse implements PackageBody {
    private boolean shouldSendNext;

    /**
     * Default constructor.
     */
    public ParamSetPackageResponse() {
        super();
    }

    /**
     * @return
     */
    public boolean shouldSendNext() {
        return shouldSendNext;
    }
    /**
     * @param shouldSendNext the shouldSendNext to set
     */
    public void setShouldSendNext(final boolean shouldSendNext) {
        this.shouldSendNext = shouldSendNext;
    }
}
