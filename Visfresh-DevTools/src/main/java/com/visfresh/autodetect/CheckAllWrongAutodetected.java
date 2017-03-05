/**
 *
 */
package com.visfresh.autodetect;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.jdbc.JdbcConfig;
import com.visfresh.jdbc.JdbcTemplateHolder;
import com.visfresh.model.DeviceMessage;
import com.visfresh.model.Location;
import com.visfresh.model.LocationProfile;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CheckAllWrongAutodetected {
    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor
     */
    public CheckAllWrongAutodetected(final NamedParameterJdbcTemplate jdbc) {
        super();
        this.jdbc = jdbc;
    }

    public void run() {
        final Set<Shipment> shipments = getAllWrongShipments();
        for (final Shipment shipment : shipments) {
            final List<LocationProfile> locations = getAlternativeLocations(shipment.getId());
            getOutput().println("Shipment: " + shipment);

            if (locations.isEmpty()) {
                getOutput().println("Not found alternative locations for shipment");
            } else {
                //print locations
                getOutput().println("Location list (size: " + locations.size() + "):");
                for (final LocationProfile loc : locations) {
                    getOutput().println(loc);
                }
                final List<DeviceMessage> messages = getMessages(shipment.getId());
                doCheck(messages, locations);
            }
            getOutput().println("------==================---------");
        }
    }

    /**
     * @param id shipment ID.
     * @return list of device messages.
     */
    private List<DeviceMessage> getMessages(final Long id) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("shipment", id);

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from trackerevents where shipment = :shipment", params);
        final List<DeviceMessage> messages = new LinkedList<>();
        for (final Map<String,Object> row : rows) {
            messages.add(parseDeviceMessage(row));
        }
        return messages;
    }

    /**
     * @param row data row from DB.
     * @return device message.
     */
    private DeviceMessage parseDeviceMessage(final Map<String, Object> row) {
        final DeviceMessage msg = new DeviceMessage();

        msg.setBattery(((Number) row.get("battery")).intValue());
        msg.setTemperature(((Number) row.get("temperature")).doubleValue());
        msg.setTime((Date) row.get("time"));
        msg.setType((String) row.get("type"));
        msg.setImei((String) row.get("device"));

        //location
        final Location loc = new Location();
        msg.setLocation(loc);
        final Number lat = (Number) row.get("latitude");
        if (lat != null) {
            loc.setLatitude(lat.doubleValue());
        }
        final Number lon = (Number) row.get("longitude");
        if (lon != null) {
            loc.setLongitude(lon.doubleValue());
        }

        return msg;
    }

    /**
     * @param id shipment ID.
     * @return list of location profiles.
     */
    private List<LocationProfile> getAlternativeLocations(final Long id) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("shipment", id);

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from locationprofiles where id in (select location from alternativelocations"
                + " where shipment = :shipment and loctype = 'to' group by location)", params);
        final List<LocationProfile> locations = new LinkedList<>();
        for (final Map<String,Object> row : rows) {
            locations.add(parseLocation(row));
        }
        return locations;
    }

    /**
     * @param row data row from DB.
     * @return location profile instance.
     */
    private LocationProfile parseLocation(final Map<String, Object> row) {
        final LocationProfile loc = new LocationProfile();
        loc.setId(((Number) row.get("id")).longValue());

        loc.setAddress((String) row.get("address"));
        loc.setCompanyName((String) row.get("companydetails"));
        loc.setInterim((Boolean) row.get("interim"));
        loc.setName((String) row.get("name"));
        loc.setNotes((String) row.get("notes"));
        loc.setRadius(((Number) row.get("radius")).intValue());
        loc.setStart((Boolean) row.get("start"));
        loc.setStop((Boolean) row.get("stop"));
        loc.getLocation().setLatitude(((Number) row.get("latitude")).doubleValue());
        loc.getLocation().setLongitude(((Number) row.get("longitude")).doubleValue());

        return loc;
    }

    /**
     * @return
     */
    private Set<Shipment> getAllWrongShipments() {
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select id, device, tripcount from shipments where company = 3 and status = 'Ended'"
                + " and startdate > '2017-02-01'"
                + " and lasteventdate - startdate > 99999", new HashMap<>());
        final Set<Shipment> shipments = new HashSet<>();
        for (final Map<String,Object> row : rows) {
            shipments.add(createShipment(row));
        }
        return shipments;
    }

    /**
     * @param row
     * @return
     */
    private Shipment createShipment(final Map<String, Object> row) {
        final Shipment s = new Shipment();
        s.setId(((Number) row.get("id")).longValue());
        s.setSn(Shipment.getSerialNumber((String) row.get("device")));
        s.setTripCount(((Number) row.get("tripcount")).intValue());
        return s;
    }

    /**
     * @param messages
     * @param locations
     */
    private void doCheck(final List<DeviceMessage> messages, final List<LocationProfile> locations) {
        double minDistance = Double.MAX_VALUE;

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        final Map<LocationProfile, Integer> visitedLocations = new HashMap<>();

        for (final LocationProfile location : locations) {
            getOutput().println();
            getOutput().println("Checking locaiton: " + location);
            for (final DeviceMessage dm : messages) {
                final Location l = dm.getLocation();

                boolean alreadyStartedPrintMessages = false;
                if (l != null && l.getLatitude() != null && l.getLongitude() != null) {
                    final Location end = location.getLocation();
                    double distance = LocationUtils.getDistanceMeters(
                            l.getLatitude(), l.getLongitude(), end.getLatitude(), end.getLongitude());
                    distance = Math.max(0., distance - location.getRadius());

                    minDistance = Math.min(distance, minDistance);
                    if (distance < 0.1) {
                        if (!alreadyStartedPrintMessages) {
                            System.out.println("Messages inside of end location:");
                            alreadyStartedPrintMessages = true;
                        }
                        getOutput().println(dm.getType() + ", " + df.format(dm.getTime()) + " ("
                                + l + "), distance: " + distance);

                        Integer numVisits = visitedLocations.get(location);
                        if (numVisits == null) {
                            numVisits = 0;
                        }

                        visitedLocations.put(location, numVisits + 1);
                    }
                }
//                else {
//                    getOutput().println();
//                    getOutput().println("Null locatiion: " + dm.getType() + ", " + df.format(dm.getTime()));
//                }
            }

            getOutput().println("Min distance for location "
                    + location.getName() + ": " + minDistance);
        }

        getOutput().println();
        final Set<LocationProfile> visLocsSet = visitedLocations.keySet();

        getOutput().println("Number of visited locations: " + visLocsSet.size());
        for (final LocationProfile loc : visLocsSet) {
            getOutput().println(loc + ". Number of visits: " + visitedLocations.get(loc));
        }
    }

    /**
     * @return
     */
    protected PrintStream getOutput() {
        return System.out;
    }

    public static void main(final String[] args) {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

        try {
            //initialize context
            ctx.scan(JdbcConfig.class.getPackage().getName());
            ctx.refresh();

            final CheckAllWrongAutodetected tool = new CheckAllWrongAutodetected(
                    ctx.getBean(JdbcTemplateHolder.class).getJdbc());
            tool.run();
        } finally {
            ctx.close();
        }
    }
}
