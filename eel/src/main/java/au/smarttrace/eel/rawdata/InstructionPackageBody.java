/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InstructionPackageBody implements PackageBody {
    public enum InstructionType {
        DeviceCommand(0x01),//0x01: Indicate that instruction content is a device command
        Other(-1);//Other: Reserved

        private final int value;

        /**
         * @param value numeric value.
         */
        private InstructionType(final int value) {
            this.value = value;
        }

        public static InstructionType valueOf(final int value) {
            for (final InstructionType pid : values()) {
                if (pid.value == value) {
                    return pid;
                }
            }
            return Other;
        }
        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    private InstructionType type;
    private long uid;
    private String instruction;

    /**
     * Default constructor.
     */
    public InstructionPackageBody() {
        super();
    }

    /**
     * @param t
     */
    public void setType(final InstructionType t) {
        this.type = t;
    }
    /**
     * @return the type
     */
    public InstructionType getType() {
        return type;
    }
    /**
     * @param uid
     */
    public void setUid(final long uid) {
        this.uid = uid;
    }
    /**
     * @return the uid
     */
    public long getUid() {
        return uid;
    }
    /**
     * @param str
     */
    public void setInstruction(final String str) {
        this.instruction = str;
    }
    /**
     * @return the instruction
     */
    public String getInstruction() {
        return instruction;
    }
}
