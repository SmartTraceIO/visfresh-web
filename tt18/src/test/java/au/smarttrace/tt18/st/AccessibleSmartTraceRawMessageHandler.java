/**
 *
 */
package au.smarttrace.tt18.st;

import au.smarttrace.tt18.RawMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AccessibleSmartTraceRawMessageHandler extends SmartTraceRawMessageHandler {
    /**
     * Default constructor.
     */
    public AccessibleSmartTraceRawMessageHandler() {
        super();
    }

    /* (non-Javadoc)
     * @see au.smarttrace.tt18.st.SmartTraceRawMessageHandler#convert(au.smarttrace.tt18.RawMessage)
     */
    @Override
    public DeviceMessage convert(final RawMessage msg) {
        return super.convert(msg);
    }
}
