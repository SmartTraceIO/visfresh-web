/**
 *
 */
package com.visfresh.controllers;


import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
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
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.InterimStopDto;
import com.visfresh.io.json.InterimStopSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("InterimStop")
@RequestMapping("/rest")
public class InterimStopController extends AbstractShipmentBaseController implements ShipmentConstants {
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
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/addInterimStop", method = RequestMethod.POST)
    public JsonObject addInterimStop(final @RequestBody JsonObject jsonRequest) throws RestServiceException {
        return saveInterimStop(jsonRequest);
    }
    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/saveInterimStop", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveInterimStop(final @RequestBody JsonObject jsonRequest) throws RestServiceException {
        final User user = getLoggedInUser();
        final InterimStopSerializer serializer = getSerializer(user);
        final InterimStopDto req = serializer.parseInterimStopDto(jsonRequest);

        //find shipment
        final Shipment shipment = shipmentDao.findOne(req.getShipmentId());
        if (shipment == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Unable to found shipment " + req.getShipmentId());
        }
        checkCompanyAccess(user, shipment);

        //find location
        final LocationProfile location = locationProfileDao.findOne(req.getLocationId());
        if (location == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
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
    }
    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getInterimStop", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject getInterimStop(
            final @RequestParam(value = "shipment") Long shipmentId,
            final @RequestParam Long id) throws RestServiceException {
            final User user = getLoggedInUser();
        //find shipment
        final Shipment shipment = shipmentDao.findOne(shipmentId);
        if (shipment == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Unable to found shipment " + shipmentId);
        }
        checkCompanyAccess(user, shipment);

        final InterimStop stp = interimStopDao.findOne(shipment, id);
        if (stp == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Unable to found interim stop by id " + id + " for shipment " + shipmentId);
        }

        final InterimStopSerializer serializer = getSerializer(user);
        final InterimStopDto dto = new InterimStopDto(shipment, stp);

        return createSuccessResponse(serializer.toJson(dto));
    }
    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/deleteInterimStop", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject deleteInterimStop(
            final @RequestParam(value = "shipment") Long shipmentId,
            final @RequestParam Long id) throws RestServiceException {
            final User user = getLoggedInUser();
        //find shipment
        final Shipment shipment = shipmentDao.findOne(shipmentId);
        if (shipment == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Unable to found shipment " + shipmentId);
        }
        checkCompanyAccess(user, shipment);

        final InterimStop stp = interimStopDao.findOne(shipment, id);
        if (stp == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Unable to found interim stop by id " + id + " for shipment " + shipmentId);
        }
        interimStopDao.delete(shipment, stp);

        return createSuccessResponse(null);
    }
    /**
     * @param authToken authentication token.
     * @param jsonRequest JSON save shipment request.
     * @return ID of saved shipment.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getInterimStops", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject getInterimStops(
            final @RequestParam(value = "shipment") Long shipmentId) throws RestServiceException {
            final User user = getLoggedInUser();
        //find shipment
        final Shipment shipment = shipmentDao.findOne(shipmentId);
        if (shipment == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
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
    }
    /**
     * @param user
     * @return
     */
    private InterimStopSerializer getSerializer(final User user) {
        return new InterimStopSerializer(user.getLanguage(), user.getTimeZone());
    }
}
