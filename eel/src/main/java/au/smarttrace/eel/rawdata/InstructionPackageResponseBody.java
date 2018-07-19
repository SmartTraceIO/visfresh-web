/**
 *
 */
package au.smarttrace.eel.rawdata;

import au.smarttrace.eel.rawdata.InstructionPackageBody.InstructionType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InstructionPackageResponseBody implements PackageBody {
    private InstructionType type;
    private int uid;
    private String instructionResult;

    /**
     * Default constructor.
     */
    public InstructionPackageResponseBody() {
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
    public void setUid(final int uid) {
        this.uid = uid;
    }
    /**
     * @return the uid
     */
    public int getUid() {
        return uid;
    }

    /**
     * @param result
     */
    public void setInstructionResult(final String result) {
        this.instructionResult = result;
    }
    /**
     * @return the instructionResult
     */
    public String getInstructionResult() {
        return instructionResult;
    }
}
