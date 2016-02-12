/**
 *
 */
package com.visfresh.mpl.services.siblings;

import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.utils.CollectionUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultSiblingDetectorBugFix extends DefaultSiblingDetector {
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Map<Long, Shipment> activeShipments = new HashMap<>();
    private final Map<Long, Shipment> savedShipments = new HashMap<>();
    private final Map<Long, List<TrackerEvent>> trackerEvents = new HashMap<>();

    private Company company;
    /**
     * Default constructor.
     */
    public DefaultSiblingDetectorBugFix() throws Exception {
        super(0);
        company = new Company();
        company.setId(1l);
        company.setName("JUnit Company");

        parseData();
    }

    /**
     *
     */
    private void parseData() throws Exception {
        final Reader r = new InputStreamReader(
                DefaultSiblingDetectorBugFix.class.getResourceAsStream("data.json"), "UTF-8");
        final JsonArray events = new JsonParser().parse(r).getAsJsonArray();

        for (final JsonElement e : events) {
            final JsonObject json = e.getAsJsonObject();
            //"shipment" : 366
            //create shipment.
            final Long shipmentId = json.get("shipment").getAsLong();
            Shipment s = this.activeShipments.get(shipmentId);
            if (s == null) {
                s = creaeShipment(shipmentId);
                activeShipments.put(shipmentId, s);
                trackerEvents.put(shipmentId, new LinkedList<TrackerEvent>());
            }

            final List<TrackerEvent> list = trackerEvents.get(shipmentId);
            final TrackerEvent event = new TrackerEvent();
            list.add(event);

            //  "id" : 17409,
            event.setId(json.get("id").getAsLong());
            //  "battery" : 4058,
            event.setBattery(json.get("battery").getAsInt());
            //  "temperature" : 23.25,
            event.setTemperature(json.get("temperature").getAsDouble());
            //  "time" : "2016-02-10 23:01:37",
            event.setTime(dateFormat.parse(json.get("time").getAsString()));
            //  "type" : "INIT",
            event.setType(TrackerEventType.valueOf(json.get("type").getAsString()));
            //  "latitude" : -33.855881,
            event.setLatitude(json.get("latitude").getAsDouble());
            //  "longitude" : 151.049713,
            event.setLongitude(json.get("longitude").getAsDouble());
            //  "imei" : "354430070002333",

            final String imei = json.get("imei").getAsString();
            if (s.getDevice() == null) {
                final Device d = new Device();
                d.setImei(imei);
                d.setCompany(company);
                d.setName("BugFix device");

                s.setDevice(d);
            }

            event.setDevice(s.getDevice());
            event.setShipment(s);
        }
    }

    /**
     * @param id shipment ID.
     * @return
     */
    private Shipment creaeShipment(final long id) {
        final Shipment s = new Shipment();
        s.setId(id);
        s.setCompany(company);
        s.setShipmentDescription("Test_" + id);
        return s;
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#findActiveShipments(com.visfresh.entities.Company)
     */
    @Override
    protected List<Shipment> findActiveShipments(final Company company) {
        final List<Shipment> list = new LinkedList<>(activeShipments.values());
        CollectionUtils.sortById(list);
        return list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    protected void saveShipment(final Shipment shipment) {
        savedShipments.put(shipment.getId(), shipment);
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#getTrackeEvents(com.visfresh.entities.Shipment)
     */
    @Override
    protected TrackerEvent[] getTrackeEvents(final Shipment shipment) {
        final List<TrackerEvent> events = trackerEvents.get(shipment.getId());
        if (events == null) {
            return new TrackerEvent[0];
        }
        return events.toArray(new TrackerEvent[events.size()]);
    }

    public static void main(final String[] args) throws Exception {
        final DefaultSiblingDetectorBugFix bugFix = new DefaultSiblingDetectorBugFix();
        bugFix.updateShipmentSiblingsForCompany(bugFix.company);
    }
}
