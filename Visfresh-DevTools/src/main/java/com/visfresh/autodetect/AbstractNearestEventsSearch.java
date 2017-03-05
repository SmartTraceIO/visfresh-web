/**
 *
 */
package com.visfresh.autodetect;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.json.LocationSerializer;
import com.visfresh.model.DeviceMessage;
import com.visfresh.model.Location;
import com.visfresh.model.LocationProfile;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractNearestEventsSearch {
    private final double maxDistance;

    /**
     * Default constructor.
     * @throws IOException
     * @throws ParseException
     */
    public AbstractNearestEventsSearch(final double distance) throws IOException, ParseException {
        super();
        this.maxDistance = distance;
    }
    /**
     * @return
     * @throws IOException
     */
    private LocationProfile loadLocaion() {
        JsonObject json;
        try {
            json = getLocaionsJson();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return new LocationSerializer().parseLocationProfile(json);
    }
    /**
     * @return
     * @throws ParseException
     * @throws IOException
     * @throws Exception
     */
    protected abstract List<DeviceMessage> loadMessages();

    /**
     * @return
     * @throws IOException
     */
    private JsonObject getLocaionsJson() throws IOException {
        try(Reader r = createLocationsStream()) {
            return new JsonParser().parse(r).getAsJsonObject();
        }
    }
    /**
     * @return
     */
    protected InputStreamReader createLocationsStream() {
        return new InputStreamReader(new BufferedInputStream(
                AbstractNearestEventsSearch.class.getResourceAsStream("location.json")));
    }
    public void run() {
        final LocationProfile location = loadLocaion();
        final List<DeviceMessage> messages = loadMessages();

        double minDistance = Double.MAX_VALUE;

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (final DeviceMessage dm : messages) {
            final Location l = dm.getLocation();

            if (l != null && l.getLatitude() != null && l.getLongitude() != null) {
                final Location end = location.getLocation();
                double distance = LocationUtils.getDistanceMeters(
                        l.getLatitude(), l.getLongitude(), end.getLatitude(), end.getLongitude());
                distance = Math.max(0., distance - location.getRadius());

                minDistance = Math.min(distance, minDistance);
                if (distance <= maxDistance) {
                    System.out.println(dm.getType() + ", " + df.format(dm.getTime()) + " ("
                            + l + "), distance: " + distance);
                }
            } else {
                System.out.println("Null locatiion: " + dm.getType() + ", " + df.format(dm.getTime()));
            }
        }

        System.out.println("Min distance: " + minDistance);
    }
}
