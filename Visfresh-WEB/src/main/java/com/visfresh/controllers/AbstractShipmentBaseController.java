/**
 *
 */
package com.visfresh.controllers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.User;
import com.visfresh.io.ShipmentBaseDto;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.EntityUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractShipmentBaseController extends AbstractController {
    @Autowired
    private AlertProfileDao alertProfileDao;
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private NotificationScheduleDao notificationScheduleDao;
    @Autowired
    private AlternativeLocationsDao alternativeLocationsDao;

    /**
     * Default constructor.
     */
    public AbstractShipmentBaseController() {
        super();
    }

    /**
     * @param user
     * @param dto
     * @param t
     * @throws RestServiceException
     */
    protected void resolveReferences(final User user,
            final ShipmentBaseDto dto, final ShipmentBase t)
            throws RestServiceException {
        resolveAlertProfile(user, t, dto);
        resolveLocations(user, t, dto);
        resolveNotificationSchedules(user, t, dto);
    }
    /**
     * @param s shipment template.
     * @param dto shipment template DTO.
     * @throws RestServiceException
     */
    protected void resolveAlertProfile(final User user, final ShipmentBase s, final ShipmentBaseDto dto)
            throws RestServiceException {
        if (dto.getAlertProfile() != null) {
            s.setAlertProfile(alertProfileDao.findOne(dto.getAlertProfile()));
            checkCompanyAccess(user, s.getAlertProfile());
        }
    }
    /**
     * @param t shipment template.
     * @param dto shipment template DTO.
     * @throws RestServiceException
     */
    protected void resolveLocations(final User user, final ShipmentBase t, final ShipmentBaseDto dto)
            throws RestServiceException {
        final Set<Long> ids = new HashSet<>();
        if (dto.getShippedFrom() != null) {
            ids.add(dto.getShippedFrom());
        }
        if (dto.getShippedTo() != null) {
            ids.add(dto.getShippedTo());
        }

        final Map<Long, LocationProfile> map = EntityUtils.resolveEntities(locationProfileDao, ids);
        checkCompanyAccess(user, map.values());

        t.setShippedFrom(map.get(dto.getShippedFrom()));
        t.setShippedTo(map.get(dto.getShippedTo()));
    }
    /**
     * @param t shipment template.
     * @param dto shipment template DTO.
     * @throws RestServiceException
     */
    protected void resolveNotificationSchedules(final User user, final ShipmentBase t,
            final ShipmentBaseDto dto) throws RestServiceException {
        final Set<Long> ids = new HashSet<>();
        ids.addAll(dto.getAlertsNotificationSchedules());
        ids.addAll(dto.getArrivalNotificationSchedules());

        final Map<Long, NotificationSchedule> map = EntityUtils.resolveEntities(notificationScheduleDao, ids);
        checkCompanyAccess(user, map.values());

        resolveSchedules(map, dto.getAlertsNotificationSchedules(), t.getAlertsNotificationSchedules());
        resolveSchedules(map, dto.getArrivalNotificationSchedules(), t.getArrivalNotificationSchedules());
    }
    /**
     * @param s shipment base.
     * @param locs locations.
     * @throws RestServiceException
     */
    protected void saveAlternativeAndInterimLoations(final User user, final ShipmentBase s,
            final List<Long> locs, final List<Long> alternativeEnds) throws RestServiceException {
        if (locs == null && alternativeEnds == null) {
            return;
        }

        AlternativeLocations a = alternativeLocationsDao.getBy(s);
        if (a == null && (locs != null || alternativeEnds != null)) {
            a = new AlternativeLocations();
        }

        if (locs != null) {
            //save interim locations
            final Map<Long, LocationProfile> map = EntityUtils.resolveEntities(
                    locationProfileDao, new HashSet<Long>(locs));
            checkCompanyAccess(user, map.values());

            a.getInterim().clear();
            a.getInterim().addAll(map.values());
        }
        if (alternativeEnds != null) {
            //save interim locations
            final Map<Long, LocationProfile> map = EntityUtils.resolveEntities(
                    locationProfileDao, new HashSet<Long>(alternativeEnds));
            checkCompanyAccess(user, map.values());

            a.getTo().clear();
            a.getTo().addAll(map.values());
        }

        if (a != null) {
            saveAlternativeLoations(s, a);
        }
    }
    /**
     * @param s shipment.
     * @param values locations.
     */
    protected void saveAlternativeLoations(final ShipmentBase s, final AlternativeLocations a) {
        alternativeLocationsDao.save(s, a);
    }
    /**
     * @param dto shipment DTO.
     * @param s shipment.
     */
    protected void addInterimLocations(final ShipmentBaseDto dto, final ShipmentBase s) {
        final AlternativeLocations a = alternativeLocationsDao.getBy(s);
        dto.setInterimLocations(new LinkedList<>(EntityUtils.getIdList(a.getInterim())));
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
    /**
     * @param dto
     * @param tpl
     */
    protected void copyBaseData(final ShipmentBaseDto dto, final ShipmentBase tpl) {
        tpl.setId(dto.getId());
        tpl.setShipmentDescription(dto.getShipmentDescription());
        tpl.setAlertSuppressionMinutes(dto.getAlertSuppressionMinutes());
        tpl.setCommentsForReceiver(dto.getCommentsForReceiver());
        tpl.setArrivalNotificationWithinKm(dto.getArrivalNotificationWithinKm());
        tpl.setExcludeNotificationsIfNoAlerts(dto.isExcludeNotificationsIfNoAlerts());
        tpl.setShutdownDeviceAfterMinutes(dto.getShutdownDeviceAfterMinutes());
        tpl.setNoAlertsAfterArrivalMinutes(dto.getNoAlertsAfterArrivalMinutes());
        tpl.setNoAlertsAfterStartMinutes(dto.getNoAlertsAfterStartMinutes());
        tpl.setShutDownAfterStartMinutes(dto.getShutDownAfterStartMinutes());
        tpl.setSendArrivalReport(dto.isSendArrivalReport());
        tpl.setSendArrivalReportOnlyIfAlerts(dto.isSendArrivalReportOnlyIfAlerts());
    }
}
