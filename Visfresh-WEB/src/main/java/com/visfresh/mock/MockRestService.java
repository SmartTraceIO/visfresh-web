/**
 *
 */
package com.visfresh.mock;

import java.util.Date;
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
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceData;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.SchedulePersonHowWhen;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
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
    public final Map<Long, Notification> notifications = new ConcurrentHashMap<Long, Notification>();
    public final Map<Long, Alert> alerts = new ConcurrentHashMap<Long, Alert>();
    public final Map<Long, Arrival> arrivals = new ConcurrentHashMap<Long, Arrival>();
    public final Map<String, List<TrackerEvent>> trackerEvents = new ConcurrentHashMap<String, List<TrackerEvent>>();

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
    public Long saveAlertProfile(final AlertProfile alert) {
        if (alert.getId() == null) {
            alert.setId(ids.incrementAndGet());
            synchronized (alertProfiles) {
                alertProfiles.put(alert.getId(), alert);
            }
        }
        return alert.getId();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getAlertProfiles()
     */
    @Override
    public List<AlertProfile> getAlertProfiles() {
        synchronized (alertProfiles) {
            return new LinkedList<AlertProfile>(alertProfiles.values());
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveLocationProfile(com.visfresh.entities.LocationProfile)
     */
    @Override
    public Long saveLocationProfile(final LocationProfile profile) {
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
    public List<LocationProfile> getLocationProfiles() {
        synchronized (locationProfiles) {
            return new LinkedList<LocationProfile>(locationProfiles.values());
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveNotificationSchedule(com.visfresh.entities.NotificationSchedule)
     */
    @Override
    public Long saveNotificationSchedule(final NotificationSchedule schedule) {
        if (schedule.getId() == null) {
            schedule.setId(ids.incrementAndGet());
            for (final SchedulePersonHowWhen s : schedule.getSchedules()) {
                s.setId(ids.incrementAndGet());
            }
            synchronized (notificationSchedules) {
                notificationSchedules.put(schedule.getId(), schedule);
            }
        }
        return schedule.getId();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getNotificationSchedules()
     */
    @Override
    public List<NotificationSchedule> getNotificationSchedules() {
        synchronized (notificationSchedules) {
            return new LinkedList<NotificationSchedule>(notificationSchedules.values());
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveShipmentTemplate(com.visfresh.entities.ShipmentTemplate)
     */
    @Override
    public Long saveShipmentTemplate(final ShipmentTemplate tpl) {
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
    public List<ShipmentTemplate> getShipmentTemplates() {
        synchronized (shipmentTemplates) {
            return new LinkedList<ShipmentTemplate>(shipmentTemplates.values());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getDevices()
     */
    @Override
    public List<Device> getDevices() {
        synchronized (devices) {
            return new LinkedList<Device>(devices.values());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveDevice(com.visfresh.entities.Device)
     */
    @Override
    public void saveDevice(final Device device) {
        synchronized (devices) {
            devices.put(device.getImei(), device);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipments()
     */
    @Override
    public List<Shipment> getShipments() {
        synchronized (shipments) {
            return new LinkedList<Shipment>(shipments.values());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getNotifications(java.lang.Long)
     */
    @Override
    public List<Notification> getNotifications(final Long id) {
        final Shipment shipment = shipments.get(id);
        if (shipment == null) {
            return new LinkedList<Notification>();
        }

        //create device IMEI set
        final Set<String> devices = new HashSet<String>();
        for (final Device d : shipment.getDevices()) {
            devices.add(d.getImei());
        }

        final List<Notification> result = new LinkedList<Notification>(notifications.values());
        final Iterator<Notification> iter = result.iterator();

        while (iter.hasNext()) {
            final Object issue = iter.next().getIssue();

            if (issue instanceof Alert) {
                if (!isOwnAlert(shipment, (Alert) issue)) {
                    iter.remove();
                }
            } else if (issue instanceof Arrival) {
                if (!isOwnArrival(shipment, (Arrival) issue)) {
                    iter.remove();
                }
            } else {
                iter.remove();
            }
        }

        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#markNotificationsAsRead(java.util.List)
     */
    @Override
    public void markNotificationsAsRead(final User user, final List<Long> ids) {
        //TODO remove notifications only for given user.
        for (final Long id : ids) {
            notifications.remove(id);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getShipmentData(java.util.Date, java.util.Date, java.lang.String)
     */
    @Override
    public List<ShipmentData> getShipmentData(final Date startDate, final Date endDate,
            final String onlyWithAlerts) {
        //device data map
        final Map<String, DeviceData> deviceData= new HashMap<String, DeviceData>();

        //add alerts
        for (final Alert a : new LinkedList<Alert>(alerts.values())) {
            final String imei = a.getDevice().getImei();

            DeviceData data = deviceData.get(imei);
            if (data == null) {
                data = new DeviceData();
                data.setDevice(a.getDevice());
                deviceData.put(imei, data);
            }

            data.getAlerts().add(a);
        }

        //add tracker events
        for (final Map.Entry<String, DeviceData> e : deviceData.entrySet()) {
            final String imei = e.getKey();
            final List<TrackerEvent> te = trackerEvents.get(imei);
            if (te != null) {
                e.getValue().getEvents().addAll(te);
            }
        }

        //build result
        final List<ShipmentData> result = new LinkedList<ShipmentData>();
        for (final Shipment s : new LinkedList<Shipment>(shipments.values())) {
            final ShipmentData sd = new ShipmentData();
            sd.setShipment(s);

            for (final Device d : s.getDevices()) {
                final DeviceData dd = deviceData.get(d.getImei());
                if (dd != null) {
                    sd.getDeviceData().add(dd);
                }
            }

            if (sd.getDeviceData().size() > 0) {
                result.add(sd);
            }
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    public Long saveShipment(final Shipment shipment) {
        if (shipment.getId() == null) {
            shipment.setId(ids.incrementAndGet());
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
    public Long createShipmentTemplate(final Shipment shipment, final String templateName) {
        final ShipmentTemplate tpl = new ShipmentTemplate(shipment);
        tpl.setName(templateName);
        return saveShipmentTemplate(tpl);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#sendCommandToDevice(com.visfresh.entities.Device, java.lang.String)
     */
    @Override
    public void sendCommandToDevice(final Device device, final String command) {
        // TODO Auto-generated method stub
    }

    /**
     * @param shipment
     * @param issue
     * @return
     */
    private boolean isOwnArrival(final Shipment shipment, final Arrival issue) {
        final String imei = issue.getDevice().getImei();
        for (final Device d : shipment.getDevices()) {
            if (d.getImei().equals(imei)) {
                return true;
            }
        }
        return false;
    }
    /**
     * @param shipment
     * @param isue
     * @return
     */
    private boolean isOwnAlert(final Shipment shipment, final Alert issue) {
        final String imei = issue.getDevice().getImei();
        for (final Device d : shipment.getDevices()) {
            if (d.getImei().equals(imei)) {
                return true;
            }
        }
        return false;
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
    }
}
