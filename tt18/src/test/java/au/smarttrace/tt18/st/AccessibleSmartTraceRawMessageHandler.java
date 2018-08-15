/**
 *
 */
package au.smarttrace.tt18.st;

import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;
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
    public DataWithGsmInfo<DeviceMessage> convert(final RawMessage raw) {
        return super.convert(raw);
    }
}
