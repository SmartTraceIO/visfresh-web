/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceGroupConstants;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.Page;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.json.DeviceGroupSerializer;
import com.visfresh.io.json.DeviceSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("DeviceGroup")
@RequestMapping("/rest")
public class DeviceGroupController extends AbstractController implements DeviceGroupConstants {
    @Autowired
    private DeviceGroupDao dao;
    @Autowired
    private DeviceDao deviceDao;

    /**
     * Default constructor.
     */
    public DeviceGroupController() {
        super();
    }
    /**
     * @param authToken authentication token.
     * @param group device group.
     * @return success response.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/saveDeviceGroup", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveDeviceGroup(
            final @RequestBody JsonObject group) throws RestServiceException {
            final User user = getLoggedInUser();
            DeviceGroup g = createSerializer(user).parseDeviceGroup(group);
            g.setCompany(user.getCompanyId());

            final DeviceGroup old = dao.findOne(g.getId());
            checkCompanyAccess(user, old);

            g = dao.save(g);
            return createIdResponse("deviceGroupId", g.getId());
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of devices.
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getDeviceGroups", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getDeviceGroups(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        //check logged in.
        final User user = getLoggedInUser();
        final DeviceGroupSerializer ser = createSerializer(user);

        final List<DeviceGroup> groups = dao.findByCompany(user.getCompanyId(),
                createSorting(sc, so, getDefaultSortOrder(), 1),
                page,
                null);

        final int total = dao.getEntityCount(user.getCompanyId(), null);
        final JsonArray array = new JsonArray();
        for (final DeviceGroup t : groups) {
            array.add(ser.toJson(t));
        }

        return createListSuccessResponse(array, total);
    }
    /**
     * @param authToken authentication token.
     * @param groupName group name.
     * @return list of devices.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getDevicesOfGroup", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getDevicesOfGroup(
            @RequestParam(required = false) final String groupName,
            @RequestParam(required = false) final Long groupId
            ) throws RestServiceException {
        if (groupName == null && groupId == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "One from 'groupName' or 'groupId' should be specified");
        }

        //check logged in.
        final User user = getLoggedInUser();
        DeviceGroup group = null;
        if (groupId != null) {
            group = dao.findOne(groupId);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + groupId + "' not found");
            }
        } else if (groupName != null){
            group = dao.findByName(groupName);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + groupName + "' not found");
            }
        }

        checkCompanyAccess(user, group);

        final List<Device> devices = deviceDao.findByGroup(group);

        final DeviceSerializer ser = new DeviceSerializer(user);
        final JsonArray array = new JsonArray();
        for (final Device d : devices) {
            array.add(ser.toJson(d));
        }

        return createListSuccessResponse(array, devices.size());
    }
    /**
     * @param authToken authentication token.
     * @param device device IMEI.
     * @return list of devices.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getGroupsOfDevice", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getGroupsOfDevice(
            @RequestParam final String device) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final Device d = deviceDao.findOne(device);
        checkCompanyAccess(user, d);

        final List<DeviceGroup> devices = dao.findByDevice(d);

        final DeviceGroupSerializer ser = createSerializer(user);
        final JsonArray array = new JsonArray();
        for (final DeviceGroup g : devices) {
            array.add(ser.toJson(g));
        }

        return createListSuccessResponse(array, devices.size());
    }
    /**
     * @param authToken authentication token.
     * @param name device group name.
     * @return JSON response with device group.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getDeviceGroup", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getDeviceGroup(
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final Long id) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        DeviceGroup group = null;
        if (id != null) {
            group = dao.findOne(id);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + id + "' not found");
            }
        } else if (name != null){
            group = dao.findByName(name);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + name + "' not found");
            }
        }

        checkCompanyAccess(user, group);

        return createSuccessResponse(createSerializer(user).toJson(group));
    }
    /**
     * @param authToken authentication token.
     * @param name device group name.
     * @return JSON response with device group.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/deleteDeviceGroup", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject deleteDeviceGroup(
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final Long id) throws RestServiceException {
            //check logged in.
        final User user = getLoggedInUser();
        DeviceGroup group = null;
        if (id != null) {
            group = dao.findOne(id);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + id + "' not found");
            }
        } else if (name != null){
            group = dao.findByName(name);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + name + "' not found");
            }
        }

        checkCompanyAccess(user, group);
        dao.delete(group);
        return createSuccessResponse(null);
    }
    /**
     * @param authToken access token.
     * @param groupName group name.
     * @param device device IMEI code.
     * @return response.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/addDeviceToGroup", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject addDeviceToGroup(
            @RequestParam(required = false) final String groupName,
            @RequestParam(required = false) final Long groupId,
            @RequestParam final String device) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        if (groupName == null && groupId == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "One from 'groupName' or 'groupId' should be specified");
        }

        DeviceGroup group = null;
        if (groupId != null) {
            group = dao.findOne(groupId);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + groupId + "' not found");
            }
        } else if (groupName != null){
            group = dao.findByName(groupName);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + groupName + "' not found");
            }
        }
        checkCompanyAccess(user, group);

        final Device d = deviceDao.findOne(device);
        checkCompanyAccess(user, d);

        dao.addDevice(group, d);
        return createSuccessResponse(null);
    }
    /**
     * Removes device from group.
     * @param authToken access token.
     * @param groupName group name.
     * @param device device IMEI code.
     * @return response.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/removeDeviceFromGroup", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject removeDeviceFromGroup(
            @RequestParam(required = false) final String groupName,
            @RequestParam(required = false) final Long groupId,
            @RequestParam final String device) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        if (groupName == null && groupId == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "One from 'groupName' or 'groupId' should be specified");
        }

        DeviceGroup group = null;
        if (groupId != null) {
            group = dao.findOne(groupId);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + groupId + "' not found");
            }
        } else if (groupName != null){
            group = dao.findByName(groupName);
            if (group == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Group '" + groupName + "' not found");
            }
        }
        checkCompanyAccess(user, group);

        final Device d = deviceDao.findOne(device);
        checkCompanyAccess(user, d);

        dao.removeDevice(group, d);
        return createSuccessResponse(null);
    }
    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            PROPERTY_ID,
            PROPERTY_NAME,
            PROPERTY_DESCRIPTION
        };
    }
    /**
     * @param user the user.
     * @return device group serializer.
     */
    private DeviceGroupSerializer createSerializer(final User user) {
        return new DeviceGroupSerializer(user);
    }
}
