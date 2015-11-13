/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.DeviceRestClient;
import com.visfresh.dao.DeviceDao;
import com.visfresh.entities.Device;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceControllerTest extends AbstractRestServiceTest {
    private DeviceDao dao;
    private DeviceRestClient client = new DeviceRestClient(UTC);

    /**
     * Default constructor.
     */
    public DeviceControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(DeviceDao.class);
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
    }
    //@RequestMapping(value = "/saveDevice/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveDevice(@PathVariable final String authToken,
    //        final @RequestBody String alert) {
    @Test
    public void testSaveDevice() throws RestServiceException, IOException {
        final Device p = createDevice("0239487043987", false);
        client.saveDevice(p);
        assertNotNull(dao.findOne(p.getImei()));
    }
    @Test
    public void testGetDevice() throws IOException, RestServiceException {
        final Device ap = createDevice("0239487043987", true);
        assertNotNull(client.getDevice(ap.getId()));
    }
    @Test
    public void testDeleteDevice() throws RestServiceException, IOException {
        final Device p = createDevice("0239487043987", true);
        client.deleteDevice(p);
        assertNull(dao.findOne(p.getId()));
    }
    //@RequestMapping(value = "/getDevices/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getDevices(@PathVariable final String authToken) {
    @Test
    public void testGetDevices() throws RestServiceException, IOException {
        createDevice("0239487043987", true);
        createDevice("0239487043222", true);

        assertEquals(2, client.getDevices(null, null).size());
        assertEquals(1, client.getDevices(1, 1).size());
        assertEquals(1, client.getDevices(2, 1).size());
        assertEquals(0, client.getDevices(3, 1).size());
    }
    @Test
    public void testSendCommandToDevice() throws RestServiceException, IOException {
        final Device device = createDevice("089723409857032498", true);
        client.saveDevice(device);

        client.sendCommandToDevice(device, "shutdown");
    }
}
