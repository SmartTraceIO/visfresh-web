/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceControllerTest extends AbstractRestServiceTest {
    /**
     * Default constructor.
     */
    public DeviceControllerTest() {
        super();
    }
    //@RequestMapping(value = "/saveDevice/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveDevice(@PathVariable final String authToken,
    //        final @RequestBody String alert) {
    @Test
    public void testSaveDevice() throws RestServiceException, IOException {
        final Device p = createDevice("0239487043987", false);
        facade.saveDevice(p);
        assertNotNull(getRestService().devices.get(p.getImei()));
    }
    @Test
    public void testGetDevice() throws IOException, RestServiceException {
        final Device ap = createDevice("0239487043987", true);
        assertNotNull(facade.getDevice(ap.getId()));
    }
    @Test
    public void testDeleteDevice() throws RestServiceException, IOException {
        final Device p = createDevice("0239487043987", true);
        facade.deleteDevice(p);
        assertNull(getRestService().getDevice(getCompany(), p.getId()));
    }
    //@RequestMapping(value = "/getDevices/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getDevices(@PathVariable final String authToken) {
    @Test
    public void testGetDevices() throws RestServiceException, IOException {
        createDevice("0239487043987", true);
        createDevice("0239487043222", true);

        assertEquals(2, facade.getDevices(1, 10000).size());
        assertEquals(1, facade.getDevices(1, 1).size());
        assertEquals(1, facade.getDevices(2, 1).size());
        assertEquals(0, facade.getDevices(3, 1).size());
    }
}
