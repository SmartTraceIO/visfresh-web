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
import com.visfresh.constants.DeviceConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.User;
import com.visfresh.io.DeviceResolver;
import com.visfresh.io.json.DeviceSerializer;
import com.visfresh.services.DeviceCommandService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Device")
@RequestMapping("/rest")
public class DeviceController extends AbstractController implements DeviceConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    @Autowired
    private DeviceDao dao;
    @Autowired
    private DeviceCommandService commandService;
    @Autowired
    private DeviceResolver deviceResolver;

    /**
     * Default constructor.
     */
    public DeviceController() {
        super();
    }
    /**
     * @param authToken authentication token.
     * @param device device.
     * @return ID of saved device.
     */
    @RequestMapping(value = "/saveDevice/{authToken}", method = RequestMethod.POST)
    public JsonObject saveDevice(@PathVariable final String authToken,
            final @RequestBody JsonObject device) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanManageDevices(user);

            final Device d = createSerializer(user).parseDevice(device);
            checkCompanyAccess(user, d);

            d.setCompany(user.getCompany());
            dao.save(d);
            return createSuccessResponse(null);
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
    @RequestMapping(value = "/getDevices/{authToken}", method = RequestMethod.GET)
    public JsonObject getDevices(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex, @RequestParam(required = false) final Integer pageSize) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetDevices(user);

            final DeviceSerializer ser = createSerializer(user);

            final List<Device> devices = dao.findByCompany(user.getCompany(),
                    new Sorting(getDefaultSortOrder()),
                    page,
                    null);

            final int total = dao.getEntityCount(user.getCompany(), null);
            final JsonArray array = new JsonArray();
            for (final Device t : devices) {
                array.add(ser.toJson(t));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            PROPERTY_NAME,
            PROPERTY_IMEI
        };
    }
    /**
     * @param authToken authentication token.
     * @param imei device ID.
     * @return device.
     */
    @RequestMapping(value = "/getDevice/{authToken}", method = RequestMethod.GET)
    public JsonObject getDevice(@PathVariable final String authToken,
            @RequestParam final String imei) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetDevices(user);

            final Device device = dao.findByImei(imei);
            checkCompanyAccess(user, device);

            return createSuccessResponse(createSerializer(user).toJson(device));
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param imei device ID.
     * @return device.
     */
    @RequestMapping(value = "/deleteDevice/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteDevice(@PathVariable final String authToken,
            @RequestParam final String imei) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanManageDevices(user);

            final Device d = dao.findOne(imei);
            checkCompanyAccess(user, d);

            dao.delete(d);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param req shipment.
     * @return status.
     */
    @RequestMapping(value = "/sendCommandToDevice/{authToken}", method = RequestMethod.POST)
    public JsonObject sendCommandToDevice(@PathVariable final String authToken,
            final @RequestBody JsonObject req) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanSendCommandToDevice(user);

            final DeviceCommand cmd = createSerializer(user).parseDeviceCommand(req);
            commandService.sendCommand(cmd);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private DeviceSerializer createSerializer(final User user) {
        final DeviceSerializer deviceSerializer = new DeviceSerializer(user.getTimeZone());
        deviceSerializer.setDeviceResolver(deviceResolver);
        return deviceSerializer;
    }
}
