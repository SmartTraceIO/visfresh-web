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
import java.util.Iterator;
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
import com.visfresh.utils.ToStringer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CheckAllWrongAutodetected {
    private final NamedParameterJdbcTemplate jdbc;
    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Default constructor
     */
    public CheckAllWrongAutodetected(final NamedParameterJdbcTemplate jdbc) {
        super();
        this.jdbc = jdbc;
    }

    public void run() {
        final List<ShipmentResult> results = getShipmentStatistics();
        printResult(results);
    }

    /**
     * @param results
     */
    private void printResult(final List<ShipmentResult> origin) {
        final List<ShipmentResult> issuesShipments = new LinkedList<>(origin);
        final List<ShipmentResult> correctShipments = cutCorrectShipments(issuesShipments);

        //print correct shipment
        if (correctShipments.size() > 0) {
            getLog().println("-- Correct shipments (" + correctShipments.size() + ").--");
            for (final ShipmentResult s : correctShipments) {
                System.out.println(s.getShipment() + ", number of locations: "
                        + s.getLocationResults().size() + ", min distance: " + s.getMinDistance());
            }
        }

        //print issues shipments
        if (issuesShipments.size() > 0) {
            getLog().println();
            getLog().println("-- Issues shipments (" + issuesShipments.size() + ").--");

            getLog().println("List of issues shipments: [" + String.join("],[",
                getToStringList(issuesShipments, o -> o.toString())) + "]");
            getLog().println("Exspanded shipment info:");

            for (final ShipmentResult s : issuesShipments) {
                getLog().println();
                getLog().println("-- Shipment: " + s.getShipment()
                    + ", device: " + s.getShipment().getDevice()
                    + ", min distance: " + s.getMinDistance()
                    + ", total number of readings: " + s.getMessages().size());
                getLog().println("Location list: ["
                        + String.join("],[", getToStringList(s.getLocationResults(), l -> l.getLocation().getName()))
                        + "]");

                //print expanded location info
                for (final LocationResult lr : s.getLocationResults()) {
                    getLog().println(lr.getLocation() + ", number of visits: "
                            + lr.getMessagesInsideLocation().size() + ", min distance: " + lr.getMinDinstance());
                    if (lr.getMessagesInsideLocation().size() > 0) {
                        getLog().println("Number of events inside locations " + lr.getMessagesInsideLocation().size()
                                + ", events in location: ");
                        for (final DeviceMessage dm : lr.getMessagesInsideLocation()) {
                            System.out.println(df.format(dm.getTime()) + " (" + dm.getType() + ")"
                                    + ", " + dm.getLocation());
                        }
                    }
                }

                getLog().println();
                getLog().println("Research result:");
            }
        }
    }

    /**
     * @param issuesShipments
     * @return
     */
    private List<ShipmentResult> cutCorrectShipments(final List<ShipmentResult> issuesShipments) {
        final List<ShipmentResult> correct = new LinkedList<>();
        final Iterator<ShipmentResult> iter = issuesShipments.iterator();
        while (iter.hasNext()) {
            final ShipmentResult res = iter.next();
            if (res.getMinDistance() > 100) {
                iter.remove();
                correct.add(res);
            }
        }

        return correct;
    }

    /**
     * @param issuesShipments
     * @return
     */
    private <M> List<String> getToStringList(final List<M> list, final ToStringer<M> toStringer) {
        final List<String> toStrings = new LinkedList<>();
        list.forEach(s -> toStrings.add(toStringer.toString(s)));
        return toStrings;
    }

    /**
     * The logger.
     */
    private PrintStream getLog() {
        return System.out;
    }

    /**
     * @return
     */
    protected List<ShipmentResult> getShipmentStatistics() {
        final List<ShipmentResult> results = new LinkedList<>();

        final Set<Shipment> shipments = getAllWrongShipments();
        for (final Shipment shipment : shipments) {
            final ShipmentResult result = new ShipmentResult();
            result.setShipment(shipment);
            results.add(result);

            final List<LocationProfile> locations = getAlternativeLocations(shipment.getId());
            final List<LocationResult> lresults = new LinkedList<>();
            result.setLocationResults(lresults);

            for (final LocationProfile loc : locations) {
                final LocationResult lr = new LocationResult();
                lr.setLocation(loc);
                lresults.add(lr);
            }

            final List<DeviceMessage> messages = getMessages(shipment.getId());
            result.setMessages(messages);
            result.setMinDistance(doCheck(messages, lresults));
        }
        return results;
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
        s.setDevice((String) row.get("device"));
        s.setTripCount(((Number) row.get("tripcount")).intValue());
        return s;
    }

    /**
     * @param messages
     * @param locations
     * return global for all location minimal distance.
     */
    private double doCheck(final List<DeviceMessage> messages, final List<LocationResult> locations) {
        double globalMinDistance = Double.MAX_VALUE;

        final Map<LocationProfile, Integer> visitedLocations = new HashMap<>();

        for (final LocationResult result : locations) {
            final LocationProfile location = result.getLocation();

            double minDistance = Double.MAX_VALUE;
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
                        result.getMessagesInsideLocation().add(dm);
                        if (!alreadyStartedPrintMessages) {
                            alreadyStartedPrintMessages = true;
                        }

                        Integer numVisits = visitedLocations.get(location);
                        if (numVisits == null) {
                            numVisits = 0;
                        }

                        visitedLocations.put(location, numVisits + 1);
                    }
                }
            }

            globalMinDistance = Math.min(globalMinDistance, minDistance);
        }

        return globalMinDistance;
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
