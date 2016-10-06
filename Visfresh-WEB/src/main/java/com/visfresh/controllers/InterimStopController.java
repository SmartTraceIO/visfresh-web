/**
 *
 */
package com.visfresh.controllers;


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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.io.InterimStopDto;
import com.visfresh.io.json.InterimStopSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("InterimStop")
@RequestMapping("/rest")
public class InterimStopController extends AbstractShipmentBaseController implements ShipmentConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(InterimStopController.class);

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private InterimStopDao interimStopDao;

    /**
     * Default constructor.
     */
    public InterimStopController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/addInterimStop/{authToken}", method = RequestMethod.POST)
    public JsonObject addInterimStop(@PathVariable final String authToken,
            final @RequestBody JsonObject jsonRequest) {
        return saveInterimStop(authToken, jsonRequest);
    }
    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/saveInterimStop/{authToken}", method = RequestMethod.POST)
    public JsonObject saveInterimStop(@PathVariable final String authToken,
            final @RequestBody JsonObject jsonRequest) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            final InterimStopSerializer serializer = getSerializer(user);
            final InterimStopDto req = serializer.parseInterimStopDto(jsonRequest);

            //find shipment
            final Shipment shipment = shipmentDao.findOne(req.getShipmentId());
            if (shipment == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to found shipment " + req.getShipmentId());
            }
            checkCompanyAccess(user, shipment);

            if (req.getId() == null) {
                //check number of already saved
                if (interimStopDao.getByShipment(shipment).size() > 0) {
                    return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                            "Only one interim stop can be saved for given API version");
                }
            }

            //find location
            final LocationProfile location = locationProfileDao.findOne(req.getLocationId());
            if (location == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to found location " + req.getLocationId());
            }
            checkCompanyAccess(user, location);

            //create interim stop
            final InterimStop stop = new InterimStop();
            stop.setDate(req.getDate() == null ? new Date() : req.getDate());
            stop.setLocation(location);
            stop.setTime(req.getTime());

            interimStopDao.save(shipment, stop);

            return createIdResponse("id", stop.getId());
        } catch (final Exception e) {
            log.error("Failed to save shipment by request: " + jsonRequest, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/getInterimStop/{authToken}", method = RequestMethod.GET)
    public JsonObject getInterimStop(@PathVariable final String authToken,
            final @RequestParam(value = "shipment") Long shipmentId,
            final @RequestParam Long id) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            //find shipment
            final Shipment shipment = shipmentDao.findOne(shipmentId);
            if (shipment == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to found shipment " + shipmentId);
            }
            checkCompanyAccess(user, shipment);

            final InterimStop stp = interimStopDao.findOne(shipment, id);
            if (stp == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to found interim stop by id " + id + " for shipment " + shipmentId);
            }

            final InterimStopSerializer serializer = getSerializer(user);
            final InterimStopDto dto = new InterimStopDto(shipment, stp);

            return createSuccessResponse(serializer.toJson(dto));
        } catch (final Exception e) {
            log.error("Failed to get interim stop", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/deleteInterimStop/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteInterimStop(@PathVariable final String authToken,
            final @RequestParam(value = "shipment") Long shipmentId,
            final @RequestParam Long id) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            //find shipment
            final Shipment shipment = shipmentDao.findOne(shipmentId);
            if (shipment == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to found shipment " + shipmentId);
            }
            checkCompanyAccess(user, shipment);

            final InterimStop stp = interimStopDao.findOne(shipment, id);
            if (stp == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to found interim stop by id " + id + " for shipment " + shipmentId);
            }
            interimStopDao.delete(shipment, stp);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get interim stop", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     */
    @RequestMapping(value = "/getInterimStops/{authToken}", method = RequestMethod.GET)
    public JsonObject getInterimStops(@PathVariable final String authToken,
            final @RequestParam(value = "shipment") Long shipmentId) {
        try {
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.BasicUser);

            //find shipment
            final Shipment shipment = shipmentDao.findOne(shipmentId);
            if (shipment == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to found shipment " + shipmentId);
            }
            checkCompanyAccess(user, shipment);

            final InterimStopSerializer serializer = getSerializer(user);

            final JsonArray array = new JsonArray();
            for (final InterimStop stp : interimStopDao.getByShipment(shipment)) {
                final InterimStopDto dto = new InterimStopDto(shipment, stp);
                array.add(serializer.toJson(dto));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get interim stop", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private InterimStopSerializer getSerializer(final User user) {
        return new InterimStopSerializer(user.getLanguage(), user.getTimeZone());
    }
}
