/**
 *
 */
package com.visfresh.rules;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractInterimStopRule implements TrackerEventRule {
    @Autowired
    protected InterimStopDao interimStopDao;
    @Autowired
    private AbstractRuleEngine engine;

    protected static class InterimStopInfo {
        private long startTime;
        private int numReadings = 0;
        private Long id; //id of reference to InterimStop in DB
        private double latitude;
        private double longitude;

        /**
         * @return the sartTime
         */
        public long getStartTime() {
            return startTime;
        }
        /**
         * @param sartTime the sartTime to set
         */
        public void setStartTime(final long sartTime) {
            this.startTime = sartTime;
        }
        /**
         * @return the numReadings
         */
        public int getNumReadings() {
            return numReadings;
        }
        /**
         * @param numReadings the numReadings to set
         */
        public void setNumReadings(final int numReadings) {
            this.numReadings = numReadings;
        }
        /**
         * @return the id
         */
        public Long getId() {
            return id;
        }
        /**
         * @param id the id to set
         */
        public void setId(final Long id) {
            this.id = id;
        }
        /**
         * @return the latitude
         */
        public double getLatitude() {
            return latitude;
        }
        /**
         * @param latitude the latitude to set
         */
        public void setLatitude(final double latitude) {
            this.latitude = latitude;
        }
        /**
         * @return the longitude
         */
        public double getLongitude() {
            return longitude;
        }
        /**
         * @param longitude the longitude to set
         */
        public void setLongitude(final double longitude) {
            this.longitude = longitude;
        }
    }

    /**
     * Default constructor.
     */
    public AbstractInterimStopRule() {
        super();
    }

    /**
     * @param shipment
     * @return
     */
    protected boolean hasInterimStops(final Shipment shipment) {
        return (interimStopDao.getByShipment(shipment).size() > 0);
    }
    protected boolean isNearInterimStop(final Shipment shipment, final ShipmentSession state,
            final Double latitude, final Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

        final List<LocationProfile> locs = getInterimLocations(shipment);
        if (locs != null) {
            final LocationProfile p = getBestLocation(locs, latitude, longitude);
            return p != null;
        }
        return false;
    }

    /**
     * @param locs location list.
     * @param latitude latitude.
     * @param longitude longitude.
     * @return mathes location.
     */
    protected static LocationProfile getBestLocation(final List<LocationProfile> locs,
            final double latitude, final double longitude) {
        int maxDistance = Integer.MAX_VALUE;
        LocationProfile best = null;

        for (final LocationProfile l : locs) {
            final int distance = (int) Math.round(LocationUtils.getDistanceMeters(
                    latitude, longitude, l.getLocation().getLatitude(), l.getLocation().getLongitude()));

            //if inside of location radius.
            if (Math.max(0, distance - l.getRadius()) == 0 && distance < maxDistance) {
                maxDistance = distance;
                best = l;
            }
        }
        return best;
    }

    /**
     * @param shipment
     * @return
     */
    protected List<LocationProfile> getInterimLocations(final Shipment shipment) {
        return engine.getInterimLocations(shipment);
    }

    /**
     * @param state interim stop state.
     * @param stop the interim stop info.
     */
    protected void setInterimStopState(final ShipmentSession state, final InterimStopInfo stop) {
        state.setShipmentProperty(createInterimStopKey(), toJson(stop).toString());
    }
    /**
     * @param shipment
     * @param stop
     * @return
     */
    protected Long save(final Shipment shipment, final InterimStop stop) {
        interimStopDao.save(shipment, stop);
        return stop.getId();
    }
    /**
     * @param stop interim stop info.
     * @param minutes stop time in minutes.
     */
    protected void updateStopTime(final InterimStopInfo stop, final int minutes) {
        interimStopDao.updateTime(stop.getId(), minutes);
    }

    /**
     * @param state
     * @return
     */
    protected static boolean isInInterimStop(final ShipmentSession state) {
        return getInterimStop(state) != null;
    }
    /**
     * @param state
     * @return
     */
    protected static InterimStopInfo getInterimStop(final ShipmentSession state) {
        final String key = createInterimStopKey();
        final String info = state.getShipmentProperty(key);

        if (info != null) {
            return parseInterimStopInfo(SerializerUtils.parseJson(info).getAsJsonObject());
        }
        return null;
    }
    /**
     * @param json JSON object.
     * @return interim stop info.
     */
    protected static InterimStopInfo parseInterimStopInfo(final JsonObject json) {
        final InterimStopInfo info = new InterimStopInfo();
        final JsonElement id = json.get("id");
        if (id != null && !id.isJsonNull()) {
            info.setId(id.getAsLong());
        }
        info.setLatitude(json.get("latitude").getAsDouble());
        info.setLongitude(json.get("longitude").getAsDouble());
        info.setNumReadings(json.get("numReadings").getAsInt());
        info.setStartTime(json.get("startTime").getAsLong());
        return info;
    }
    protected static JsonObject toJson(final InterimStopInfo info) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", info.getId());
        json.addProperty("latitude", info.getLatitude());
        json.addProperty("longitude", info.getLongitude());
        json.addProperty("numReadings", info.getNumReadings());
        json.addProperty("startTime", info.getStartTime());
        return json;
    }
    /**
     * @return
     */
    private static String createInterimStopKey() {
        return "InterimStop-stop";
    }
}
