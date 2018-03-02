/**
 *
 */
package com.visfresh.controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceConstants;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.dao.NoteDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Note;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.NoteDto;
import com.visfresh.io.json.NoteSerializer;
import com.visfresh.services.RestServiceException;
import com.visfresh.services.ShipmentAuditService;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("Note")
@RequestMapping("/rest")
public class NoteController extends AbstractController implements DeviceConstants {
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private ShipmentAuditService auditService;

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
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getNotes", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getNotes(
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip
            ) throws RestServiceException {
        //check parameters
        if (shipmentId == null && (sn == null || trip == null)) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Should be specified shipmentId or (sn and trip) request parameters");
        }

        //check logged in.
        final User user = getLoggedInUser();
        final Shipment s;
        if (shipmentId != null) {
            s = shipmentDao.findOne(shipmentId);
        } else {
            s = shipmentDao.findBySnTrip(user.getCompanyId(), sn, trip);
        }

        checkCompanyAccess(user, s);
        if (s == null) {
            return createSuccessResponse(new JsonArray());
        }

        final NoteSerializer ser = getSerializer(user);
        final DateFormat iso = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());
        final List<Note> notes = noteDao.findByShipment(s);

        final JsonArray array = new JsonArray();
        for (final Note note : notes) {
            final NoteDto dto = creaetNoteDto(note, s, iso);

            array.add(ser.toJson(dto));
        }

        return createSuccessResponse(array);
    }
    @RequestMapping(value = "/deleteNote", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject deleteNote(
            @RequestParam(required = false) final Long shipmentId,
            @RequestParam(required = false) final String sn,
            @RequestParam(required = false) final Integer trip,
            @RequestParam final Integer noteNum
            ) throws RestServiceException {
        //check parameters
        if (shipmentId == null && (sn == null || trip == null)) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Should be specified shipmentId or (sn and trip) request parameters");
        }

        //check logged in.
        final User user = getLoggedInUser();
        final Shipment s;
        if (shipmentId != null) {
            s = shipmentDao.findOne(shipmentId);
        } else {
            s = shipmentDao.findBySnTrip(user.getCompanyId(), sn, trip);
        }

        checkCompanyAccess(user, s);
        if (s == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA, "Shipment not found");
        }

        final Note note = noteDao.getNote(s, noteNum);
        if (note == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Note with given number " + noteNum + " not found");
        }

        note.setActive(false);
        noteDao.save(s, note);
        auditService.handleShipmentAction(s.getId(), user, ShipmentAuditAction.DeletedNote, null);

        return createSuccessResponse(null);
    }
    /**
     * @param note
     * @param s
     * @param iso
     * @return
     */
    protected static NoteDto creaetNoteDto(final Note note, final Shipment s,
            final DateFormat iso) {
        final NoteDto dto = new NoteDto();
        //populate note DTO
        dto.setActiveFlag(note.isActive());
        dto.setCreatedBy(note.getCreatedBy());
        dto.setCreationDate(iso.format(note.getCreationDate()));
        dto.setNoteNum(note.getNoteNum());
        dto.setNoteText(note.getNoteText());
        dto.setShipmentId(s.getId());
        dto.setNoteType(note.getNoteType());
        dto.setSn(s.getDevice().getSn());
        dto.setTrip(s.getTripCount());
        dto.setTimeOnChart(iso.format(note.getTimeOnChart()));
        dto.setCreatedByName(note.getCreateCreatedByName());
        return dto;
    }
    @RequestMapping(value = "/saveNote", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveNote(final @RequestBody JsonObject jsonRequest) throws RestServiceException, ParseException {
        //check logged in.
        final User user = getLoggedInUser();
        final NoteSerializer ser = getSerializer(user);
        final NoteDto dto = ser.parseNoteDto(jsonRequest);

        //check parameters
        if (dto.getShipmentId() == null && (dto.getSn() == null || dto.getTrip() == null)) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "Should be specified shipmentId or (sn and trip) request parameters");
        }

        final Shipment s;
        if (dto.getShipmentId() != null) {
            s = shipmentDao.findOne(dto.getShipmentId());
        } else {
            s = shipmentDao.findBySnTrip(user.getCompanyId(), dto.getSn(), dto.getTrip());
        }

        checkCompanyAccess(user, s);
        if (s == null) {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA, "Shipment not found");
        }

        final DateFormat iso = DateTimeUtils.createIsoFormat(user.getLanguage(), user.getTimeZone());

        Note note = new Note();
        note.setCreationDate(dto.getCreationDate() == null ? new Date() : iso.parse(dto.getCreationDate()));
        note.setCreatedBy(dto.getCreatedBy() == null ? user.getEmail() : dto.getCreatedBy());
        note.setNoteNum(dto.getNoteNum());
        note.setNoteText(dto.getNoteText());
        note.setNoteType(dto.getNoteType());
        note.setTimeOnChart(iso.parse(dto.getTimeOnChart()));
        note.setActive(dto.isActiveFlag());

        final boolean isNew = note.getNoteNum() == null;
        note = noteDao.save(s, note);

        if (isNew) {
            auditService.handleShipmentAction(s.getId(), user, ShipmentAuditAction.AddedNote, null);
        } else {
            auditService.handleShipmentAction(s.getId(), user, ShipmentAuditAction.UpdatedNote, null);
        }

        return createSuccessResponse(ser.createSaveResponse(note));
    }
    /**
     * @param user the user.
     * @return note serializer.
     */
    private NoteSerializer getSerializer(final User user) {
        return new NoteSerializer(user.getTimeZone());
    }
}
