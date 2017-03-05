/**
 *
 */
package com.visfresh.autodetect;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

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
        for (final DeviceMessage dm : messages) {
            final Location location = dm.getLocation();

            if (location.getLatitude() != null && location.getLongitude() != null) {
                for (final LocationProfile loc : locations) {
                    final Location end = loc.getLocation();
                    double distance = LocationUtils.getDistanceMeters(
                            location.getLatitude(), location.getLongitude(), end.getLatitude(), end.getLongitude());
                    distance = Math.max(0., distance - loc.getRadius());
                    if (distance == 0) {
                        System.out.println(loc.getName());
//                        return;
                    }
                }
            }
        }
    }

    public static void main(final String[] args) throws IOException, ParseException {
        new DegugLocationEntering().run();
    }
}
