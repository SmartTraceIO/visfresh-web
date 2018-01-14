/**
 *
 */
package com.visfresh.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.AutoStartShipmentConstants;
import com.visfresh.constants.ShipmentTemplateConstants;
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
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.SpringRoles;
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
    @Autowired
    private AutoStartShipmentDao dao;
    @Autowired
    private LocationProfileDao locationProfileDao;
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
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/saveAutoStartShipment", method = RequestMethod.POST)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject saveAutoStartShipment(final @RequestBody JsonObject defShipment) throws RestServiceException {
        final User user = getLoggedInUser();
        final AutoStartShipmentDto dto = createSerializer(user)
                .parseAutoStartShipmentDto(defShipment);

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
        cfg.setStartOnLeaveLocation(dto.isStartOnLeaveLocation());

        //set template fields
        fillTemplate(dto, tpl, user);
        shipmentTemplateDao.save(tpl);

        final Long id = dao.save(cfg).getId();
        return createIdResponse("defaultShipmentId", id);
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
        final Set<Long> locInterims = new HashSet<>(dto.getInterimStops());

        final Set<Long> allLoctions = new HashSet<Long>();
        allLoctions.addAll(locFroms);
        allLoctions.addAll(locTos);
        allLoctions.addAll(locInterims);

        final List<LocationProfile> loctions = locationProfileDao.findAll(allLoctions);
        //check company access
        for (final LocationProfile l : loctions) {
            checkCompanyAccess(user, l);
        }

        //resolve locations
        cfg.getShippedFrom().clear();
        cfg.getShippedTo().clear();
        cfg.getInterimStops().clear();
        for (final LocationProfile l : loctions) {
            if (locFroms.contains(l.getId())) {
                cfg.getShippedFrom().add(l);
            }
            if (locTos.contains(l.getId())) {
                cfg.getShippedTo().add(l);
            }
            if (locInterims.contains(l.getId())) {
                cfg.getInterimStops().add(l);
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
        tpl.setNoAlertsAfterStartMinutes(dto.getNoAlertsAfterStartMinutes());

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
        checkCompanyAccess(user, schedules);

        //resolve locations
        tpl.getAlertsNotificationSchedules().clear();
        tpl.getArrivalNotificationSchedules().clear();
        for (final NotificationSchedule n : schedules) {
            if (alerts.contains(n.getId())) {
                tpl.getAlertsNotificationSchedules().add(n);
            }
            if (arrivals.contains(n.getId())) {
                tpl.getArrivalNotificationSchedules().add(n);
            }
        }
    }
    /**
     * @param authToken authentication token.
     * @param autoStartShipmentId default shipment ID.
     * @return default shipment as JSON.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/getAutoStartShipment", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonObject getAutoStartShipment(
            @RequestParam final Long autoStartShipmentId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final AutoStartShipment cfg = dao.findOne(autoStartShipmentId);
        checkCompanyAccess(user, cfg);

        return createSuccessResponse(createSerializer(user).toJson(
                new AutoStartShipmentDto(cfg)));
    }
    /**
     * @param authToken authentication token.
     * @param autoStartShipmentId default shipment ID.
     * @return default shipment.
     * @throws AuthenticationException
     * @throws RestServiceException
     */
    @RequestMapping(value = "/deleteAutoStartShipment", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser})
    public JsonObject deleteAutoStartShipment(
            @RequestParam final Long autoStartShipmentId) throws RestServiceException {
        //check logged in.
        final User user = getLoggedInUser();
        final AutoStartShipment cfg = dao.findOne(autoStartShipmentId);
        checkCompanyAccess(user, cfg);
        dao.delete(cfg);

        return createSuccessResponse(null);
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of default shipments.
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getAutoStartShipments", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonElement getAutoStartShipments(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) throws RestServiceException {
        final Page page = (pageIndex != null && pageSize != null) ? new Page(pageIndex, pageSize) : null;

        //check logged in.
        final User user = getLoggedInUser();
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
    }
    /**
     * @param authToken authentication token.
     * @param pageIndex the page index.
     * @param pageSize the page size.
     * @return list of default shipments.
     * @throws AuthenticationException
     */
    @RequestMapping(value = "/getAutoStartTemplates", method = RequestMethod.GET)
    @Secured({SpringRoles.SmartTraceAdmin, SpringRoles.Admin, SpringRoles.BasicUser, SpringRoles.NormalUser})
    public JsonElement getAutoStartTemplates(
            @RequestParam(required = false) final Integer pageIndex,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam(required = false) final String sc,
            @RequestParam(required = false) final String so
            ) throws RestServiceException {
        return getAutoStartShipments(pageIndex, pageSize, sc, so);
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
            ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME,
            ShipmentTemplateConstants.SHIPMENT_DESCRIPTION,
            START_LOCATIONS,
            END_LOCATIONS,
            INTERIM_STOPS,
            ALERT_PROFILE_NAME
        };
    }
}
