/**
 *
 */
package com.visfresh.mpl.services;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.Language;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.ArrivalService;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ArrivalServiceImpl implements ArrivalService {
    private static final Logger log = LoggerFactory.getLogger(ArrivalServiceImpl.class);
    @Autowired
    private LocationProfileDao locationDao;

    protected class ArrivalData {
        private Date arrivalDate;
        private boolean hasBrt;
        private Date stopTime;

        public ArrivalData() {
            super();
        }

        public Date getArrivalDate() {
            return arrivalDate;
        }
        public void setArrivalDate(final Date arrivalDate) {
            this.arrivalDate = arrivalDate;
        }
        public boolean hasBrt() {
            return hasBrt;
        }
        public void setHasBrt(final boolean hasBrt) {
            this.hasBrt = hasBrt;
        }
        public Date getStopTime() {
            return stopTime;
        }
        public void setStopTime(final Date stopTime) {
            this.stopTime = stopTime;
        }
    }

    /**
     * Default constructor.
     */
    public ArrivalServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalService#isNearLocation(com.visfresh.entities.LocationProfile, com.visfresh.entities.Location)
     */
    @Override
    public boolean isNearLocation(final LocationProfile loc, final Location l) {
        if (loc != null) {
            final double distance = getNumberOfMetersForArrival(
                    l.getLatitude(), l.getLongitude(), loc);
            return distance < 1.0;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalService#handleNearLocation(com.visfresh.entities.LocationProfile, com.visfresh.entities.Location, com.visfresh.rules.state.ShipmentSession)
     */
    @Override
    public boolean handleNearLocation(
            final LocationProfile loc,
            final TrackerEvent event,
            final ShipmentSession session) {
        final String key = createKey(session, loc);

        ArrivalData data;
        try {
            data = getArrivalData(key, session);
        } catch (final ParseException e) {
            log.error("Failed to parse arrival data", e);
            return false;
        }

        if (data == null) {
            data = new ArrivalData();
            data.setArrivalDate(event.getTime());
        }

        if (event.getType() == TrackerEventType.BRT) {
            data.setHasBrt(true);
        } else if (event.getType() == TrackerEventType.STP) {
            data.setStopTime(event.getTime());
        }

        putArrivalData(key, session, data);
        final long presenceTime = event.getTime().getTime() - data.getArrivalDate().getTime();
        final long stopTime = data.getStopTime() == null
                ? 0 : event.getTime().getTime() - data.getStopTime().getTime();

//      TimeStoppedWithinRadiusOfPossibleDestination = 10min or more
        if (stopTime >= 10 * 60 * 1000l) {
            return true;
        }
//      TimeWithinRadiusOfPossibleDestination = 10min or more (with BRT reading in that time period)
        if (data.hasBrt() && presenceTime >= 10 * 60 * 1000l) {
            return true;
        }

//      TimeWithinRadiusOfPossibleDestination = 20min or more
        if (presenceTime >= 20 * 60 * 1000l) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalService#clearLocationHistory(com.visfresh.entities.LocationProfile, com.visfresh.rules.state.ShipmentSession)
     */
    @Override
    public void clearLocationHistory(final LocationProfile loc,
            final ShipmentSession session) {
        final String key = createKey(session, loc);
        session.setShipmentProperty(key, null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalService#clearLocationHistory(com.visfresh.entities.LocationProfile, com.visfresh.rules.state.ShipmentSession)
     */
    @Override
    public List<LocationProfile> getEnteredLocations(final ShipmentSession session) {
        final String prefix = getKeyPrefix(session);

        final LinkedList<Long> locs = new LinkedList<>();
        for (final String key : session.getShipmentKeys()) {
            if (key.startsWith(prefix)) {
                locs.add(Long.parseLong(key.substring(prefix.length())));
            }
        }

        return getLocations(locs);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalService#hasEnteredLocations(com.visfresh.rules.state.ShipmentSession)
     */
    @Override
    public boolean hasEnteredLocations(final ShipmentSession session) {
        final String prefix = getKeyPrefix(session);

        for (final String key : session.getShipmentKeys()) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param locs
     * @return
     */
    protected List<LocationProfile> getLocations(final Collection<Long> locs) {
        return locationDao.findAll(locs);
    }
    /**
     * @param key session key.
     * @param session shipment session.
     * @param data arrival data to store.
     */
    private void putArrivalData(final String key, final ShipmentSession session,
            final ArrivalData data) {
        session.setShipmentProperty(key, data == null ? null : toJson(data));
    }
    /**
     * @param key session key.
     * @param session shipment session.
     * @return arrival data.
     * @throws ParseException
     */
    private ArrivalData getArrivalData(final String key,
            final ShipmentSession session) throws ParseException {
        final String str = session.getShipmentProperty(key);
        return str == null ? null : parseArrivalData(str);
    }

    /**
     * @param session
     * @param loc
     * @return
     */
    private String createKey(final ShipmentSession session, final LocationProfile loc) {
        return getKeyPrefix(session) + loc.getId();
    }
    /**
     * @param session
     * @return
     */
    private String getKeyPrefix(final ShipmentSession session) {
        return "ArrivalService-ship-" + session.getShipmentId() + "-loc-";
    }
    /**
     * @param latitude
     * @param longitude
     * @param endLocation
     * @return
     */
    private static int getNumberOfMetersForArrival(final double latitude,
            final double longitude, final LocationProfile endLocation) {
        final Location end = endLocation.getLocation();
        double distance = LocationUtils.getDistanceMeters(latitude, longitude, end.getLatitude(), end.getLongitude());
        distance = Math.max(0., distance - endLocation.getRadius());
        return (int) Math.round(distance);
    }

    /**
     * @param str
     * @return
     * @throws ParseException
     */
    protected ArrivalData parseArrivalData(final String str) throws ParseException {
        if (str == null) {
            return null;
        }

        final JsonObject json = SerializerUtils.parseJson(str).getAsJsonObject();

        final ArrivalData data = new ArrivalData();
        final DateFormat df = createDateFormat();
        data.setArrivalDate(df.parse(
                json.get("arrivalDate").getAsString()));
        data.setHasBrt(json.get("hasBrt").getAsBoolean());

        final JsonElement stopTime = json.get("stopTime");
        if (stopTime != null && !stopTime.isJsonNull()) {
            data.setStopTime(df.parse(
                    stopTime.getAsString()));

        }

        return data;
    }
    /**
     * @param data arrival data.
     * @return arrival data as JSON string.
     */
    protected String toJson(final ArrivalData data) {
        final JsonObject obj = new JsonObject();

        obj.addProperty("arrivalDate",
                createDateFormat().format(data.getArrivalDate()));
        obj.addProperty("hasBrt", data.hasBrt());
        final Date stopTime = data.getStopTime();
        obj.addProperty("stopTime",
                stopTime == null ? null : createDateFormat().format(stopTime));

        return obj.toString();
    }
    /**
     * @return date format.
     */
    private DateFormat createDateFormat() {
        return DateTimeUtils.createDateFormat("yyyy-MM-dd' 'HH:mm:ss.SS",
                Language.English, TimeZone.getTimeZone("UTC"));
    }
}
