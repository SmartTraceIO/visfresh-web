/**
 *
 */
package com.visfresh.controllers;

import java.util.HashMap;
import java.util.Map;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UserResolver;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MockReferenceResolver implements ReferenceResolver, ShipmentResolver, UserResolver {
    private final Map<Long, LocationProfile> locationProfiles = new HashMap<Long, LocationProfile>();
    private final Map<Long, AlertProfile> alertProfiles = new HashMap<Long, AlertProfile>();
    private final Map<Long, NotificationSchedule> notificationSchedules = new HashMap<Long, NotificationSchedule>();
    private final Map<String, Device> devices = new HashMap<String, Device>();
    private final Map<Long, Shipment> shipments = new HashMap<Long, Shipment>();
    private final Map<Long, Company> companies = new HashMap<Long, Company>();
    private final Map<Long, User> users = new HashMap<Long, User>();

    /**
     * Default constructor.
     */
    public MockReferenceResolver() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getLocationProfile(java.lang.Long)
     */
    @Override
    public LocationProfile getLocationProfile(final Long id) {
        return locationProfiles.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getAlertProfile(java.lang.Long)
     */
    @Override
    public AlertProfile getAlertProfile(final Long id) {
        return alertProfiles.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getNotificationSchedule(java.lang.Long)
     */
    @Override
    public NotificationSchedule getNotificationSchedule(final Long id) {
        return notificationSchedules.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getDevice(java.lang.String)
     */
    @Override
    public Device getDevice(final String id) {
        return devices.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getShipment(java.lang.Long)
     */
    @Override
    public Shipment getShipment(final Long id) {
        return shipments.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.UserResolver#getUser(java.lang.String)
     */
    @Override
    public User getUser(final Long id) {
        return users.get(id);
    }

    public void add(final Device t) {
        devices.put(t.getId(), t);
    }
    public void add(final LocationProfile p) {
        locationProfiles.put(p.getId(), p);
    }
    public void add(final AlertProfile p) {
        alertProfiles.put(p.getId(), p);
    }
    public void add(final NotificationSchedule s) {
        notificationSchedules.put(s.getId(), s);
    }
    public void add(final Shipment s) {
        shipments.put(s.getId(), s);
    }
    public void add(final Company s) {
        companies.put(s.getId(), s);
    }
    public void add(final User u) {
        users.put(u.getId(), u);
    }
}
