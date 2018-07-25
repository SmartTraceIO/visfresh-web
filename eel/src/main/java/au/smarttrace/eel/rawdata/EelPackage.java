/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EelPackage {
    private PackageHeader header;
    private PackageBody body;

    /**
     * Default constructor.
     */
    public EelPackage() {
        super();
    }

    /**
     * @return the header
     */
    public PackageHeader getHeader() {
        return header;
    }
    /**
     * @param header the header to set
     */
    public void setHeader(final PackageHeader header) {
        this.header = header;
    }
    /**
     * @return the body
     */
    public PackageBody getBody() {
        return body;
    }
    /**
     * @param body the body to set
     */
    public void setBody(final PackageBody body) {
        this.body = body;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getHeader() + "";
    }
}
