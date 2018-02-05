/**
 *
 */
package au.st.messaging;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageGroupException extends SystemMessageException {
    private static final long serialVersionUID = 1425496469726193684L;
    private boolean shouldPauseGroup;

    /**
     * Default constructor.
     */
    public SystemMessageGroupException() {
        super();
    }

    /**
     * @param message message.
     * @param cause origin exception.
     */
    public SystemMessageGroupException(final String message, final Throwable cause) {
        super(message, cause);
    }
    /**
     * @param message message.
     */
    public SystemMessageGroupException(final String message) {
        super(message);
    }
    /**
     * @param cause origin exception.
     */
    public SystemMessageGroupException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param shouldPauseGroup the shouldPauseGroup to set
     */
    public void setShouldPauseGroup(final boolean shouldPauseGroup) {
        this.shouldPauseGroup = shouldPauseGroup;
    }
    /**
     * @return the shouldPauseGroup
     */
    public boolean isShouldPauseGroup() {
        return shouldPauseGroup;
    }
}
