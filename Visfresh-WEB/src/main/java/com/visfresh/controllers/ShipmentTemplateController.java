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
import com.visfresh.constants.ShipmentTemplateConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.impl.ShipmentTemplateDaoImpl;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.SpringRoles;
import com.visfresh.entities.User;
import com.visfresh.io.ShipmentTemplateDto;
import com.visfresh.io.json.ShipmentTemplateSerializer;
import com.visfresh.lists.ListShipmentTemplateItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("ShipmentTemplate")
@RequestMapping("/rest")
public class ShipmentTemplateController extends AbstractShipmentBaseController implements ShipmentTemplateConstants {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ShipmentTemplateController.class);
    @Autowired
    private ShipmentTemplateDao shipmentTemplateDao;

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
    @RequestMapping(value = "/saveShipmentTemplate", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveShipmentTemplate(
            final @RequestBody JsonObject tpl) {
        try {
            final User user = getLoggedInUser();
            final ShipmentTemplateDto dto = createSerializer(user).parseShipmentTemplate(tpl);

            checkCompanyAccess(user, shipmentTemplateDao.findOne(dto.getId()));

            final ShipmentTemplate t = createTemplate(dto);
            t.setCompany(user.getCompany());

            //check company access to sub entiites
            resolveReferences(user, dto, t);

            final Long id = shipmentTemplateDao.save(t).getId();
            saveAlternativeAndInterimLoations(user, t,
                    dto.getInterimLocations(),
                    dto.getEndLocationAlternatives());
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
    @RequestMapping(value = "/getShipmentTemplates", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getShipmentTemplates(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so) {

        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        try {
            //check logged in.
            final User user = getLoggedInUser();

            final Filter filter = new Filter();
            filter.addFilter(ShipmentTemplateDaoImpl.AUTOSTART_FIELD, false);
            final List<ShipmentTemplate> templates = shipmentTemplateDao.findByCompany(
                    user.getCompany(),
                    createSorting(sc, so, getDefaultSortOrder(), 1),
                    page,
                    filter);
            final int total = shipmentTemplateDao.getEntityCount(user.getCompany(), filter);

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
            SHIPMENT_TEMPLATE_NAME,
            SHIPMENT_DESCRIPTION,
            SHIPPED_FROM,
            SHIPPED_TO,
            ALERT_PROFILE_ID
        };
    }

    /**
     * @param authToken authentication token.
     * @param shipmentTemplateId shipment template ID.
     * @return shipment template.
     */
    @RequestMapping(value = "/getShipmentTemplate", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getShipmentTemplate(
            @RequestParam final Long shipmentTemplateId) {
        try {
            //check logged in.
            final User user = getLoggedInUser();
            final ShipmentTemplate template = shipmentTemplateDao.findOne(shipmentTemplateId);
            checkCompanyAccess(user, template);

            final ShipmentTemplateDto dto = new ShipmentTemplateDto(template);
            addInterimLocations(dto, template);

            return createSuccessResponse(createSerializer(user).toJson(dto));
        } catch (final Exception e) {
            log.error("Failed to get shipment templates", e);
            return createErrorResponse(e);
        }
    }

    /**
     * @param authToken authentication token.
     * @param shipmentTemplateId shipment template ID.
     * @return shipment template.
     */
    @RequestMapping(value = "/deleteShipmentTemplate", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject deleteShipmentTemplate(
            @RequestParam final Long shipmentTemplateId) {
        try {
            //check logged in.
            final User user = getLoggedInUser();
            final ShipmentTemplate tpl = shipmentTemplateDao.findOne(shipmentTemplateId);
            checkCompanyAccess(user, tpl);

            shipmentTemplateDao.delete(tpl);
            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete shipment templates", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param user
     * @return
     */
    private ShipmentTemplateSerializer createSerializer(final User user) {
        return new ShipmentTemplateSerializer(user.getTimeZone());
    }
    /**
     * @param dto
     * @return
     */
    private ShipmentTemplate createTemplate(final ShipmentTemplateDto dto) {
        final ShipmentTemplate tpl = new ShipmentTemplate();
        copyBaseData(dto, tpl);
        tpl.setName(dto.getName());
        tpl.setAddDateShipped(dto.isAddDateShipped());
        tpl.setDetectLocationForShippedFrom(dto.isDetectLocationForShippedFrom());
        return tpl;
    }
}
