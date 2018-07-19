/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LoginPackageResponse implements PackageBody {
    private boolean shouldUploadParamSetImmediately;
    private boolean shouldUploadParamSetInFuture;
    private int protocolVersion;
    private long currentTimeUtc;

    /**
     * @return
     */
    public boolean shouldUploadParamSetImmediately() {
        return shouldUploadParamSetImmediately;
    }
    /**
     * @param shouldUploadParamSetImmediately the shouldUploadParamSetImmediately to set
     */
    public void setShouldUploadParamSetImmediately(final boolean shouldUploadParamSetImmediately) {
        this.shouldUploadParamSetImmediately = shouldUploadParamSetImmediately;
    }
    /**
     * @return
     */
    public boolean shouldUploadParamSetInFuture() {
        return shouldUploadParamSetInFuture;
    }
    /**
     * @param shouldUploadParamSetInFuture the shouldUploadParamSetInFuture to set
     */
    public void setShouldUploadParamSetInFuture(final boolean shouldUploadParamSetInFuture) {
        this.shouldUploadParamSetInFuture = shouldUploadParamSetInFuture;
    }
    /**
     * @return
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }
    /**
     * @param protocolVersion the protocolVersion to set
     */
    public void setProtocolVersion(final int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    /**
     * @return
     */
    public long getCurrentTimeUtc() {
        return currentTimeUtc;
    }
    /**
     * @param currentTimeUtc the currentTimeUtc to set
     */
    public void setCurrentTimeUtc(final long currentTimeUtc) {
        this.currentTimeUtc = currentTimeUtc;
    }
}
