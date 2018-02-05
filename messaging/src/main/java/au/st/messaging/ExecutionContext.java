/**
 *
 */
package au.st.messaging;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ExecutionContext {
    public static enum LoadLevel {
        Full,
        Particularly,
        Nothing
    }

    /**
     * @param l load level for given handler.
     */
    void logActivity(final LoadLevel l);
    /**
     * @return the isStopped
     */
    boolean isStopped();
    /**
     * @param name attribute name.
     * @param value attribute value.
     */
    void setAttribute(String name, Object value);
    /**
     * @param name attribute name.
     * @return attribute value.
     */
    Object getAttribute(String name);
}
