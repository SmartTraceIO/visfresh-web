/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PackageHeader {
    public enum PackageIdentifier {
        Login(0x01),
        Heartbeat(0x03),
        Location(0x12),
        Warning(0x14),
        Message(0x16),
        ParamSet(0x1B),
        Instruction(0x80),
        Broadcast(0x81),
        Undefined(-1);

        private final int value;

        /**
         * @param value numeric value.
         */
        private PackageIdentifier(final int value) {
            this.value = value;
        }

        public static PackageIdentifier valueOf(final int value) {
            for (final PackageIdentifier pid : values()) {
                if (pid.value == value) {
                    return pid;
                }
            }

            throw new RuntimeException("Unexpected package identifier " + value);
        }
        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    // Mark 2 0x67 0x67
    private String mark;
    // PID 1 Package identifier
    private PackageIdentifier pid;
    // Size 2 Package size from next byte to end --- Unsigned 16 bits integer
    private int size;
    //Sequence 2 Package sequence number --- Unsigned 16 bits integer
    private int sequence;
    //only for undefined packages.
    private int pidOriginValue;

    /**
     * Default constructor.
     */
    public PackageHeader() {
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
     * @return the pid
     */
    public PackageIdentifier getPid() {
        return pid;
    }
    /**
     * @param pid the pid to set
     */
    public void setPid(final PackageIdentifier pid) {
        this.pid = pid;
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
     * @return the sequence
     */
    public int getSequence() {
        return sequence;
    }
    /**
     * @param sequence the sequence to set
     */
    public void setSequence(final int sequence) {
        this.sequence = sequence;
    }
    public static void main(final String[] args) {
        System.out.println(Integer.toHexString(26));
    }
    /**
     * @return the pidOriginValue
     */
    public int getPidOriginValue() {
        return pidOriginValue;
    }
    /**
     * @param pidOriginValue the pidOriginValue to set
     */
    public void setPidOriginValue(final int pidOriginValue) {
        this.pidOriginValue = pidOriginValue;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getPid().name());
        if (getPid() == PackageIdentifier.Undefined) {
            sb.append(" (originValue: 0x").append(Integer.toHexString(getPidOriginValue())).append(')');
        }
        return sb.toString();
    }
}
