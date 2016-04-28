/**
 *
 */
package com.visfresh.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.visfresh.constants.ShipmentTemplateConstants;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.impl.ShipmentTemplateDaoImpl;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Role;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.io.ShipmentTemplateDto;
import com.visfresh.io.json.ShipmentTemplateSerializer;
import com.visfresh.lists.ListShipmentTemplateItem;
import com.visfresh.utils.EntityUtils;

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
    private AlertProfileDao alertProfileDao;
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private NotificationScheduleDao notificationScheduleDao;

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
            checkAccess(user, Role.BasicUser);

            final ShipmentTemplateDto dto = createSerializer(user).parseShipmentTemplate(tpl);

            final ShipmentTemplate t = createTemplate(dto);
            t.setCompany(user.getCompany());

            //check company access to sub entiites
            resolveAlertProfile(t, dto);
            checkCompanyAccess(user, t.getAlertProfile());

            resolveShipped(t, dto);
            checkCompanyAccess(user, t.getShippedFrom());
            checkCompanyAccess(user, t.getShippedTo());

            resolveNotificationSchedules(t, dto);
            checkCompanyAccess(user, t.getAlertsNotificationSchedules());
            checkCompanyAccess(user, t.getArrivalNotificationSchedules());

            final ShipmentTemplate old = this.shipmentTemplateDao.findOne(t.getId());
            checkCompanyAccess(user, old);

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
            checkAccess(user, Role.NormalUser);

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
    @RequestMapping(value = "/getShipmentTemplate/{authToken}", method = RequestMethod.GET)
    public JsonObject getShipmentTemplate(@PathVariable final String authToken,
            @RequestParam final Long shipmentTemplateId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

            final ShipmentTemplate template = shipmentTemplateDao.findOne(shipmentTemplateId);
            checkCompanyAccess(user, template);

            return createSuccessResponse(createSerializer(user).toJson(new ShipmentTemplateDto(template)));
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
    @RequestMapping(value = "/deleteShipmentTemplate/{authToken}", method = RequestMethod.GET)
    public JsonObject deleteShipmentTemplate(@PathVariable final String authToken,
            @RequestParam final Long shipmentTemplateId) {
        try {
            //check logged in.
            final User user = getLoggedInUser(authToken);
            checkAccess(user, Role.NormalUser);

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
        tpl.setId(dto.getId());
        tpl.setName(dto.getName());
        tpl.setShipmentDescription(dto.getShipmentDescription());
        tpl.setAddDateShipped(dto.isAddDateShipped());
        tpl.setDetectLocationForShippedFrom(dto.isDetectLocationForShippedFrom());
        tpl.setAlertSuppressionMinutes(dto.getAlertSuppressionMinutes());
        tpl.setCommentsForReceiver(dto.getCommentsForReceiver());
        tpl.setArrivalNotificationWithinKm(dto.getArrivalNotificationWithinKm());
        tpl.setExcludeNotificationsIfNoAlerts(dto.isExcludeNotificationsIfNoAlerts());
        tpl.setShutdownDeviceAfterMinutes(dto.getShutdownDeviceAfterMinutes());
        tpl.setNoAlertsAfterArrivalMinutes(dto.getNoAlertsAfterArrivalMinutes());
        tpl.setNoAlertsAfterStartMinutes(dto.getNoAlertsAfterStartMinutes());
        tpl.setShutDownAfterStartMinutes(dto.getShutDownAfterStartMinutes());
        return tpl;
    }
    /**
     * @param t shipment template.
     * @param dto shipment template DTO.
     */
    private void resolveAlertProfile(final ShipmentTemplate t, final ShipmentTemplateDto dto) {
        if (dto.getAlertProfile() != null) {
            t.setAlertProfile(alertProfileDao.findOne(dto.getAlertProfile()));
        }
    }
    /**
     * @param t shipment template.
     * @param dto shipment template DTO.
     */
    private void resolveShipped(final ShipmentTemplate t, final ShipmentTemplateDto dto) {
        final Set<Long> ids = new HashSet<>();
        if (dto.getShippedFrom() != null) {
            ids.add(dto.getShippedFrom());
        }
        if (dto.getShippedTo() != null) {
            ids.add(dto.getShippedTo());
        }

        final Map<Long, LocationProfile> map = EntityUtils.resolveEntities(locationProfileDao, ids);

        t.setShippedFrom(map.get(dto.getShippedFrom()));
        t.setShippedTo(map.get(dto.getShippedTo()));
    }
    /**
     * @param t shipment template.
     * @param dto shipment template DTO.
     */
    private void resolveNotificationSchedules(final ShipmentTemplate t,
            final ShipmentTemplateDto dto) {
        final Set<Long> ids = new HashSet<>();
        ids.addAll(dto.getAlertsNotificationSchedules());
        ids.addAll(dto.getArrivalNotificationSchedules());

        final Map<Long, NotificationSchedule> map = EntityUtils.resolveEntities(notificationScheduleDao, ids);
        resolveSchedules(map, dto.getAlertsNotificationSchedules(), t.getAlertsNotificationSchedules());
        resolveSchedules(map, dto.getArrivalNotificationSchedules(), t.getArrivalNotificationSchedules());
    }
    /**
     * @param source source map ID to schedule.
     * @param ids list of ID.
     * @param schedules list of notification schedules.
     */
    private void resolveSchedules(final Map<Long, NotificationSchedule> source,
            final List<Long> ids, final List<NotificationSchedule> schedules) {
        for (final Long id : ids) {
            final NotificationSchedule sched = source.get(id);
            if (sched != null) {
                schedules.add(sched);
            }
        }
    }
}
