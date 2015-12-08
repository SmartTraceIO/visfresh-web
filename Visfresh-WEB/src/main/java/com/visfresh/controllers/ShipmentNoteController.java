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
import com.visfresh.constants.ErrorCodes;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentNoteDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentNote;
import com.visfresh.entities.User;
import com.visfresh.io.SaveShipmentNoteRequest;
import com.visfresh.io.json.ShipmentNoteSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("ShipmentNote")
@RequestMapping("/rest")
public class ShipmentNoteController extends AbstractController {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ShipmentNoteController.class);
    /**
     * Shipment DAO
     */
    @Autowired
    private ShipmentDao shipmentDao;
    /**
     * User DAO.
     */
    @Autowired
    private UserDao userDao;
    /**
     * Shipment note DAO.
     */
    @Autowired
    private ShipmentNoteDao shipmentNoteDao;

    /**
     * Default constructor.
     */
    public ShipmentNoteController() {
        super();
    }

    @RequestMapping(value = "/getShipmentNote/{authToken}", method = RequestMethod.GET)
    public JsonObject getShipmentNote(
            @PathVariable final String authToken,
            @RequestParam final long noteId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            final ShipmentNote note = shipmentNoteDao.findOne(noteId);

            if (note != null) {
                security.checkCanViewShipmentNotes(user, note.getShipment(), note.getUser());
            }
            return createSuccessResponse(getSerializer(user).toJson(note));
        } catch (final Exception e) {
            log.error("Failed to get shipment note", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/getShipmentNotes/{authToken}", method = RequestMethod.GET)
    public JsonObject getShipmentNotes(
            @PathVariable final String authToken,
            @RequestParam final long shipmentId,
            @RequestParam final long userId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);

            //find user and shipment
            final User noteOwner = userDao.findOne(userId);
            final Shipment shipment = shipmentDao.findOne(shipmentId);

            final ShipmentNoteSerializer ser = getSerializer(user);
            final JsonArray array = new JsonArray();

            if (shipment != null && noteOwner != null) {
                security.checkCanViewShipmentNotes(user, shipment, noteOwner);

                final List<ShipmentNote> notes = shipmentNoteDao.findByUserAndShipment(shipment, noteOwner);
                for (final ShipmentNote n : notes) {
                    array.add(ser.toJson(n));
                }
            }

            return createListSuccessResponse(array, array.size());
        } catch (final Exception e) {
            log.error("Failed to get shipment notes", e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/saveShipmentNote/{authToken}", method = RequestMethod.POST)
    public JsonObject saveShipmentNote(@PathVariable final String authToken,
            @RequestBody final JsonObject req) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);

            final ShipmentNoteSerializer ser = getSerializer(user);
            final SaveShipmentNoteRequest ssnr = ser.parseSaveShipmentNoteRequest(req);

            //find user and shipment
            final User noteOwner = userDao.findOne(ssnr.getUserId());
            //check user
            if (noteOwner == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to find note owner " + ssnr.getUserId());
            }

            final Shipment shipment = shipmentDao.findOne(ssnr.getShipmentId());
            //check shipment
            if (shipment == null) {
                throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Unable to find shipment " + ssnr.getShipmentId());
            }

            ShipmentNote note = null;
            //attempt to load note from DB.
            if (ssnr.getNoteId() != null) {
                note = shipmentNoteDao.findOne(ssnr.getNoteId());

                if (note != null) {
                    checkOwner(note, noteOwner);
                    checkShipment(note, shipment);
                }
            }

            //possible create new note
            if (note == null) {
                note = new ShipmentNote();
                note.setShipment(shipment);
                note.setUser(user);
            }

            note.setText(ssnr.getNoteText());

            security.checkCanEditShipmentNotes(user, shipment, noteOwner);
            final Long id = shipmentNoteDao.save(note).getId();

            return createIdResponse("shipmentNoteId", id);
        } catch (final Exception e) {
            log.error("Failed to save shipment note", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param note shipment note.
     * @param shipment shipment.
     * @throws RestServiceException
     */
    private void checkShipment(final ShipmentNote note, final Shipment shipment)
            throws RestServiceException {
        final Long expectedShipmentId = note.getShipment().getId();
        final Long actualShipmentId = shipment.getId();
        if (!expectedShipmentId.equals(actualShipmentId)) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Incorrect note shipment. Expected shipment " + expectedShipmentId
                    + ", actual shipment " + actualShipmentId);
        }

    }
    /**
     * @param note shipment note.
     * @param noteOwner note owner.
     * @throws RestServiceException
     */
    private void checkOwner(final ShipmentNote note,
            final User noteOwner) throws RestServiceException {
        final Long expectedUserId = note.getUser().getId();
        final Long actualUserId = noteOwner.getId();
        if (!expectedUserId.equals(actualUserId)) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Incorrect note user. Expected user " + expectedUserId
                    + ", actual user " + actualUserId);
        }
    }

    /**
     * @param user user.
     * @return shipment note serializer.
     */
    private ShipmentNoteSerializer getSerializer(final User user) {
        return new ShipmentNoteSerializer(user.getTimeZone());
    }
}
