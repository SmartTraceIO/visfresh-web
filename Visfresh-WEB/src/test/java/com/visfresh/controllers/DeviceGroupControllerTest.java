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

import com.visfresh.constants.DeviceGroupConstants;
import com.visfresh.controllers.restclient.DeviceGroupRestClient;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
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
    public void setUp() throws BeansException, RestServiceException {
        dao = context.getBean(DeviceGroupDao.class);
        deviceDao = context.getBean(DeviceDao.class);

        final User user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
        final String authToken = context.getBean(AuthService.class).login(user.getEmail(),"", "junit").getToken();

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

        final Long id = client.saveDeviceGroup(group);

        final DeviceGroup saved = dao.findOne(id);

        assertNotNull(saved);
        assertEquals(group.getDescription(), saved.getDescription());
        assertEquals(group.getName(), saved.getName());
        assertNotNull(saved.getCompany());
    }
    /**
     * Tests saving of device group.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testUpdateDeviceGroup() throws IOException, RestServiceException {
        final DeviceGroup group = new DeviceGroup();
        group.setName("JUnit");
        group.setDescription("JUnit device group");

        final Long id = client.saveDeviceGroup(group);
        DeviceGroup saved = dao.findOne(id);

        //do update group
        final String updatedName = "Updated Name";
        final String updatedDescription = "Updated Group Description";

        group.setId(id);
        group.setName(updatedName);
        group.setDescription(updatedDescription);
        client.saveDeviceGroup(group);

        saved = dao.findOne(id);

        assertNotNull(saved);
        assertEquals(updatedDescription, saved.getDescription());
        assertEquals(updatedName, saved.getName());
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
     * Tests get of device groups
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testGetDeviceGroupsSortedById() throws IOException, RestServiceException {
        final DeviceGroup g1 = createGroup("2", "2");
        createGroup("1", "1");
        final DeviceGroup g3 = createGroup("3", "3");

        //test paging
        List<DeviceGroup> groups;
        groups = client.getDeviceGroups(1, 100, DeviceGroupConstants.PROPERTY_ID, "asc");
        assertEquals(g1.getId(), groups.get(0).getId());

        groups = client.getDeviceGroups(1, 100, DeviceGroupConstants.PROPERTY_ID, "desc");
        assertEquals(g3.getId(), groups.get(0).getId());
    }
    /**
     * Tests get of device groups
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testGetDeviceGroupsSortedByName() throws IOException, RestServiceException {
        final DeviceGroup g1 = createGroup("3", "1");
        final DeviceGroup g2 = createGroup("1", "2");
        createGroup("2", "3");

        //test paging
        List<DeviceGroup> groups;
        groups = client.getDeviceGroups(1, 100, DeviceGroupConstants.PROPERTY_NAME, "asc");
        assertEquals(g2.getId(), groups.get(0).getId());

        groups = client.getDeviceGroups(1, 100, DeviceGroupConstants.PROPERTY_NAME, "desc");
        assertEquals(g1.getId(), groups.get(0).getId());
    }
    /**
     * Tests get of device groups
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testGetDeviceGroupsSortedByDescription() throws IOException, RestServiceException {
        final DeviceGroup g1 = createGroup("1", "3");
        final DeviceGroup g2 = createGroup("2", "1");
        createGroup("3", "2");

        //test paging
        List<DeviceGroup> groups;
        groups = client.getDeviceGroups(1, 100, DeviceGroupConstants.PROPERTY_DESCRIPTION, "asc");
        assertEquals(g2.getId(), groups.get(0).getId());

        groups = client.getDeviceGroups(1, 100, DeviceGroupConstants.PROPERTY_DESCRIPTION, "desc");
        assertEquals(g1.getId(), groups.get(0).getId());
    }
    /**
     * Tests get device group method
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testGetDeviceGroup() throws IOException, RestServiceException {
        final DeviceGroup dg = createGroup("G1", "JUnit device group");

        assertNotNull(client.getDeviceGroup("G1"));

        final DeviceGroup g = client.getDeviceGroup(dg.getId());
        assertNotNull(g);

        //check correct values
        assertEquals(dg.getId(), g.getId());
        assertEquals(dg.getName(), g.getName());
        assertEquals(dg.getDescription(), g.getDescription());
    }
    /**
     * Tests deleting of device group.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testDeleteDeviceGroup() throws IOException, RestServiceException {
        DeviceGroup group = createGroup("G1", "JUnit device group");
        client.deleteDeviceGroup(group.getName());
        assertNull(dao.findOne(group.getId()));

        //delete by grou ID.
        group = createGroup("G1", "JUnit device group");
        client.deleteDeviceGroup(group.getId());
        assertNull(dao.findOne(group.getId()));
    }
    /**
     * Tests adding device to device group.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testAddDeviceToGroup() throws IOException, RestServiceException {
        final Device d = createDevice("0238947023987", true);
        final DeviceGroup gr1 = createGroup("JUnit-1", "JUnit device group");
        final DeviceGroup gr2 = createGroup("JUnit-2", "JUnit device group");

        client.addDeviceToGroup(d.getImei(), gr1.getName());
        client.addDeviceToGroup(d.getImei(), gr2.getId());

        final List<DeviceGroup> groups = dao.findByDevice(d);
        assertEquals(2, groups.size());
        assertEquals(gr1.getName(), groups.get(0).getName());

        List<Device> devices = deviceDao.findByGroup(gr1);
        assertEquals(1, devices.size());
        assertEquals(d.getImei(), devices.get(0).getImei());

        devices = deviceDao.findByGroup(gr2);
        assertEquals(1, devices.size());
        assertEquals(d.getImei(), devices.get(0).getImei());
    }
    /**
     * Tests removing device from device group.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testRemoveDeviceFromGroup() throws IOException, RestServiceException {
        final Device d1 = createDevice("0238947023987", true);
        final Device d2 = createDevice("2938799889897", true);
        final DeviceGroup group = createGroup("JUnit", "JUnit device group");

        dao.addDevice(group, d1);
        dao.addDevice(group, d2);

        client.removeDeviceFromGroup(d1.getImei(), group.getName());
        assertEquals(0, dao.findByDevice(d1).size());
        assertEquals(1, deviceDao.findByGroup(group).size());


        client.removeDeviceFromGroup(d2.getImei(), group.getId());
        assertEquals(0, dao.findByDevice(d2).size());
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

        assertEquals(2, client.getDevicesOfGroup(group.getName()).size());
        assertEquals(2, client.getDevicesOfGroup(group.getId()).size());
    }
    /**
     * Tests get group of given device.
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testGetGroupsOfDevice() throws IOException, RestServiceException {
        final Device device = createDevice("0238947023987", true);
        final DeviceGroup g1 = createGroup("JUnit-1", "JUnit device group");
        final DeviceGroup g2 = createGroup("JUnit-2", "JUnit device group");

        dao.addDevice(g1, device);
        dao.addDevice(g2, device);

        assertEquals(2, client.getGroupsOfDevice(device.getImei()).size());
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
