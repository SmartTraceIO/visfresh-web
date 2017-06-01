/**
 *
 */
package com.visfresh.autodetect;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.visfresh.csv.CsvEventParser;
import com.visfresh.json.LocationSerializer;
import com.visfresh.model.DeviceMessage;
import com.visfresh.model.Location;
import com.visfresh.model.LocationProfile;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DegugLocationEntering {
    private final List<LocationProfile> locations;
    private final List<DeviceMessage> messages;

    /**
     * Default constructor.
     * @throws IOException
     * @throws ParseException
     */
    public DegugLocationEntering() throws IOException, ParseException {
        super();
        locations = loadLocaions();
        this.messages = loadMessages();
    }
    /**
     * @return
     * @throws IOException
     */
    private List<LocationProfile> loadLocaions() throws IOException {
        final JsonArray json = getLocaionsJson();

        final LocationSerializer parser = new LocationSerializer();
        final List<LocationProfile> locs = new LinkedList<>();

        for (final JsonElement jsonElement : json) {
            locs.add(parser.parseLocationProfile(jsonElement.getAsJsonObject()));
        }

        return locs;
    }
    /**
     * @return
     * @throws ParseException
     * @throws IOException
     * @throws Exception
     */
    private List<DeviceMessage> loadMessages() throws IOException, ParseException {
        return new CsvEventParser().parseEvents(DegugLocationEntering.class.getResource("events.csv"));
    }

    /**
     * @return
     * @throws IOException
     */
    private JsonArray getLocaionsJson() throws IOException {
        try(Reader r = createLocationsStream()) {
            return new JsonParser().parse(r).getAsJsonArray();
        }
    }
    /**
     * @return
     */
    protected InputStreamReader createLocationsStream() {
        return new InputStreamReader(new BufferedInputStream(
                DegugLocationEntering.class.getResourceAsStream("locations.json")));
    }
    public void run() {
        final Map<LocationProfile, Double> distances = new HashMap<>();
        final Map<LocationProfile, List<DeviceMessage>> nearestEvents = new HashMap<>();

        //fill maps
        for (final LocationProfile loc : locations) {
            distances.put(loc, Double.MAX_VALUE);
            nearestEvents.put(loc, new LinkedList<>());
        }


        for (final DeviceMessage dm : messages) {
            final Location location = dm.getLocation();

            if (location.getLatitude() != null && location.getLongitude() != null) {
                for (final LocationProfile loc : locations) {
                    final double distance = getDistance(loc, location);

                    //save nearest event info
                    final double old = distances.get(loc);
                    if (distance < old) {
                        distances.put(loc, distance);
                    }

                    //if inside the location.
                    if (distance == 0) {
                        nearestEvents.get(loc).add(dm);
                        System.out.println(loc.getName());
                    }
                }
            }
        }

        //print result
        for (final LocationProfile loc : locations) {
            System.out.println("------------------====================-------------------");
            System.out.println(loc);
            System.out.println("Min distance: " + distances.get(loc) + " meters");
            System.out.println();

            final List<DeviceMessage> msgs = nearestEvents.get(loc);
            if (msgs.size() > 0) {
                System.out.println("Readings inside of locations:");

                for (final DeviceMessage msg : msgs) {
                    System.out.println(msg);
                }
            } else {
                System.out.println("Not readings inside of given location.");
            }
        }

        //check nearest location.
        if (messages.size() > 0) {
            System.out.println();
            System.out.println("Distances on given moment:");

            final Location lastLocation = messages.get(messages.size() - 1).getLocation();
            for (final LocationProfile locationProfile : locations) {
                final double d = getDistance(locationProfile, lastLocation);
                System.out.println(locationProfile + " in " + d + " meters");
            }
        }
    }
    /**
     * @param loc
     * @param location
     * @return
     */
    protected double getDistance(final LocationProfile loc, final Location location) {
        final Location end = loc.getLocation();
        double distance = LocationUtils.getDistanceMeters(
                location.getLatitude(), location.getLongitude(), end.getLatitude(), end.getLongitude());
        distance = Math.max(0., distance - loc.getRadius());
        return distance;
    }

    public static void main(final String[] args) throws IOException, ParseException {
        new DegugLocationEntering().run();
    }
}
