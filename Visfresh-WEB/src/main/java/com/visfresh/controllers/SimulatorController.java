/**
 *
 */
package com.visfresh.controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.SimulatorDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Device;
import com.visfresh.entities.Role;
import com.visfresh.entities.Simulator;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.SimulatorDto;
import com.visfresh.io.StartSimulatorRequest;
import com.visfresh.io.json.SimulatorSerializer;
import com.visfresh.services.RestServiceException;
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
    private AutoStartShipmentDao autoStartShipmentDao;

    /**
     * Default constructor.
     */
    public SimulatorController() {
        super();
    }

    @RequestMapping(value = "/saveSimulator", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin})
    public JsonObject saveSimulator(final @RequestBody JsonObject json) throws RestServiceException {
            final User user = getLoggedInUser();
            final SimulatorDto dto = createSerializer(user).parseSimulator(json);
        //find source device.
        final Device d = deviceDao.findByImei(dto.getSourceDevice());
        if (d == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA, "Source device "
                    + dto.getSourceDevice() + " not found");
        }

        //find user
        final User u = userDao.findByEmail(dto.getUser());
        if (u == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA, "User "
                    + dto.getUser() + " not found");
        }
        final AutoStartShipment auto = autoStartShipmentDao.findOne(dto.getAutoStart());
        if (auto != null && !u.getCompanyId().equals(auto.getCompanyId())) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
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
        final Long autoStart = auto == null ? null : auto.getId();
        final Long oldAutoStart = simulatorDevice.getAutostartTemplateId();

        if (!equals(autoStart, oldAutoStart)) {
            simulatorDevice.setAutostartTemplateId(autoStart);
            deviceDao.save(simulatorDevice);
        }

        return createIdResponse("simulatorDevice", simulatorDevice.getImei());
    }

    @RequestMapping(value = "/getSimulator", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getSimulator(
            final @RequestParam(required = false) String user) throws RestServiceException {
        final User currentUser = getLoggedInUser();

        //find user
        final User u = getSimulatorOwner(user, currentUser);

        JsonObject response = null;
        final SimulatorDto dto = dao.findSimulatorDto(u);
        if (dto != null) {
            response = createSerializer(currentUser).toJson(dto);
        }

        return createSuccessResponse(response);
    }
    @RequestMapping(value = "/deleteSimulator", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin})
    public JsonObject deleteSimulator(final @RequestParam String user) throws RestServiceException {
        //find user
        final User u = userDao.findByEmail(user);
        if (u == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA, "User "
                    + user + " not found");
        }

        service.stopSimulator(u);
        dao.delete(u);
        return createSuccessResponse(null);
    }
    @RequestMapping(value = "/startSimulator", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject startSimulator(final @RequestBody JsonObject json) throws RestServiceException, ParseException {
        final User user = getLoggedInUser();
        final SimulatorSerializer ser = createSerializer(user);

        final StartSimulatorRequest req = ser.parseStartRequest(json);
        final User u = getSimulatorOwner(req.getUser(), user);

        //check already started
        final SimulatorDto sim = dao.findSimulatorDto(u);
        if (sim == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Simulator not found for user " + u.getEmail());
        }
        if (sim.isStarted()) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Simulator already started for user " + u.getEmail());
        }

        //check correct velosity
        if (req.getVelosity() < 1) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Invalid velosity " + req.getVelosity() + " should start from 1");
        }

        final DateFormat fmt = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());

        final Date startDate = req.getStartDate() == null ? null : fmt.parse(req.getStartDate());
        final Date endDate = req.getEndDate() == null ? null : fmt.parse(req.getEndDate());

        service.startSimulator(u, startDate, endDate, req.getVelosity());
        return createSuccessResponse(null);
    }
    @RequestMapping(value = "/stopSimulator", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject stopSimulator(final @RequestParam(required = false) String user) throws RestServiceException {
        final User currentUser = getLoggedInUser();
        //find user
        final User u = getSimulatorOwner(user, currentUser);

        service.stopSimulator(u);
        return createSuccessResponse(null);
    }

    /**
     * @param userEmail
     * @param currentUser
     * @return
     * @throws RestServiceException
     */
    protected User getSimulatorOwner(final String userEmail,
            final User currentUser) throws RestServiceException {
        final User u;
        if (userEmail != null) {
            u = userDao.findByEmail(userEmail);
            if (u == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "User " + userEmail + " not found");
            }

            if (Role.Admin.hasRole(currentUser)) {
                checkCompanyAccess(currentUser, u);
            } else if (!currentUser.getId().equals(u.getId())) {
                throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                        "Access to simulator of user " + userEmail + " is not permitted");
            }
        } else {
            u = currentUser;
        }
        return u;
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
        d.setCompany(user.getCompanyId());
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
