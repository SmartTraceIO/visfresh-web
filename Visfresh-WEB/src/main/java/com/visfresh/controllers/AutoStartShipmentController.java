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
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Role;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.io.AutoStartShipmentDto;
import com.visfresh.io.json.AutoStartShipmentSerializer;
import com.visfresh.services.RestServiceException;

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
    @Autowired
    private AlertProfileDao alertProfileDao;
    @Autowired
    private NotificationScheduleDao notificationScheduleDao;
    @Autowired
    private ShipmentTemplateDao shipmentTemplateDao;

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

            checkAccess(user, Role.NormalUser);

            AutoStartShipment cfg;
            ShipmentTemplate tpl;
            if (dto.getId() != null) {
                cfg = dao.findOne(dto.getId());
                tpl = cfg.getTemplate();

                checkCompanyAccess(user, cfg);
                checkCompanyAccess(user, tpl);
                checkCompanyAccess(user, dao.findOne(dto.getId()));
            } else {
                cfg = new AutoStartShipment();
                cfg.setCompany(user.getCompany());

                tpl = new ShipmentTemplate();
                tpl.setCompany(user.getCompany());

                cfg.setTemplate(tpl);
            }

            setLocations(cfg, dto, user);

            //set autostart fields
            cfg.setPriority(dto.getPriority());
            cfg.setTemplate(tpl);
            cfg.setId(dto.getId());

            //set template fields
            fillTemplate(dto, tpl, user);
            shipmentTemplateDao.save(tpl);

            final Long id = dao.save(cfg).getId();
            return createIdResponse("defaultShipmentId", id);
        } catch (final Exception e) {
            log.error("Failed to save autostart template", e);
            return createErrorResponse(e);
        }
    }
    /**
     * @param cfg
     * @param dto
     * @param user
     * @throws RestServiceException
     */
    private void setLocations(final AutoStartShipment cfg, final AutoStartShipmentDto dto, final User user)
            throws RestServiceException {
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

        //resolve locations
        cfg.getShippedFrom().clear();
        cfg.getShippedTo().clear();
        for (final LocationProfile l : loctions) {
            if (locFroms.contains(l.getId())) {
                cfg.getShippedFrom().add(l);
            } else {
                cfg.getShippedTo().add(l);
            }
        }
    }
    /**
     * @param dto
     * @param tpl
     * @param user
     * @throws RestServiceException
     */
    private void fillTemplate(final AutoStartShipmentDto dto, final ShipmentTemplate tpl,
            final User user) throws RestServiceException {
        final AlertProfile ap = alertProfileDao.findOne(dto.getAlertProfile());
        checkCompanyAccess(user, ap);

        tpl.setAutostart(true);
        tpl.setAlertProfile(ap);
        tpl.setAlertSuppressionMinutes(dto.getAlertSuppressionMinutes());
        tpl.setArrivalNotificationWithinKm(dto.getArrivalNotificationWithinKm());
        tpl.setCommentsForReceiver(dto.getCommentsForReceiver());
        tpl.setName(dto.getName());
        tpl.setNoAlertsAfterArrivalMinutes(dto.getNoAlertsAfterArrivalMinutes());
        tpl.setShipmentDescription(dto.getShipmentDescription());
        tpl.setShutDownAfterStartMinutes(dto.getShutDownAfterStartMinutes());
        tpl.setShutdownDeviceAfterMinutes(dto.getShutdownDeviceAfterMinutes());
        tpl.setExcludeNotificationsIfNoAlerts(dto.isExcludeNotificationsIfNoAlerts());
        tpl.setAddDateShipped(dto.isAddDateShipped());

        //get locations.
        final Set<Long> alerts = new HashSet<>(dto.getAlertsNotificationSchedules());
        final Set<Long> arrivals = new HashSet<>(dto.getArrivalNotificationSchedules());

        final Set<Long> allLoctions = new HashSet<Long>();
        allLoctions.addAll(alerts);
        allLoctions.addAll(arrivals);

        final List<NotificationSchedule> schedules = notificationScheduleDao.findAll(allLoctions);
        //check company access
        for (final NotificationSchedule n : schedules) {
            checkCompanyAccess(user, n);
        }

        //resolve locations
        tpl.getAlertsNotificationSchedules().clear();
        tpl.getArrivalNotificationSchedules().clear();
        for (final NotificationSchedule n : schedules) {
            if (alerts.contains(n.getId())) {
                tpl.getAlertsNotificationSchedules().add(n);
            } else {
                tpl.getArrivalNotificationSchedules().add(n);
            }
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
            checkAccess(user, Role.BasicUser);

            final AutoStartShipment cfg = dao.findOne(autoStartShipmentId);
            checkCompanyAccess(user, cfg);

            return createSuccessResponse(createSerializer(user).toJson(
                    new AutoStartShipmentDto(cfg)));
        } catch (final Exception e) {
            log.error("Failed to get autostart template", e);
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
            checkAccess(user, Role.NormalUser);

            final AutoStartShipment cfg = dao.findOne(autoStartShipmentId);
            checkCompanyAccess(user, cfg);
            dao.delete(cfg);

            return createSuccessResponse(null);
        } catch (final Exception e) {
            log.error("Failed to delete autostart shipmemnt", e);
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
            checkAccess(user, Role.BasicUser);

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
            log.error("Failed to get autostart templates", e);
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
            ID
        };
    }
}
