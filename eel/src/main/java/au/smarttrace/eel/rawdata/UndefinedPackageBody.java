/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UndefinedPackageBody implements PackageBody {
    private byte[] rawData;

    /**
     * Default constructor.
     */
    public UndefinedPackageBody() {
        super();
    }
    /**
     * @return the rawData
     */
    public byte[] getRawData() {
        return rawData;
    }
    /**
     * @param rawData the rawData to set
     */
    public void setRawData(final byte[] rawData) {
        this.rawData = rawData;
    }
}
