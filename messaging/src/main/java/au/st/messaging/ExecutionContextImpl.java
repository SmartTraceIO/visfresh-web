/**
 *
 */
package au.st.messaging;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ExecutionContextImpl implements ExecutionContext {
    private final DispatcherContext root;
    private final Map<String, Object> attributes = new HashMap<>();

    /**
     *
     */
    public ExecutionContextImpl(final DispatcherContext root) {
        super();
        this.root = root;
    }

    /* (non-Javadoc)
     * @see au.st.messaging.ExecutionContext#logActivity(au.st.messaging.ExecutionContext.LoadLevel)
     */
    @Override
    public void logActivity(final LoadLevel l) {
        root.setCurrentLoadLevel(l);
    }
    /* (non-Javadoc)
     * @see au.st.messaging.ExecutionContext#isStopped()
     */
    @Override
    public boolean isStopped() {
        return root.isStopped();
    }

    /* (non-Javadoc)
     * @see au.st.messaging.ExecutionContext#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(final String name, final Object value) {
        attributes.put(name, value);
    }
    /* (non-Javadoc)
     * @see au.st.messaging.ExecutionContext#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }
}
