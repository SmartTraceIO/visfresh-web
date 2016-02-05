/**
 *
 */
package com.visfresh.rules;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.mpl.services.MainSystemMessageDispatcher;
import com.visfresh.services.DeviceCommandService;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ArrivalRule extends AbstractNotificationRule implements SystemMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(ArrivalRule.class);

    public static final String NAME = "Arrival";

    @Autowired
    protected ArrivalDao arrivalDao;
    @Autowired
    protected ShipmentDao shipmentDao;
    @Autowired
    protected TrackerEventDao trackerEventDao;
    @Autowired
    private DeviceCommandService commandService;
    @Autowired
    private MainSystemMessageDispatcher systemMessageDispatcher;

    /**
     * Default constructor.
     */
    public ArrivalRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext req) {
        final TrackerEvent event = req.getEvent();
        final boolean accept = !req.getState().isArrivalProcessed()
                && super.accept(req)
                && isNearEndLocation(event.getShipment(), event.getLatitude(), event.getLongitude());
        if (accept) {
            log.debug("Arrival Rule matches for shipment "
                    + event.getShipment().getShipmentDescription()
                    + " tracker event. Notification will generated");
        }
        return accept;
    }
    /**
     * @param shipment shipment.
     * @param latitude latitude of device location.
     * @param longitude longitude of device location.
     * @return
     */
    public static boolean isNearEndLocation(final Shipment shipment, final double latitude,
            final double longitude) {
        final LocationProfile endLocation = shipment.getShippedTo();
        if (shipment.getArrivalNotificationWithinKm() != null && endLocation != null) {
            final double distance = getNumberOfMetersForArrival(latitude, longitude, endLocation);
            return distance <= shipment.getArrivalNotificationWithinKm();
        }
        return false;
    }

    /**
     * @param latitude
     * @param longitude
     * @param endLocation
     * @return
     */
    protected static int getNumberOfMetersForArrival(final double latitude,
            final double longitude, final LocationProfile endLocation) {
        final Location end = endLocation.getLocation();
        double distance = LocationUtils.getDistanceMeters(latitude, longitude, end.getLatitude(), end.getLongitude());
        distance = Math.max(0., distance - endLocation.getRadius());
        return (int) Math.round(distance);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final RuleContext context) {
        final Arrival arrival = new Arrival();
        final TrackerEvent event = context.getEvent();
        context.setProcessed(this);
        context.getState().setArrivalProcessed(true);

        arrival.setDate(event.getTime());
        arrival.setDevice(event.getDevice());
        arrival.setNumberOfMettersOfArrival(getNumberOfMetersForArrival(
                event.getLatitude(), event.getLongitude(), event.getShipment().getShippedTo()));
        arrival.setShipment(event.getShipment());

        saveArrival(arrival);
        event.getShipment().setStatus(ShipmentStatus.Arrived);
        shipmentDao.save(event.getShipment());

        if (event.getShipment().getShutdownDeviceAfterMinutes() != null) {
            final long date = System.currentTimeMillis()
                    + event.getShipment().getShutdownDeviceAfterMinutes() * 60 * 1000l;
            sendShipmentShutdown(event.getShipment(), new Date(date));
        }

        final Calendar date = new GregorianCalendar();
        //notify subscribers
        final List<PersonSchedule> schedules = getAllPersonalSchedules(event.getShipment());
        for (final PersonSchedule s : schedules) {
            if (matchesTimeFrame(s, date)) {
                sendNotification(s, arrival);
            }
        }

        return false;
    }

    /**
     * @param a alert to save.
     */
    protected void saveArrival(final Arrival a) {
        arrivalDao.save(a);
    }

    /**
     * @param shipment shipment.
     * @return
     */
    @Override
    protected List<PersonSchedule> getAllPersonalSchedules(final Shipment shipment) {
        final List<PersonSchedule> all = new LinkedList<PersonSchedule>();

        final List<NotificationSchedule> schedules = shipment.getArrivalNotificationSchedules();
        for (final NotificationSchedule schedule : schedules) {
            final List<PersonSchedule> personalSchedules = schedule.getSchedules();
            all.addAll(personalSchedules);
        }

        return all;
    }

    @PostConstruct
    public void listenSystemMessage() {
        this.systemMessageDispatcher.setSystemMessageHandler(SystemMessageType.ShutdownShipment, this);
    }
    @PreDestroy
    public void destroy() {
        this.systemMessageDispatcher.setSystemMessageHandler(SystemMessageType.ShutdownShipment, null);
    }
    /**
     * @param shipment shipment.
     * @param date shutdown date.
     */
    private void sendShipmentShutdown(final Shipment shipment, final Date date) {
        final JsonObject json = new JsonObject();
        json.addProperty("shipment", shipment.getId());

        systemMessageDispatcher.sendSystemMessage(json.toString(), SystemMessageType.ShutdownShipment, date);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final JsonObject json = SerializerUtils.parseJson(msg.getMessageInfo()).getAsJsonObject();
        final Shipment s = shipmentDao.findOne(json.get("shipment").getAsLong());
        final String imei = s.getDevice().getImei();

        if (shipmentDao.findNextShipmentFor(s) == null) {
            log.debug("Shutdown device " + imei + " for shipment " + s.getId());

            //send device shutdown command
            commandService.shutdownDevice(s.getDevice(), new Date());
            s.setDeviceShutdownTime(new Date());
        } else {
            log.warn("Shutting down shipment " + s.getId()
                    + " is not latest shipment for given device " + imei
                    + ". Device will not shutting down");
            //calculate start shipment date
            final TrackerEvent lastEvent = trackerEventDao.getLastEvent(s);
            if (lastEvent == null) {
                s.setDeviceShutdownTime(new Date());
            } else {
                s.setDeviceShutdownTime(lastEvent.getTime());
            }
        }

        shipmentDao.save(s);
    }

    @Override
    public String getName() {
        return NAME;
    }
    /**
     * @return
     */
    protected static String createProcessedKey() {
        return NAME + "_processed";
    }
}
