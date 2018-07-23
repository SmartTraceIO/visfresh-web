/**
 *
 */
package au.smarttrace.eel.db;

import au.smarttrace.eel.DeviceMessage;
import au.smarttrace.eel.rawdata.BeaconData;
import au.smarttrace.eel.rawdata.DevicePosition;
import au.smarttrace.eel.service.EelMessageHandlerImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AccessibleEelMessageHandler extends EelMessageHandlerImpl {
    /* (non-Javadoc)
     * @see au.smarttrace.eel.service.EelMessageHandlerImpl#createDeviceMessage(au.smarttrace.eel.rawdata.BeaconData, au.smarttrace.eel.rawdata.DevicePosition)
     */
    @Override
    protected DeviceMessage createDeviceMessage(final BeaconData bs, final DevicePosition location) {
        return super.createDeviceMessage(bs, location);
    }
}
