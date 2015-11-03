/**
 *
 */
package com.visfresh.mock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.ShipmentStateDto;
import com.visfresh.mpl.services.RestServiceImpl;
import com.visfresh.services.RestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockRestService implements RestService {
    public final AtomicLong ids = new AtomicLong(1);

    public final Map<Long, AlertProfile> alertProfiles = new ConcurrentHashMap<Long, AlertProfile>();
    public final Map<Long, LocationProfile> locationProfiles = new ConcurrentHashMap<Long, LocationProfile>();
    public final Map<Long, NotificationSchedule> notificationSchedules = new ConcurrentHashMap<Long, NotificationSchedule>();
    public final Map<Long, ShipmentTemplate> shipmentTemplates = new ConcurrentHashMap<Long, ShipmentTemplate>();
    public final Map<String, Device> devices = new ConcurrentHashMap<String, Device>();
    public final Map<Long, Shipment> shipments = new ConcurrentHashMap<Long, Shipment>();
    public final Map<String, List<Notification>> notifications = new ConcurrentHashMap<String, List<Notification>>();
    public final Map<Long, Alert> alerts = new ConcurrentHashMap<Long, Alert>();
    public final Map<Long, Arrival> arrivals = new ConcurrentHashMap<Long, Arrival>();
    public final Map<String, List<TrackerEvent>> trackerEvents = new ConcurrentHashMap<String, List<TrackerEvent>>();
    public final Map<String, UserProfile> profiles = new HashMap<String, UserProfile>();
    public final Map<Long, Company> companies = new HashMap<Long, Company>();

    /**
     * Default constructor.
     */
    public MockRestService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveAlertProfile(com.visfresh.entities.AlertProfile)
     */
    @Override
    public Long saveAlertProfile(final Company company, final AlertProfile alert) {
        if (alert.getId() == null) {
            alert.setId(ids.incrementAndGet());
            synchronized (alertProfiles) {
                alertProfiles.put(alert.getId(), alert);
            }
        }
        return alert.getId();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#deleteAlertProfile(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public void deleteAlertProfile(final Company company, final Long alertProfileId) {
        alertProfiles.remove(alertProfileId);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getAlertProfiles()
     */
    @Override
    public List<AlertProfile> getAlertProfiles(final Company company) {
        synchronized (alertProfiles) {
            return new LinkedList<AlertProfile>(alertProfiles.values());
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveLocationProfile(com.visfresh.entities.LocationProfile)
     */
    @Override
    public Long saveLocation(final Company company, final LocationProfile profile) {
        if (profile.getId() == null) {
            profile.setId(ids.incrementAndGet());
            synchronized (locationProfiles) {
                locationProfiles.put(profile.getId(), profile);
            }
        }
        return profile.getId();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getLocationProfiles()
     */
    @Override
    public List<LocationProfile> getLocation(final Company company) {
        synchronized (locationProfiles) {
            return new LinkedList<LocationProfile>(locationProfiles.values());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#deleteLocation(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public void deleteLocation(final Company company, final Long locationId) {
        synchronized (locationProfiles) {
            locationProfiles.remove(locationId);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveNotificationSchedule(com.visfresh.entities.NotificationSchedule)
     */
    @Override
    public Long saveNotificationSchedule(final Company company, final NotificationSchedule schedule) {
        if (schedule.getId() == null) {
            schedule.setId(ids.incrementAndGet());
            synchronized (notificationSchedules) {
                notificationSchedules.put(schedule.getId(), schedule);
            }
        }
        for (final PersonSchedule s : schedule.getSchedules()) {
            if (s.getId() == null) {
                s.setId(ids.incrementAndGet());
            }
        }
        return schedule.getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getNotificationSchedules()
     */
    @Override
    public List<NotificationSchedule> getNotificationSchedules(final Company company) {
        synchronized (notificationSchedules) {
            return new LinkedList<NotificationSchedule>(notificationSchedules.values());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#deleteNotificationSchedule(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public void deleteNotificationSchedule(final Company company,
            final Long notificationScheduleId) {
        synchronized (notificationSchedules) {
            notificationSchedules.remove(notificationScheduleId);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveShipmentTemplate(com.visfresh.entities.ShipmentTemplate)
     */
    @Override
    public Long saveShipmentTemplate(final Company company, final ShipmentTemplate tpl) {
        if (tpl.getId() == null) {
            tpl.setId(ids.incrementAndGet());
            synchronized (shipmentTemplates ) {
                shipmentTemplates.put(tpl.getId(), tpl);
            }
        }
        return tpl.getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipmentTemplates()
     */
    @Override
    public List<ShipmentTemplate> getShipmentTemplates(final Company company) {
        synchronized (shipmentTemplates) {
            return new LinkedList<ShipmentTemplate>(shipmentTemplates.values());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getDevices()
     */
    @Override
    public List<Device> getDevices(final Company company) {
        synchronized (devices) {
            return new LinkedList<Device>(devices.values());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveDevice(com.visfresh.entities.Device)
     */
    @Override
    public void saveDevice(final Company company, final Device device) {
        synchronized (devices) {
            devices.put(device.getId(), device);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#deleteDevice(com.visfresh.entities.Company, java.lang.String)
     */
    @Override
    public void deleteDevice(final Company company, final String imei) {
        devices.remove(imei);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipments()
     */
    @Override
    public List<ShipmentStateDto> getShipments(final Company company) {
        LinkedList<Shipment> list;
        synchronized (shipments) {
            list = new LinkedList<Shipment>(shipments.values());
        }


        final List<ShipmentStateDto> result = new LinkedList<ShipmentStateDto>();
        for (final Shipment s : list) {
            final ShipmentStateDto dto = new ShipmentStateDto(s);
            result.add(dto);
            final List<Alert> shipmentAlerts = getShipmentAlerts(s);
            if (shipmentAlerts != null) {
                dto.getAlertSummary().putAll(RestServiceImpl.toSummaryMap(shipmentAlerts));
            }
        }
        return result;
    }
    /**
     * @param s
     * @return
     */
    private List<Alert> getShipmentAlerts(final Shipment s) {
        final List<Alert> result = new LinkedList<Alert>();
        for (final Alert a : new LinkedList<Alert>(alerts.values())) {
            if (a.getShipment().getId().equals(s.getId())) {
                result.add(a);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getNotifications(java.lang.Long)
     */
    @Override
    public List<Notification> getNotifications(final User user) {
        final List<Notification> result = notifications.get(user.getLogin());
        if (result == null) {
            return new LinkedList<Notification>();
        }
        return new LinkedList<Notification>(result);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#markNotificationsAsRead(java.util.List)
     */
    @Override
    public void markNotificationsAsRead(final User user, final Set<Long> ids) {
        final Set<Long> idSet = new HashSet<Long>(ids);

        final List<Notification> result = notifications.get(user.getLogin());
        if (result != null) {
            final Iterator<Notification> iter = result.iterator();
            while (iter.hasNext()) {
                if (idSet.contains(iter.next().getId())) {
                    iter.remove();
                }
            }
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    public Long saveShipment(final Company company, final Shipment shipment) {
        if (shipment.getId() == null) {
            shipment.setId(ids.incrementAndGet());
            shipment.setCompany(company);
            synchronized (shipments) {
                shipments.put(shipment.getId(), shipment);
            }
        }
        return shipment.getId();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#createShipmentTemplate(com.visfresh.entities.Shipment, java.lang.String)
     */
    @Override
    public Long createShipmentTemplate(final Company company, final Shipment shipment, final String templateName) {
        final ShipmentTemplate tpl = new ShipmentTemplate(shipment);
        tpl.setName(templateName);
        return saveShipmentTemplate(company, tpl);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#sendCommandToDevice(com.visfresh.entities.Device, java.lang.String)
     */
    @Override
    public void sendCommandToDevice(final DeviceCommand cmd) {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getAlertProfile(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public AlertProfile getAlertProfile(final Company company, final Long id) {
        return alertProfiles.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getDevice(com.visfresh.entities.Company, java.lang.String)
     */
    @Override
    public Device getDevice(final Company company, final String id) {
        return devices.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getLocationProfile(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public LocationProfile getLocationProfile(final Company company, final Long id) {
        return locationProfiles.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipment(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public Shipment getShipment(final Company company, final Long id) {
        return shipments.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#deleteShipment(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public void deleteShipment(final Company company, final Long shipmentId) {
        shipments.remove(shipmentId);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipmentTemplate(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public ShipmentTemplate getShipmentTemplate(final Company company, final Long id) {
        return shipmentTemplates.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#deleteShipmentTemplate(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public void deleteShipmentTemplate(final Company company, final Long id) {
        shipmentTemplates.remove(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getNotificationSchedule(com.visfresh.entities.Company, java.lang.Long)
     */
    @Override
    public NotificationSchedule getNotificationSchedule(final Company company, final Long id) {
        return notificationSchedules.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getProfile(com.visfresh.entities.User)
     */
    @Override
    public UserProfile getProfile(final User user) {
        return profiles.get(user.getLogin());
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getCompany(java.lang.Long)
     */
    @Override
    public Company getCompany(final Long id) {
        return companies.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getCompanies()
     */
    @Override
    public List<Company> getCompanies() {
        return new LinkedList<Company>(companies.values());
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveUserProfile(com.visfresh.entities.User, com.visfresh.entities.UserProfile)
     */
    @Override
    public void saveUserProfile(final User user, final UserProfile p) {
        // check hacking
        final Long id = user.getCompany().getId();
        for (final Shipment s : p.getShipments()) {
            if (!s.getCompany().getId().equals(id)) {
                return;
            }
        }

        profiles.put(user.getLogin(), p);
    }

    /**
     * Cleares all caches.
     */
    public void clear() {
        alertProfiles.clear();
        locationProfiles.clear();
        notificationSchedules.clear();
        shipmentTemplates.clear();
        devices.clear();
        shipments.clear();
        notifications.clear();
        alerts.clear();
        arrivals.clear();
        trackerEvents.clear();
        profiles.clear();
        companies.clear();
    }

    /**
     * @param id
     * @param e
     */
    public void addTrackerEvent(final String id, final TrackerEvent e) {
        List<TrackerEvent> events = trackerEvents.get(id);
        if (events == null) {
            events = new LinkedList<TrackerEvent>();
            trackerEvents.put(id, events);
        }
        events.add(e);
    }
}
