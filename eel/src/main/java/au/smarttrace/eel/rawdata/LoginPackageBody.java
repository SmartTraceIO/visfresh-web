/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LoginPackageBody implements PackageBody {

    public enum Language {
        Chinese(0x00),
        English(0x01),
        Undefined(-1);

        private final int value;

        /**
         * @param value numeric value.
         */
        private Language(final int value) {
            this.value = value;
        }

        public static Language valueOf(final int value) {
            for (final Language lang : values()) {
                if (lang.value == value) {
                    return lang;
                }
            }
            return Undefined;
        }
        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    // IMEI 8 Device IMEI
    private String imei;
    //Language 1 Device language: 0x00 --- Chinese; 0x01 --- English; Other --- Undefined
    private Language language;
    //Timezone 1 Device timezone --- Signed 8 Bits integer (in 15 mins)
    private int timeZone;
    //Sys Ver 2 System version --- Unsigned 16 bits integer (e.g. 0x0205: V2.0.5)
    private int sysVersion;
    //App Ver 2 Application version --- Unsigned 16 bits integer (e.g. 0x0205: V2.0.5)
    private int appVersion;
    //PS Ver 2 Param-set (see note 1) version --- Unsigned 16 bits integer (e.g. 0x0001: V1)
    private int psVersion;
    //PS OSize 2 Param-set original size --- Unsigned 16 bits integer
    private int psOriginSize;
    //PS CSize 2 Param-set compressed size (see note 2) --- Unsigned 16 bits integer
    private int psCompressedSize;
    //PS Sum16 2 Param-set checksum (see note 3) --- Unsigned 16 bits integer
    private int psChecksum;

    /**
     * Default constructor.
     */
    public LoginPackageBody() {
        super();
    }

    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
    }
    /**
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }
    /**
     * @param language the language to set
     */
    public void setLanguage(final Language language) {
        this.language = language;
    }
    /**
     * @return the timeZone
     */
    public int getTimeZone() {
        return timeZone;
    }
    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(final int timeZone) {
        this.timeZone = timeZone;
    }
    /**
     * @return the sysVersion
     */
    public int getSysVersion() {
        return sysVersion;
    }
    /**
     * @param sysVersion the sysVersion to set
     */
    public void setSysVersion(final int sysVersion) {
        this.sysVersion = sysVersion;
    }
    /**
     * @return the appVersion
     */
    public int getAppVersion() {
        return appVersion;
    }
    /**
     * @param appVersion the appVersion to set
     */
    public void setAppVersion(final int appVersion) {
        this.appVersion = appVersion;
    }
    /**
     * @return the psVersion
     */
    public int getPsVersion() {
        return psVersion;
    }
    /**
     * @param psVersion the psVersion to set
     */
    public void setPsVersion(final int psVersion) {
        this.psVersion = psVersion;
    }
    /**
     * @return the psOSize
     */
    public int getPsOriginSize() {
        return psOriginSize;
    }
    /**
     * @param psOSize the psOSize to set
     */
    public void setPsOriginSize(final int psOSize) {
        this.psOriginSize = psOSize;
    }
    /**
     * @return the psCSize
     */
    public int getPsCompressedSize() {
        return psCompressedSize;
    }
    /**
     * @param psCSize the psCSize to set
     */
    public void setPsCompressedSize(final int psCSize) {
        this.psCompressedSize = psCSize;
    }
    /**
     * @return the psSum16
     */
    public int getPsChecksum() {
        return psChecksum;
    }
    /**
     * @param psSum16 the psSum16 to set
     */
    public void setPsChecksum(final int psSum16) {
        this.psChecksum = psSum16;
    }
}
