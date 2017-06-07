/**
 *
 */
package com.visfresh.controllers;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Company;
import com.visfresh.entities.EntityWithId;
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
    @Autowired
    private UserDao userDao;
    @Autowired
    private CompanyDao companyDao;

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
        resolveUserAccess(user, t, dto);
        resolveCompanyAccess(user, t, dto);
    }
    /**
     * @param user user.
     * @param t shipment.
     * @param dto shipment DTO
     * @throws RestServiceException
     */
    private void resolveUserAccess(final User user, final ShipmentBase t, final ShipmentBaseDto dto)
            throws RestServiceException {
        final Set<Long> ids = new HashSet<>();
        ids.addAll(dto.getUserAccess());

        final Map<Long, User> map = EntityUtils.resolveEntities(userDao, ids);
        resolveEntities(map, dto.getUserAccess(), t.getUserAccess());
    }
    /**
     * @param user user.
     * @param t shipment.
     * @param dto shipment DTO
     * @throws RestServiceException
     */
    private void resolveCompanyAccess(final User user, final ShipmentBase t, final ShipmentBaseDto dto)
            throws RestServiceException {
        final Set<Long> ids = new HashSet<>();
        ids.addAll(dto.getCompanyAccess());

        final Map<Long, Company> map = EntityUtils.resolveEntities(companyDao, ids);
        resolveEntities(map, dto.getCompanyAccess(), t.getCompanyAccess());
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

        resolveEntities(map, dto.getAlertsNotificationSchedules(), t.getAlertsNotificationSchedules());
        resolveEntities(map, dto.getArrivalNotificationSchedules(), t.getArrivalNotificationSchedules());
    }
    /**
     * @param s shipment base.
     * @param interimIds locations.
     * @throws RestServiceException
     */
    protected void saveAlternativeAndInterimLoations(final User user, final ShipmentBase s,
            final List<Long> interimIds, final List<Long> alternativeEndIds) throws RestServiceException {
        //interim locations
        Collection<LocationProfile> interims = null;
        if (interimIds != null) {
            interims = EntityUtils.resolveEntities(
                    locationProfileDao, new HashSet<Long>(interimIds)).values();
            checkCompanyAccess(user, interims);
        }

        //alternative ends
        Collection<LocationProfile> alternativeEnds = null;
        if (alternativeEndIds != null) {
            alternativeEnds = EntityUtils.resolveEntities(
                    locationProfileDao, new HashSet<Long>(alternativeEndIds)).values();
            checkCompanyAccess(user, alternativeEnds);
        }

        saveAlternativeAndInterimLoations(s, interims, alternativeEnds);
    }

    /**
     * @param s
     * @param interims
     * @param alternativeEnds
     */
    protected void saveAlternativeAndInterimLoations(final ShipmentBase s, final Collection<LocationProfile> interims,
            final Collection<LocationProfile> alternativeEnds) {
        AlternativeLocations a = alternativeLocationsDao.getBy(s);
        if (a == null && (interims != null || alternativeEnds != null)) {
            a = new AlternativeLocations();
        }

        if (interims != null) {
            //save interim locations
            a.getInterim().clear();
            a.getInterim().addAll(interims);
        }
        if (alternativeEnds != null) {
            //save interim locations
            a.getTo().clear();
            a.getTo().addAll(alternativeEnds);
        }

        if (a != null) {
            alternativeLocationsDao.save(s, a);
        }
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
    private <ID extends Serializable & Comparable<ID>, E extends EntityWithId<ID>> void resolveEntities(
            final Map<ID, E> source,
            final List<ID> ids, final List<E> schedules) {
        for (final ID id : ids) {
            final E sched = source.get(id);
            if (sched != null) {
                schedules.add(sched);
            }
        }
    }
    /**
     * @param dto
     * @param tpl
     */
    protected <T extends ShipmentBase> T copyBaseData(final ShipmentBaseDto dto, final T tpl) {
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
        return tpl;
    }
}
