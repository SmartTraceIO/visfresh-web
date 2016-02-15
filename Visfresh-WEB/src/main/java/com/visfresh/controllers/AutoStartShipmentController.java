/**
 *
 */
package com.visfresh.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.AutoStartShipmentConstants;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.io.AutoStartShipmentDto;
import com.visfresh.io.json.AutoStartShipmentSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@RestController("AutoStartShipment")
@RequestMapping("/rest")
public class AutoStartShipmentController extends AbstractController
        implements AutoStartShipmentConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AutoStartShipmentController.class);

    @Autowired
    private AutoStartShipmentDao dao;
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private ShipmentTemplateDao template;

    /**
     * Default constructor.
     */
    public AutoStartShipmentController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param defShipment alert profile.
     * @return ID of saved alert profile.
     */
    @RequestMapping(value = "/saveAutoStartShipment/{authToken}", method = RequestMethod.POST)
    public JsonObject saveAutoStartShipment(@PathVariable final String authToken,
            final @RequestBody JsonObject defShipment) {
        try {
            final User user = getLoggedInUser(authToken);
            final AutoStartShipmentDto dto = createSerializer(user)
                    .parseAutoStartShipmentDto(defShipment);

            security.checkCanSaveAutoStartShipment(user);

            final ShipmentTemplate tpl = template.findOne(dto.getTemplate());
            checkCompanyAccess(user, tpl);

            //get locations.
            final Set<Long> locFroms = new HashSet<>(dto.getStartLocations());
            final Set<Long> locTos = new HashSet<>(dto.getEndLocations());

            final Set<Long> allLoctions = new HashSet<Long>();
            allLoctions.addAll(locFroms);
            allLoctions.addAll(locTos);

            final List<LocationProfile> loctions = locationProfileDao.findAll(allLoctions);
            //check company access
            for (final LocationProfile l : loctions) {
                checkCompanyAccess(user, l);
            }

            final AutoStartShipment cfg = new AutoStartShipment();
            cfg.setCompany(user.getCompany());
            cfg.setTemplate(tpl);
            cfg.setId(dto.getId());

            //resolve locations
            for (final LocationProfile l : loctions) {
                if (locFroms.contains(l.getId())) {
                    cfg.getShippedFrom().add(l);
                } else {
                    cfg.getShippedTo().add(l);
                }
            }

            final Long id = dao.save(cfg).getId();
            return createIdResponse("defaultShipmentId", id);
        } catch (final Exception e) {
            log.error("Failed to save default shipment", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param autoStartShipmentId default shipment ID.
     * @return default shipment as JSON.
     */
    @RequestMapping(value = "/getAutoStartShipment/{authToken}", method = RequestMethod.GET)
    public JsonObject getAutoStartShipment(@PathVariable final String authToken,
            @RequestParam final Long autoStartShipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanViewAutoStartShipments(user);

            final AutoStartShipment cfg = dao.findOne(autoStartShipmentId);
            checkCompanyAccess(user, cfg);

            return createSuccessResponse(createSerializer(user).toJson(
                    new AutoStartShipmentDto(cfg)));
        } catch (final Exception e) {
            log.error("Failed to get default shipment", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param autoStartShipmentId default shipment ID.
     * @return default shipment.
     */
    @RequestMapping(value = "/deleteAutoStartShipment/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteAutoStartShipment(@PathVariable final String authToken,
            @RequestParam final Long autoStartShipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveAutoStartShipment(user);

            final AutoStartShipment cfg = dao.findOne(autoStartShipmentId);
            checkCompanyAccess(user, cfg);
            dao.delete(cfg);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to get default shipmemnt", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of default shipments.
     */
    @RequestMapping(value = "/getAutoStartShipments/{authToken}", method = RequestMethod.GET)
    public JsonElement getAutoStartShipments(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetAlertProfiles(user);
            final AutoStartShipmentSerializer ser = createSerializer(user);

            final List<AutoStartShipment> configs = dao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 2),
                    page,
                    null);
            final int total = dao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            for (final AutoStartShipment cfg : configs) {
                array.add(ser.toJson(new AutoStartShipmentDto(cfg)));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get default shipments", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private AutoStartShipmentSerializer createSerializer(final User user) {
        return new AutoStartShipmentSerializer(user.getTimeZone());
    }

    /**
     * @return default sort order.
     */
    private String[] getDefaultSortOrder() {
        return new String[] {
            ID,
            TEMPLATE
        };
    }
}
