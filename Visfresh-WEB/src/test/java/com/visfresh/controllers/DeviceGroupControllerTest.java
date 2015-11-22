/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;

import com.visfresh.controllers.restclient.DeviceGroupRestClient;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceGroupControllerTest extends AbstractRestServiceTest {
    private DeviceGroupRestClient client;
    private DeviceGroupDao dao;
    private DeviceDao deviceDao;

    /**
     * Default constructor.
     */
    public DeviceGroupControllerTest() {
        super();
    }

    @Before
    public void setUp() throws BeansException, AuthenticationException {
        dao = context.getBean(DeviceGroupDao.class);
        deviceDao = context.getBean(DeviceDao.class);

        final User user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
        final String authToken = context.getBean(AuthService.class).login(user.getLogin(),"").getToken();

        client = new DeviceGroupRestClient(user);
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(authToken);
    }

    /**
     * Tests saving of device group.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testSaveDeviceGroup() throws IOException, RestServiceException {
        final DeviceGroup group = new DeviceGroup();
        group.setName("JUnit");
        group.setDescription("JUnit device group");

        client.saveDeviceGroup(group);

        final DeviceGroup saved = dao.findOne(group.getName());

        assertNotNull(saved);
        assertEquals(group.getDescription(), saved.getDescription());
        assertEquals(group.getName(), saved.getName());
        assertNotNull(saved.getCompany());
    }
    /**
     * Tests get of device groups
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testGetDeviceGroups() throws IOException, RestServiceException {
        createGroup("G1", "JUnit device group");
        createGroup("G2", "JUnit device group");

        final List<DeviceGroup> groups = client.getDeviceGroups(null, null);
        assertEquals(2, groups.size());
        assertEquals("G1", groups.get(0).getName());

        //test paging
        assertEquals(1, client.getDeviceGroups(1, 1).size());
        assertEquals(1, client.getDeviceGroups(2, 1).size());
        assertEquals(0, client.getDeviceGroups(3, 1).size());
    }
    /**
     * Tests get device group method
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testGetDeviceGroup() throws IOException, RestServiceException {
        createGroup("G1", "JUnit device group");
        assertNotNull(client.getDeviceGroup("G1"));
    }
    /**
     * Tests deleting of device group.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testDeleteDeviceGroup() throws IOException, RestServiceException {
        final DeviceGroup group = createGroup("G1", "JUnit device group");
        client.deleteDeviceGroup(group.getName());

        assertNull(dao.findOne(group.getName()));
    }
    /**
     * Tests adding device to device group.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testAddDeviceToGroup() throws IOException, RestServiceException {
        final Device d = createDevice("0238947023987", true);
        final DeviceGroup group = createGroup("JUnit", "JUnit device group");

        client.addDeviceToGroup(d.getImei(), group.getName());

        final List<DeviceGroup> devices = dao.findByDevice(d);
        assertEquals(1, devices.size());
        assertEquals(group.getName(), devices.get(0).getName());

        final List<Device> groups = deviceDao.findByGroup(group);
        assertEquals(1, groups.size());
        assertEquals(d.getImei(), groups.get(0).getImei());
    }
    /**
     * Tests removing device from device group.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testRemoveDeviceFromGroup() throws IOException, RestServiceException {
        final Device d = createDevice("0238947023987", true);
        final DeviceGroup group = createGroup("JUnit", "JUnit device group");

        dao.addDevice(group, d);
        client.removeDeviceFromGroup(d.getImei(), group.getName());

        assertEquals(0, dao.findByDevice(d).size());
        assertEquals(0, deviceDao.findByGroup(group).size());
    }
    /**
     * Tests get devices of given group.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testGetGroupDevices() throws IOException, RestServiceException {
        final Device d1 = createDevice("0238947023987", true);
        final Device d2 = createDevice("2398472903879", true);
        final DeviceGroup group = createGroup("JUnit", "JUnit device group");

        dao.addDevice(group, d1);
        dao.addDevice(group, d2);

        assertEquals(2, client.getGroupDevices(group.getName()).size());
    }
    /**
     * @param name
     * @param description
     * @return
     */
    protected DeviceGroup createGroup(final String name, final String description) {
        final DeviceGroup group = new DeviceGroup();
        group.setName(name);
        group.setDescription(description);
        group.setCompany(getCompany());
        dao.save(group);
        return group;
    }
}
