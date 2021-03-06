/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.constants.PairedPhoneConstants;
import com.visfresh.dao.Page;
import com.visfresh.dao.PairedPhoneDao;
import com.visfresh.entities.PairedPhone;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.json.PairedPhoneSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("PairedPhone")
@RequestMapping("/rest")
public class PairedPhoneController extends AbstractController {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(PairedPhoneController.class);

    @Autowired
    private PairedPhoneDao dao;

    /**
     * Default constructor.
     */
    public PairedPhoneController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param userId ID of user for request info.
     * @return user info
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getPairedPhone", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getPairedPhone(
            final @RequestParam(required = false) Long id,
            final @RequestParam(required = false) String phone,
            final @RequestParam(required = false) String beacon
            ) throws RestServiceException {
        final User user = getLoggedInUser();

        final PairedPhone p;
        if (id != null) {
            p = dao.findOne(id);
        } else if (phone != null && beacon != null){
            p = dao.findOne(phone, beacon);
        } else {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "one id or phone with beacon should be specified");
        }

        if (p != null) {
            checkCompanyAccess(user, p.getCompany());
        }

        return createSuccessResponse(p == null ? null : createSerializer(user).toJson(p));
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @param sc sort column
     * @param so sort order.
     * @return
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getPairedPhones", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser,
        SpringRoles.NormalUser})
    public JsonObject getPairedPhones(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        final User user = getLoggedInUser();
        final int total = dao.getEntityCount(user.getCompanyId(), null);
        final PairedPhoneSerializer ser = createSerializer(user);
        final JsonArray array = new JsonArray();

        final List<PairedPhone> phones = dao.findByCompany(
                user.getCompanyId(),
                createSorting(sc, so, getDefaultListShipmentsSortingOrder(), 2),
                page,
                null);

        for (final PairedPhone phone : phones) {
            array.add(ser.toJson(phone));
        }
        return createListSuccessResponse(array, total);
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @param sc sort column
     * @param so sort order.
     * @return
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getPairedBeacons", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser,
        SpringRoles.NormalUser})
    public JsonObject getPairedBeacons(final String phone) throws RestServiceException {
        final List<PairedPhone> paredPhones = dao.getPairedBeacons(phone);

        final JsonArray array = new JsonArray();
        for (final PairedPhone p : paredPhones) {
            try {
                checkCompanyAccess(getLoggedInUser(), p.getCompany());
                array.add(new JsonPrimitive(p.getBeaconId()));
            } catch (final Exception e) {
                // it is ok
            }
        }

        return createSuccessResponse(array);
    }

    /**
     * @return
     */
    private String[] getDefaultListShipmentsSortingOrder() {
        return new String[] {
                PairedPhoneConstants.ID,
                PairedPhoneConstants.IMEI,
                PairedPhoneConstants.BEACON_ID,
                PairedPhoneConstants.ACTIVE,
                PairedPhoneConstants.COMAPNY,
                PairedPhoneConstants.DESCRIPTION
        };
    }
    /**
     * @param authToken authentication token.
     * @param req save user request.
     * @return user ID.
     * @throws RestServiceException
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/savePairedPhone", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser,
        SpringRoles.NormalUser})
    public JsonObject savePairedPhone(final @RequestBody JsonObject req) throws RestServiceException {
        final User user = getLoggedInUser();
        final PairedPhone phone = createSerializer(user).parsePairedPhone(req);

        checkCompany(user, phone.getCompany());
        dao.save(phone);

        return createIdResponse("id", phone.getId());
    }

    @RequestMapping(value = "/deletePairedPhone", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser,
        SpringRoles.NormalUser})
    public JsonObject deletePairedPhone(
            final @RequestParam(required = false) Long id,
            final @RequestParam(required = false) String phone,
            final @RequestParam(required = false) String beacon
            ) throws RestServiceException {
        final User user = getLoggedInUser();

        final PairedPhone p;
        if (id != null) {
            p = dao.findOne(id);
        } else if (phone != null && beacon != null){
            p = dao.findOne(phone, beacon);
        } else {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "one id or phone with beacon should be specified");
        }

        if (p != null) {
            checkCompanyAccess(user, user.getCompanyId());
            dao.delete(p.getId());
            return createSuccessResponse(null);
        } else {
            throw new RestServiceException(ErrorCodes.INCORRECT_REQUEST_DATA,
                    "PairedPhone not exists");
        }
    }
    /**
     * @param user
     * @return
     */
    private PairedPhoneSerializer createSerializer(final User user) {
        final PairedPhoneSerializer deviceSerializer = new PairedPhoneSerializer(user.getTimeZone());
        return deviceSerializer;
    }
}
