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
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.json.ShipmentTemplateSerializer;
import com.visfresh.services.lists.ListShipmentTemplateItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("ShipmentTemplate")
@RequestMapping("/rest")
public class ShipmentTemplateController extends AbstractController implements ShipmentTemplateConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ShipmentTemplateController.class);
    @Autowired
    private ShipmentTemplateDao shipmentTemplateDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private ReferenceResolver referenceResolver;

    /**
     * Default constructor.
     */
    public ShipmentTemplateController() {
        super();
    }

    /**
     * @param authToken authentication token.
     * @param tpl shipment template.
     * @return ID of saved shipment template.
     */
    @RequestMapping(value = "/saveShipmentTemplate/{authToken}", method = RequestMethod.POST)
    public JsonObject saveShipmentTemplate(@PathVariable final String authToken,
            final @RequestBody JsonObject tpl) {
        try {
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipmentTemplate(user);

            final ShipmentTemplate t = createSerializer(user).parseShipmentTemplate(tpl);
            checkCompanyAccess(user, t);

            t.setCompany(user.getCompany());
            final Long id = shipmentTemplateDao.save(t).getId();
            return createIdResponse("shipmentTemplateId", id);
        } catch (final Exception e) {
            log.error("Failed to save shipment template", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of shipment templates.
     */
    @RequestMapping(value = "/getShipmentTemplates/{authToken}", method = RequestMethod.GET)
    public JsonObject getShipmentTemplates(@PathVariable final String authToken,
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so) {

        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipmentTemplates(user);

            final List<ShipmentTemplate> templates = shipmentTemplateDao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 1),
                    page,
                    null);
            final int total = shipmentTemplateDao.getEntityCount(user.getCompany(), null);

            final JsonArray array = new JsonArray();
            final ShipmentTemplateSerializer ser = createSerializer(user);
            for (final ShipmentTemplate tpl : templates) {
                final ListShipmentTemplateItem item = new ListShipmentTemplateItem(tpl);
                array.add(ser.toJson(item));
            }

            return createListSuccessResponse(array, total);
        } catch (final Exception e) {
            log.error("Failed to get shipment templates", e);
            return createErrorResponse(e);
        }
    }
    private String[] getDefaultSortOrder() {
        return new String[] {
            PROPERTY_SHIPMENT_TEMPLATE_NAME,
            PROPERTY_SHIPMENT_DESCRIPTION,
            PROPERTY_SHIPPED_FROM,
            PROPERTY_SHIPPED_TO,
            PROPERTY_ALERT_PROFILE_ID
        };
    }

    /**
     * @param authToken authentication token.
     * @param shipmentTemplateId shipment template ID.
     * @return shipment template.
     */
    @RequestMapping(value = "/getShipmentTemplate/{authToken}", method = RequestMethod.GET)
    public JsonObject getShipmentTemplate(@PathVariable final String authToken,
            @RequestParam final Long shipmentTemplateId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanGetShipmentTemplates(user);

            final ShipmentTemplate template = shipmentTemplateDao.findOne(shipmentTemplateId);
            checkCompanyAccess(user, template);

            return createSuccessResponse(createSerializer(user).toJson(template));
        } catch (final Exception e) {
            log.error("Failed to get shipment templates", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private ShipmentTemplateSerializer createSerializer(final User user) {
        final ShipmentTemplateSerializer tpl = new ShipmentTemplateSerializer(user.getTimeZone());
        tpl.setReferenceResolver(referenceResolver);
        return tpl;
    }

    /**
     * @param authToken authentication token.
     * @param shipmentTemplateId shipment template ID.
     * @return shipment template.
     */
    @RequestMapping(value = "/deleteShipmentTemplate/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteShipmentTemplate(@PathVariable final String authToken,
            @RequestParam final Long shipmentTemplateId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            security.checkCanSaveShipmentTemplate(user);

            final ShipmentTemplate tpl = shipmentTemplateDao.findOne(shipmentTemplateId);
            checkCompanyAccess(user, tpl);

            shipmentTemplateDao.delete(tpl);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete shipment templates", e);
            return createErrorResponse(e);
        }
    }
}
