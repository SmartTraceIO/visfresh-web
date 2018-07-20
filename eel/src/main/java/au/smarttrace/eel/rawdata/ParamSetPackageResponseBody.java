/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ParamSetPackageResponseBody implements PackageBody {
    private boolean shouldSendNext;

    /**
     * Default constructor.
     */
    public ParamSetPackageResponseBody() {
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
