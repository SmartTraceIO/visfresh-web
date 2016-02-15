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
import com.visfresh.constants.DefaultShipmentConstants;
import com.visfresh.dao.DefaultShipmentDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.DefaultShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.io.DefaultShipmentDto;
import com.visfresh.io.json.DefaultShipmentSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@RestController("DefaultShipment")
@RequestMapping("/rest")
public class DefaultShipmentController extends AbstractController
        implements DefaultShipmentConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultShipmentController.class);

    @Autowired
    private DefaultShipmentDao dao;
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private ShipmentTemplateDao template;

    /**
     * Default constructor.
     */
    public DefaultShipmentController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param defShipment alert profile.
     * @return ID of saved alert profile.
     */
    @RequestMapping(value = "/saveDefaultShipment/{authToken}", method = RequestMethod.POST)
    public JsonObject saveAlertProfile(@PathVariable final String authToken,
            final @RequestBody JsonObject defShipment) {
        try {
            final User user = getLoggedInUser(authToken);
            final DefaultShipmentDto dto = createSerializer(user)
                    .parseDefaultShipmentDto(defShipment);

            security.checkCanSaveDefaultShipment(user);

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

            final DefaultShipment cfg = new DefaultShipment();
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
     * @param defaultShipmentId default shipment ID.
     * @return default shipment as JSON.
     */
    @RequestMapping(value = "/getDefaultShipment/{authToken}", method = RequestMethod.GET)
    public JsonObject getDefaultShipment(@PathVariable final String authToken,
            @RequestParam final Long defaultShipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanViewDefaultShipments(user);

            final DefaultShipment cfg = dao.findOne(defaultShipmentId);
            checkCompanyAccess(user, cfg);

            return createSuccessResponse(createSerializer(user).toJson(
                    new DefaultShipmentDto(cfg)));
        } catch (final Exception e) {
            log.error("Failed to get default shipment", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param defaultShipmentId default shipment ID.
     * @return default shipment.
     */
    @RequestMapping(value = "/deleteDefaultShipment/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteDefaultShipment(@PathVariable final String authToken,
            @RequestParam final Long defaultShipmentId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveDefaultShipment(user);

            final DefaultShipment cfg = dao.findOne(defaultShipmentId);
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
    @RequestMapping(value = "/getDefaultShipments/{authToken}", method = RequestMethod.GET)
    public JsonElement getDefaultShipments(@PathVariable final String authToken,
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
            final DefaultShipmentSerializer ser = createSerializer(user);

            final List<DefaultShipment> configs = dao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 2),
                    page,
                    null);
            final int total = dao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            for (final DefaultShipment cfg : configs) {
                array.add(ser.toJson(new DefaultShipmentDto(cfg)));
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
    private DefaultShipmentSerializer createSerializer(final User user) {
        return new DefaultShipmentSerializer(user.getTimeZone());
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
