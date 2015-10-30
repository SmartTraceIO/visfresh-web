/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;
import com.visfresh.services.RestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller("Device")
@RequestMapping("/rest")
public class DeviceController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);
    /**
     * REST service.
     */
    @Autowired
    private RestService restService;

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
    public @ResponseBody String saveDevice(@PathVariable final String authToken,
            final @RequestBody String device) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveDevice(user);

            restService.saveDevice(user.getCompany(), getSerializer().parseDevice(getJSonObject(device)));
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
    public @ResponseBody String getDevices(@PathVariable final String authToken,
            @RequestParam final int pageIndex, @RequestParam final int pageSize) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetDevices(user);

            final EntityJSonSerializer ser = getSerializer();

            final List<Device> devices = getPage(restService.getDevices(user.getCompany()), pageIndex, pageSize);
            final JsonArray array = new JsonArray();
            for (final Device t : devices) {
                array.add(ser.toJson(t));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get devices", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param deviceId device ID.
     * @return device.
     */
    @RequestMapping(value = "/getDevice/{authToken}", method = RequestMethod.GET)
    public @ResponseBody String getDevice(@PathVariable final String authToken,
            @RequestParam final String deviceId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetDevices(user);

            final Device device = restService.getDevice(user.getCompany(), deviceId);
            return createSuccessResponse(getSerializer().toJson(device));
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
    public @ResponseBody String sendCommandToDevice(@PathVariable final String authToken,
            final @RequestBody String req) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanSendCommandToDevice(user);

            final DeviceCommand cmd = getSerializer().parseDeviceCommand(getJSonObject(req));
            restService.sendCommandToDevice(cmd);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to send command to device", e);
            return createErrorResponse(e);
        }
    }
}
