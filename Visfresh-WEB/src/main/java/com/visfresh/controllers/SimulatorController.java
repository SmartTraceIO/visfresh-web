/**
 *
 */
package com.visfresh.controllers;

import java.text.DateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.SimulatorDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Role;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.Simulator;
import com.visfresh.entities.User;
import com.visfresh.io.SimulatorDto;
import com.visfresh.io.StartSimulatorRequest;
import com.visfresh.io.json.SimulatorSerializer;
import com.visfresh.services.SimulatorService;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Simulator")
@RequestMapping("/rest")
public class SimulatorController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SimulatorController.class);
    @Autowired
    private UserDao userDao;
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private SimulatorDao dao;
    @Autowired
    private SimulatorService service;
    @Autowired
    private ShipmentTemplateDao shipmentTemplateDao;

    /**
     * Default constructor.
     */
    public SimulatorController() {
        super();
    }

    @RequestMapping(value = "/saveSimulator/{authToken}", method = RequestMethod.POST)
    public JsonObject saveSimulator(@PathVariable final String authToken,
            final @RequestBody JsonObject json) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.SmartTraceAdmin);

            final SimulatorDto dto = createSerializer(user).parseSimulator(json);

            //find source device.
            final Device d = deviceDao.findByImei(dto.getSourceDevice());
            if (d == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA, "Source device "
                        + dto.getSourceDevice() + " not found");
            }

            //find user
            final User u = userDao.findByEmail(dto.getUser());
            if (u == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA, "User "
                        + dto.getUser() + " not found");
            }
            final ShipmentTemplate tpl = shipmentTemplateDao.findOne(dto.getAutoStart());
            if (tpl != null && !u.getCompany().getId().equals(tpl.getCompany().getId())) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Shipment template should be from same company as user");
            }

            final String simulatorImei = generateImei(u.getId());
            Device simulatorDevice = deviceDao.findByImei(simulatorImei);
            if (simulatorDevice == null) {
                log.debug("Create simulator device '" + simulatorImei + "' for user " + u.getEmail());
                simulatorDevice = createSimulatorDevice(simulatorImei, u);
            }

            //save simulator
            final Simulator sim = new Simulator();
            sim.setUser(u);
            sim.setSource(d);
            sim.setTarget(simulatorDevice);

            dao.save(sim);

            //set autostart template to virtual device.
            final Long autoStart = tpl == null ? null : tpl.getId();
            final Long oldAutoStart = simulatorDevice.getAutostartTemplateId();

            if (!equals(autoStart, oldAutoStart)) {
                simulatorDevice.setAutostartTemplateId(autoStart);
                deviceDao.save(simulatorDevice);
            }

            return createIdResponse("simulatorDevice", simulatorDevice.getImei());
        } catch (final Exception e) {
            log.error("Failed to save simulator", e);
            return createErrorResponse(e);
        }
    }

    @RequestMapping(value = "/getSimulator/{authToken}", method = RequestMethod.GET)
    public JsonObject getSimulator(@PathVariable final String authToken,
            final @RequestParam(required = false) String user) {
        try {
            final User currentUser = getLoggedInUser(authToken);

            //find user
            final User u;
            if (user != null) {
                u = userDao.findByEmail(user);
                if (u == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA, "User "
                            + user + " not found");
                }

                if (!u.getId().equals(currentUser.getId())) {
                    checkAccess(currentUser, Role.SmartTraceAdmin);
                }
            } else {
                u = currentUser;
            }

            JsonObject response = null;
            final SimulatorDto dto = dao.findSimulatorDto(u);
            if (dto != null) {
                response = createSerializer(currentUser).toJson(dto);
            }

            return createSuccessResponse(response);
        } catch (final Exception e) {
            log.error("Failed to get simulator", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/deleteSimulator/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteSimulator(@PathVariable final String authToken,
            final @RequestParam String user) {
        try {
            final User currentUser = getLoggedInUser(authToken);
            checkAccess(currentUser, Role.SmartTraceAdmin);

            //find user
            final User u = userDao.findByEmail(user);
            if (u == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA, "User "
                        + user + " not found");
            }

            service.stopSimulator(u);
            dao.delete(u);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete simulator", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/startSimulator/{authToken}", method = RequestMethod.POST)
    public JsonObject startSimulator(@PathVariable final String authToken,
            final @RequestBody JsonObject json) {
        try {
            final User user = getLoggedInUser(authToken);
            final SimulatorSerializer ser = createSerializer(user);

            final StartSimulatorRequest req = ser.parseStartRequest(json);
            User u = user;
            if (req.getUser() != null && !req.getUser().equals(user.getEmail())) {
                checkAccess(user, Role.SmartTraceAdmin);
                u = userDao.findByEmail(req.getUser());
                if (u == null) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "User not found " + req.getUser());
                }
            }

            //check already started
            final SimulatorDto sim = dao.findSimulatorDto(u);
            if (sim == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Simulator not found for user " + u.getEmail());
            }
            if (sim.isStarted()) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Simulator already started for user " + u.getEmail());
            }

            //check correct velosity
            if (req.getVelosity() < 1) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Invalid velosity " + req.getVelosity() + " should start from 1");
            }

            final DateFormat fmt = DateTimeUtils.createIsoFormat(user);

            final Date startDate = req.getStartDate() == null ? null : fmt.parse(req.getStartDate());
            final Date endDate = req.getEndDate() == null ? null : fmt.parse(req.getEndDate());

            service.startSimulator(u, startDate, endDate, req.getVelosity());
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete simulator", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/stopSimulator/{authToken}", method = RequestMethod.GET)
    public JsonObject stopSimulator(@PathVariable final String authToken) {
        try {
            final User user = getLoggedInUser(authToken);
            service.stopSimulator(user);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete simulator", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param imei device.
     * @param user user.
     * @return
     */
    private Device createSimulatorDevice(final String imei, final User user) {
        final Device d = new Device();
        d.setImei(imei);
        d.setActive(true);
        d.setCompany(user.getCompany());
        d.setName("Simulator device for user " + user.getEmail());
        return deviceDao.save(d);
    }

    /**
     * @param id user ID.
     * @return virtual IMEI.
     */
    public static String generateImei(final Long id) {
        final char[] alphabet = {'a','b','c','d','e','f','g','h','i','j'};

        //add user ID
        final String userId = id.toString();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < userId.length(); i++) {
            sb.append(alphabet[(userId.charAt(i) - '0')]);
        }

        //add last character
        sb.append('h');

        //add leading chars
        while (sb.length() < 15) {
            sb.insert(0, 'a');
        }

        return sb.toString();
    }
    /**
     * @param o1
     * @param o2
     * @return
     */
    private boolean equals(final Object o1, final Object o2) {
        return o1 == null && o2 == null || o1 != null && o1.equals(o2);
    }
    /**
     * @param user user.
     * @return simulator parser.
     */
    private SimulatorSerializer createSerializer(final User user) {
        return new SimulatorSerializer(user);
    }
}
