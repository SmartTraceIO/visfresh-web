/**
 *
 */
package com.visfresh.bt04;

import org.junit.Test;

import com.visfresh.Device;
import com.visfresh.DeviceMessage;
import com.visfresh.Location;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Bt04ServiceTest extends Bt04Service {
    /**
     * Default constructor.
     */
    public Bt04ServiceTest() {
        super();
    }

    @Test
    public void testSendSystemMessage() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testNotSendSystemMessageForNotFoundDevice() {
        throw new AssertionFailedError("TODO implement");
    }
    @Test
    public void testAlertForInactiveDevice() {
        throw new AssertionFailedError("TODO implement");
    }

    /* (non-Javadoc)
     * @see com.visfresh.bt04.Bt04Service#sendAlert(java.lang.String, java.lang.String)
     */
    @Override
    protected void sendAlert(final String subject, final String message) {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see com.visfresh.bt04.Bt04Service#sendMessage(com.visfresh.DeviceMessage, com.visfresh.Location)
     */
    @Override
    protected void sendMessage(final DeviceMessage msg, final Location loc) {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see com.visfresh.bt04.Bt04Service#getDeviceByImei(java.lang.String)
     */
    @Override
    protected Device getDeviceByImei(final String imei) {
        // TODO Auto-generated method stub
        return super.getDeviceByImei(imei);
    }
}
