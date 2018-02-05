/**
 *
 */
package au.st.messaging;

import au.st.messaging.ExecutionContext.LoadLevel;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DispatcherContext {
    private volatile boolean isStopped;
    private volatile LoadLevel currentLoadLevel;

    /**
     * Default constructor.
     */
    public DispatcherContext() {
        super();
    }

    /**
     * @param l load level for given handler.
     */
    public void logActivity(final LoadLevel l) {
        setCurrentLoadLevel(l);
    }
    /**
     * @return the currentLoadLevel
     */
    public LoadLevel getCurrentLoadLevel() {
        return currentLoadLevel;
    }
    /**
     * @param level the currentLoadLevel to set
     */
    protected void setCurrentLoadLevel(final LoadLevel level) {
        this.currentLoadLevel = level;
    }
    /**
     * @param isStopped the isStopped to set
     */
    protected void setStopped(final boolean isStopped) {
        this.isStopped = isStopped;
    }
    /**
     * @return the isStopped
     */
    public boolean isStopped() {
        return isStopped;
    }
}
