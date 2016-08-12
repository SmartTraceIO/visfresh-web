/**
 *
 */
package com.visfresh.rules;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.rules.state.LeaveLocationState;
import com.visfresh.services.AutoStartShipmentService;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AutoStartOnLeaveStartLocationRule implements TrackerEventRule {
    protected static final long CHECK_TIMEOUT = 60 * 60 * 1000L;
    public static final String NAME = "AutoStartOnLeaveStartLocation";
    private static final Logger log = LoggerFactory.getLogger(AutoStartOnLeaveStartLocationRule.class);
    protected static final int CONTROL_DISTANCE = 5000; //meters

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private AutoStartShipmentDao autoStartDao;
    @Autowired
    private AutoStartShipmentService autoStartService;
    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public AutoStartOnLeaveStartLocationRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final Shipment s = e.getShipment();
        if (e.getLatitude() == null
            || e.getLongitude() == null
            || s != null && !s.hasFinalStatus()
            || e.getDevice().getAutostartTemplateId() == null) {
            return false;
        }

        //get autostart configuration
        final AutoStartShipment autoStart = findAutoStart(e.getDevice().getAutostartTemplateId());
        if (autoStart == null || !autoStart.isStartOnLeaveLocation()) {
            return false;
        }

        //check need start to watch
        final LeaveLocationState state = getLeaveLocationState(context.getDeviceState());
        final LocationProfile startLocation = getCurrentHostLocation(autoStart, e);

        //device entered one from start locations, need start to watch
        if (state == null && startLocation != null) {
            return true;
        }

        //device has entered start location before but leave it now
        if (state != null && startLocation == null) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final AutoStartShipment autoStart = findAutoStart(e.getDevice().getAutostartTemplateId());

        //check need start to watch
        final LeaveLocationState state = getLeaveLocationState(context.getDeviceState());

        //device entered one from start locations, need start to watch
        if (state == null) {
            final LocationProfile startLocation = getCurrentHostLocation(autoStart, e);
            log.debug("The device " + e.getDevice().getImei()
                    + " has entered autostart location "
                    + startLocation.getName() + ", the watch is started for it");
            saveLeaveLocationState(new LeaveLocationState(startLocation), context.getDeviceState());
            return false;
        }

        //device has entered start location before but leave it now
        if (state.getLeaveOn() == null) {
            log.debug("The device " + e.getDevice().getImei()
                    + " possible leave the location "
                    + state.getName() + ", the watch is started for it");
            //need only start to watch of leaving location
            state.setLeaveOn(e.getTime());
            saveLeaveLocationState(state, context.getDeviceState());
            return false;
        }

        //check time out
        if (e.getTime().getTime() - state.getLeaveOn().getTime() > CHECK_TIMEOUT) {
            //clear leave start location watch
            clearLeaveLocationState(context.getDeviceState());

            //should autostart shipment
            final Shipment s = autoStartNewShipment(e.getDevice(),
                    state.getLatitude(), state.getLongitude(), state.getLeaveOn());
            e.setShipment(s);
            assignShipment(e.getId(), s);

            //possible assign given shipment to old events.
            final List<ShortTrackerEvent> oldEvents = findTrackerEvents(
                    e.getDevice().getImei(), state.getLeaveOn(), e.getTime());

            final Map<Long, List<ShortTrackerEvent>> assignedEvents = new HashMap<>();
            //process not assigned shipments
            //and collect assigned
            for (final ShortTrackerEvent ev : oldEvents) {
                if (ev.getShipmentId() == null) {
                    assignShipment(ev.getId(), s);
                } else if (!ev.getShipmentId().equals(s.getId())){
                    List<ShortTrackerEvent> events = assignedEvents.get(ev.getShipmentId());
                    if (events == null) {
                        events = new LinkedList<>();
                        assignedEvents.put(ev.getShipmentId(), events);
                    }

                    events.add(ev);
                }
            }

            //process assigned events
            for (final Map.Entry<Long, List<ShortTrackerEvent>> entry : assignedEvents.entrySet()) {
                final Shipment oldShipment = getShipment(entry.getKey());
                if (oldShipment.getStatus() == ShipmentStatus.Arrived) {
                    for (final ShortTrackerEvent ev : entry.getValue()) {
                        assignShipment(ev.getId(), s);
                    }
                }
            }

            return true;
        }

        return false;
    }
    /**
     * @param id shipment ID.
     * @return
     */
    protected Shipment getShipment(final Long id) {
        return shipmentDao.findOne(id);
    }

    /**
     * @param autoStart autostart configuration.
     * @param e tracker event.
     * @return first found location profile.
     */
    private LocationProfile getCurrentHostLocation(final AutoStartShipment autoStart,
            final TrackerEvent e) {
        for (final LocationProfile p : autoStart.getShippedFrom()) {
            if (!isOutsideLocation(p, e)) {
                return p;
            }
        }
        return null;
    }
    /**
     * @param loc location.
     * @param e tracker event.
     * @return true if is outside of start location.
     */
    private static boolean isOutsideLocation(final LocationProfile loc,
            final TrackerEvent e) {
        final Location l = loc.getLocation();
        final int radius = loc.getRadius();
        return isOutsideLocation(e, l, radius);
    }

    /**
     * @param e tracker event.
     * @param l location.
     * @param radius location radius.
     * @return true if the tracker event is out of location by given raduis.
     */
    public static boolean isOutsideLocation(final TrackerEvent e,
            final Location l, final int radius) {
        final int distance = (int) Math.round(LocationUtils.getDistanceMeters(
                e.getLatitude(),
                e.getLongitude(),
                l.getLatitude(),
                l.getLongitude()));

        //if outside of location radius.
        if (distance > radius + CONTROL_DISTANCE) {
            return true;
        }

        return false;
    }
    /**
     * @param state device state.
     * @return leave start location state.
     */
    public static LeaveLocationState getLeaveLocationState(final DeviceState state) {
        final String str = state.getProperty("leaveLocationState");
        if (str != null) {
            final JsonObject json = SerializerUtils.parseJson(str).getAsJsonObject();

            final LeaveLocationState ls = new LeaveLocationState();
            ls.setLatitude(json.get("latitude").getAsDouble());
            ls.setLongitude(json.get("longitude").getAsDouble());
            ls.setName(json.get("name").getAsString());
            ls.setLocationId(json.get("locationId").getAsLong());
            ls.setLeaveOn(parseDate(json.get("leaveOn")));
            ls.setRadius(json.get("radius").getAsInt());
            return ls;
        }
        return null;
    }
    /**
     * @param s leave location state.
     * @param deviceState device state.
     * @return leave start location state.
     */
    protected static void saveLeaveLocationState(final LeaveLocationState s, final DeviceState deviceState) {
        final JsonObject json = new JsonObject();

        json.addProperty("latitude", s.getLatitude());
        json.addProperty("longitude", s.getLongitude());
        json.addProperty("name", s.getName());
        json.addProperty("locationId", s.getLocationId());
        if (s.getLeaveOn() != null) {
            json.addProperty("leaveOn", createDateFormat().format(s.getLeaveOn()));
        }
        json.addProperty("radius", s.getRadius());

        deviceState.setProperty("leaveLocationState", json.toString());
    }
    /**
     * @param deviceState
     */
    public static void clearLeaveLocationState(final DeviceState deviceState) {
        deviceState.setProperty("leaveLocationState", null);
    }

    /**
     * @param el
     * @return
     */
    private static Date parseDate(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        try {
            return createDateFormat().parse(el.getAsString());
        } catch (final ParseException e) {
            log.error("Failed to parse date " + el.getAsString());
            return null;
        }
    }
    /**
     * @return date format.
     */
    private static DateFormat createDateFormat() {
        return DateTimeUtils.createDateFormat("yyyy-MM-dd' 'HH:mm:ss.SS",
                Language.English, TimeZone.getTimeZone("UTC"));
    }
    /**
     * @param autostartTemplateId
     * @return
     */
    protected AutoStartShipment findAutoStart(final Long autostartTemplateId) {
        return autoStartDao.findOne(autostartTemplateId);
    }
    /**
     * @param device device.
     * @param latitude latitude of location.
     * @param longitude longitude of location.
     * @param date autostart date.
     * @return
     */
    protected Shipment autoStartNewShipment(final Device device, final double latitude, final double longitude, final Date date) {
        return autoStartService.autoStartNewShipment(device, latitude, longitude, date);
    }
    /**
     * @param device device IMEI.
     * @param startDate start date.
     * @param endDate end date.
     * @return
     */
    protected List<ShortTrackerEvent> findTrackerEvents(final String device,
            final Date startDate, final Date endDate) {
        return trackerEventDao.findBy(device, startDate, endDate);
    }
    /**
     * @param id tracker event ID.
     * @param s shipment to assign.
     */
    protected void assignShipment(final Long id, final Shipment s) {
        trackerEventDao.assignShipment(id, s);
    }
}
