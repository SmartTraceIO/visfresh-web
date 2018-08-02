/**
 *
 */
package com.visfresh.entities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceTest {
    /**
     * Default constructor.
     */
    public DeviceTest() {
        super();
    }

    @Test
    public void testGetSerialNumber() {
        final String imei = "2034987098279879";
        assertEquals("827987", Device.getSerialNumber(DeviceModel.SmartTrace, imei));
        assertEquals(imei, Device.getSerialNumber(DeviceModel.STB1, imei));
    }
}
