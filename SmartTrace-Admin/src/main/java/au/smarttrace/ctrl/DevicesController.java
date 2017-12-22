/**
 *
 */
package au.smarttrace.ctrl;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.smarttrace.ApplicationException;
import au.smarttrace.Color;
import au.smarttrace.Device;
import au.smarttrace.Roles;
import au.smarttrace.ctrl.res.ColorDto;
import au.smarttrace.ctrl.res.ListResponse;
import au.smarttrace.device.DevicesService;
import au.smarttrace.device.GetDevicesRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("devices")
@RequestMapping(produces = "application/json;charset=UTF-8")
public class DevicesController {
    @Autowired
    private DevicesService service;

    /**
     * Default constructor.
     */
    public DevicesController() {
        super();
    }

    @RequestMapping(value = "/getDevices", method = RequestMethod.POST)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public ListResponse<Device> getDevices(final @RequestBody GetDevicesRequest req) {
        return service.getDevices(req);
    }
    @RequestMapping(value = "/getDevice", method = RequestMethod.GET)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public Device getDevice(@RequestParam final String imei) {
        return service.getDevice(imei);
    }
    @RequestMapping(value = "/saveDevice", method = RequestMethod.POST)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public String saveDevice(@RequestBody final Device d) {
        service.updateDevice(d);
        return "OK";
    }
    @RequestMapping(value = "/createDevice", method = RequestMethod.POST)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public String createDevice(@RequestBody final Device d) {
        service.createDevice(d);
        return "OK";
    }
    @RequestMapping(value = "/deleteDevice", method = RequestMethod.GET)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public String deleteDevice(@RequestParam final String imei) {
        service.deleteDevice(imei);
        return "OK";
    }
    /**
     * @param device device to move.
     * @param company new company.
     * @return backup device, which have all data associated with moved device.
     * @throws ApplicationException
     */
    @RequestMapping(value = "/moveDevice", method = RequestMethod.GET)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public Device moveDevice(final String device, final Long company) throws ApplicationException {
        return service.moveDevice(device, company);
    }
    /**
     * @return list device colors.
     */
    @RequestMapping(value = "/getDeviceColors", method = RequestMethod.GET)
    public List<ColorDto> getColors() {
        final List<ColorDto> res = new LinkedList<>();
        for (final Color c : service.getAvailableColors()) {
            res.add(new ColorDto(c));
        }
        return res;
    }
}
