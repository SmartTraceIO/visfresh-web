/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ParamSetPackage extends AbstractPackage {
    private int version;
    private int originalSize;
    private int compressedSize;
    private int shecksum;
    private int offset;
    private byte[] data;

    /**
     * Default constructor.
     */
    public ParamSetPackage() {
        super();
    }

    /**
     * @param v
     */
    public void setVersion(final int v) {
        this.version = v;
    }
    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }
    /**
     * @param size
     */
    public void setOriginalSize(final int size) {
        this.originalSize = size;
    }
    /**
     * @return the originalSize
     */
    public int getOriginalSize() {
        return originalSize;
    }
    /**
     * @param size
     */
    public void setCompressedSize(final int size) {
        this.compressedSize = size;
    }
    /**
     * @return the compressedSize
     */
    public int getCompressedSize() {
        return compressedSize;
    }
    /**
     * @param summ
     */
    public void setChecksum(final int summ) {
        this.shecksum = summ;
    }
    /**
     * @return the shecksum
     */
    public int getShecksum() {
        return shecksum;
    }
    /**
     * @param offset
     */
    public void setOffset(final int offset) {
        this.offset = offset;
    }
    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }
    /**
     * @param data
     */
    public void setData(final byte[] data) {
        this.data = data;
    }
    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }
}
