/**
 *
 */
package au.smarttrace.eel.db;

import au.smarttrace.eel.rawdata.BeaconData;
import au.smarttrace.eel.service.EelMessageHandlerImpl;
import au.smarttrace.geolocation.DeviceMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AccessibleEelMessageHandler extends EelMessageHandlerImpl {
    /* (non-Javadoc)
     * @see au.smarttrace.eel.service.EelMessageHandlerImpl#createDeviceMessage(au.smarttrace.eel.rawdata.BeaconData, au.smarttrace.eel.rawdata.DevicePosition)
     */
    @Override
    public DeviceMessage createDeviceMessage(final BeaconData bs) {
        return super.createDeviceMessage(bs);
    }
}
