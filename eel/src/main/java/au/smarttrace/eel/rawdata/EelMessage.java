/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EelMessage {
    // header

    // Mark 2 'EL'
    private String mark;
    // Size 2 Datagram size from next byte to end
    private int size;
    // Checksum 2 Datagram checksum (see note 1) from next byte to end
    private int checkSumm;
    //IMEI 8 Device IMEI
    private String imei;

    //package.
    private final List<EelPackage> packages = new LinkedList<>();

    /**
     * Default constructor.
     */
    public EelMessage() {
        super();
    }

    /**
     * @return the mark
     */
    public String getMark() {
        return mark;
    }
    /**
     * @param mark the mark to set
     */
    public void setMark(final String mark) {
        this.mark = mark;
    }
    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }
    /**
     * @param size the size to set
     */
    public void setSize(final int size) {
        this.size = size;
    }
    /**
     * @return the checkSumm
     */
    public int getCheckSumm() {
        return checkSumm;
    }
    /**
     * @param checkSumm the checkSumm to set
     */
    public void setCheckSumm(final int checkSumm) {
        this.checkSumm = checkSumm;
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
     * @return the packages
     */
    public List<EelPackage> getPackages() {
        return packages;
    }
}
