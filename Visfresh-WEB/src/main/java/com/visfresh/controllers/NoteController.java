/**
 *
 */
package com.visfresh.controllers;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceConstants;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.dao.NoteDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Note;
import com.visfresh.entities.NoteType;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.io.NoteDto;
import com.visfresh.io.json.NoteSerializer;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
//@RestController("Note")
//@RequestMapping("/rest")
public class NoteController extends AbstractController implements DeviceConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(NoteController.class);
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private NoteDao noteDao;

    /**
     * Default constructor.
     */
    public NoteController() {
        super();
    }
    /**
     * @param authToken
     * @param shipmentId
     * @param sn
     * @param trip
     * @return
     */
    @RequestMapping(value = "/getNotes/{authToken}", method = RequestMethod.GET)
    public JsonObject getNotes(@PathVariable final String authToken,
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip
            ) {
        //check parameters
        if (shipmentId == null && (sn == null || trip == null)) {
            return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Should be specified shipmentId or (sn and trip) request parameters");
        }

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final Shipment s;
            if (shipmentId != null) {
                s = shipmentDao.findOne(shipmentId);
            } else {
                s = shipmentDao.findBySnTrip(sn, trip);
            }

            checkCompanyAccess(user, s);
            if (s == null) {
                return createSuccessResponse(new JsonArray());
            }

            final NoteSerializer ser = getSerializer(user);
            final DateFormat iso = DateTimeUtils.createIsoFormat(user);
            final List<Note> notes = noteDao.findByShipment(s);

            final JsonArray array = new JsonArray();
            for (final Note note : notes) {
                final NoteDto dto = new NoteDto();
                //populate note DTO
                dto.setActiveFlag(s.getDevice().isActive());
                dto.setCreatedBy(user.getEmail());
                dto.setCreationDate(iso.format(dto.getCreationDate()));
                dto.setNoteNum(note.getNoteNum());
                dto.setNoteText(note.getNoteText());
                dto.setShipmentId(s.getId());
                dto.setNoteType(note.getNoteType().name());
                dto.setSn(s.getDevice().getSn());
                dto.setTrip(s.getTripCount());
                dto.setTimeOnChart(iso.format(note.getTimeOnChart()));

                array.add(ser.toJson(dto));
            }

            return createSuccessResponse(array);
        } catch (final Exception e) {
            log.error("Failed to get notes for : " + shipmentId, e);
            return createErrorResponse(e);
        }
    }
    @RequestMapping(value = "/saveNote/{authToken}", method = RequestMethod.POST)
    public JsonObject saveNote(@PathVariable final String authToken,
            final @RequestBody JsonObject jsonRequest) {
        try {

            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final NoteSerializer ser = getSerializer(user);
            final NoteDto dto = ser.parseNoteDto(jsonRequest);

            //check parameters
            if (dto.getShipmentId() == null && (dto.getSn() == null || dto.getTrip() == null)) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA,
                        "Should be specified shipmentId or (sn and trip) request parameters");
            }

            final Shipment s;
            if (dto.getShipmentId() != null) {
                s = shipmentDao.findOne(dto.getShipmentId());
            } else {
                s = shipmentDao.findBySnTrip(dto.getSn(), dto.getTrip());
            }

            checkCompanyAccess(user, s);
            if (s == null) {
                return createErrorResponse(ErrorCodes.INCORRECT_REQUEST_DATA, "Shipment not found");
            }

            final DateFormat iso = DateTimeUtils.createIsoFormat(user);

            Note note = new Note();
            note.setCreationDate(dto.getCreationDate() == null ? new Date() : iso.parse(dto.getCreationDate()));
            note.setCreatedBy(dto.getCreatedBy() == null ? user.getEmail() : dto.getCreatedBy());
            note.setNoteNum(dto.getNoteNum());
            note.setNoteText(dto.getNoteText());
            note.setNoteType(dto.getNoteType() == null ? NoteType.Simple : NoteType.valueOf(dto.getNoteType()));
            note.setTimeOnChart(iso.parse(dto.getTimeOnChart()));

            note = noteDao.save(s, note);

            return createSuccessResponse(ser.createSaveResponse(note));
        } catch (final Exception e) {
            log.error("Failed to save note" + jsonRequest, e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user the user.
     * @return note serializer.
     */
    private NoteSerializer getSerializer(final User user) {
        return new NoteSerializer(user.getTimeZone());
    }
}
