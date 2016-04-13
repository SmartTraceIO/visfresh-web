/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.io.json.DeviceGroupSerializer;
import com.visfresh.io.json.DeviceSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("DeviceGroup")
@RequestMapping("/rest")
public class DeviceGroupController extends AbstractController implements DeviceGroupConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DeviceGroupController.class);

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
     */
    @RequestMapping(value = "/saveDeviceGroup/{authToken}", method = RequestMethod.POST)
    public JsonObject saveDeviceGroup(@PathVariable final String authToken,
            final @RequestBody JsonObject group) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            DeviceGroup g = createSerializer(user).parseDeviceGroup(group);
            g.setCompany(user.getCompany());

            final DeviceGroup old = dao.findOne(g.getId());
            checkCompanyAccess(user, old);

            g = dao.save(g);
            return createIdResponse("deviceGroupId", g.getId());
        } catch (final Exception e) {
            log.error("Failed to save device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of devices.
     */
    @RequestMapping(value = "/getDeviceGroups/{authToken}", method = RequestMethod.GET)
    public JsonObject getDeviceGroups(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final DeviceGroupSerializer ser = createSerializer(user);

            final List<DeviceGroup> groups = dao.findByCompany(user.getCompany(),
                    new Sorting(getDefaultSortOrder()),
                    page,
                    null);

            final int total = dao.getEntityCount(user.getCompany(), null);
            final JsonArray array = new JsonArray();
            for (final DeviceGroup t : groups) {
                array.add(ser.toJson(t));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get device groups", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param groupName group name.
     * @return list of devices.
     */
    @RequestMapping(value = "/getDevicesOfGroup/{authToken}", method = RequestMethod.GET)
    public JsonObject getDevicesOfGroup(@PathVariable final String authToken,
            @RequestParam(required = false) final String groupName,
            @RequestParam(required = false) final Long groupId
            ) {
        try {
            if (groupName == null && groupId == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "One from 'groupName' or 'groupId' should be specified");
            }

            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            DeviceGroup group = null;
            if (groupId != null) {
                group = dao.findOne(groupId);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + groupId + "' not found");
                }
            } else if (groupName != null){
                group = dao.findByName(groupName);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + groupName + "' not found");
                }
            }

            checkCompanyAccess(user, group);

            final List<Device> devices = deviceDao.findByGroup(group);

            final DeviceSerializer ser = new DeviceSerializer(user.getTimeZone());
            final JsonArray array = new JsonArray();
            for (final Device d : devices) {
                array.add(ser.toJson(d));
            }

            return createListSuccessResponse(array, devices.size());
        } catch (final Exception e) {
            log.error("Failed to get device groups", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param device device IMEI.
     * @return list of devices.
     */
    @RequestMapping(value = "/getGroupsOfDevice/{authToken}", method = RequestMethod.GET)
    public JsonObject getGroupsOfDevice(@PathVariable final String authToken,
            @RequestParam final String device) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final Device d = deviceDao.findOne(device);
            checkCompanyAccess(user, d);

            final List<DeviceGroup> devices = dao.findByDevice(d);

            final DeviceGroupSerializer ser = createSerializer(user);
            final JsonArray array = new JsonArray();
            for (final DeviceGroup g : devices) {
                array.add(ser.toJson(g));
            }

            return createListSuccessResponse(array, devices.size());
        } catch (final Exception e) {
            log.error("Failed to get device groups", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param name device group name.
     * @return JSON response with device group.
     */
    @RequestMapping(value = "/getDeviceGroup/{authToken}", method = RequestMethod.GET)
    public JsonObject getDeviceGroup(@PathVariable final String authToken,
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            DeviceGroup group = null;
            if (id != null) {
                group = dao.findOne(id);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + id + "' not found");
                }
            } else if (name != null){
                group = dao.findByName(name);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + name + "' not found");
                }
            }

            checkCompanyAccess(user, group);

            return createSuccessResponse(createSerializer(user).toJson(group));
        } catch (final Exception e) {
            log.error("Failed to get device group", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param name device group name.
     * @return JSON response with device group.
     */
    @RequestMapping(value = "/deleteDeviceGroup/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteDeviceGroup(@PathVariable final String authToken,
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final Long id) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            DeviceGroup group = null;
            if (id != null) {
                group = dao.findOne(id);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + id + "' not found");
                }
            } else if (name != null){
                group = dao.findByName(name);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + name + "' not found");
                }
            }

            checkCompanyAccess(user, group);
            dao.delete(group);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete device group", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken access token.
     * @param groupName group name.
     * @param device device IMEI code.
     * @return response.
     */
    @RequestMapping(value = "/addDeviceToGroup/{authToken}", method = RequestMethod.GET)
    public JsonObject addDeviceToGroup(@PathVariable final String authToken,
            @RequestParam(required = false) final String groupName,
            @RequestParam(required = false) final Long groupId,
            @RequestParam final String device) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            if (groupName == null && groupId == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "One from 'groupName' or 'groupId' should be specified");
            }

            DeviceGroup group = null;
            if (groupId != null) {
                group = dao.findOne(groupId);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + groupId + "' not found");
                }
            } else if (groupName != null){
                group = dao.findByName(groupName);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + groupName + "' not found");
                }
            }
            checkCompanyAccess(user, group);

            final Device d = deviceDao.findOne(device);
            checkCompanyAccess(user, d);

            dao.addDevice(group, d);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete device group", e);
            return createErrorResponse(e);
        }
    }
    /**
     * Removes device from group.
     * @param authToken access token.
     * @param groupName group name.
     * @param device device IMEI code.
     * @return response.
     */
    @RequestMapping(value = "/removeDeviceFromGroup/{authToken}", method = RequestMethod.GET)
    public JsonObject removeDeviceFromGroup(@PathVariable final String authToken,
            @RequestParam(required = false) final String groupName,
            @RequestParam(required = false) final Long groupId,
            @RequestParam final String device) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            if (groupName == null && groupId == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "One from 'groupName' or 'groupId' should be specified");
            }

            DeviceGroup group = null;
            if (groupId != null) {
                group = dao.findOne(groupId);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + groupId + "' not found");
                }
            } else if (groupName != null){
                group = dao.findByName(groupName);
                if (group == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Group '" + groupName + "' not found");
                }
            }
            checkCompanyAccess(user, group);

            final Device d = deviceDao.findOne(device);
            checkCompanyAccess(user, d);

            dao.removeDevice(group, d);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete device group", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
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
