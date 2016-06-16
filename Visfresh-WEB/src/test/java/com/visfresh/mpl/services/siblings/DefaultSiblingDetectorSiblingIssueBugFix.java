/**
 *
 */
package com.visfresh.mpl.services.siblings;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.StringUtils;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultSiblingDetectorSiblingIssueBugFix extends DefaultSiblingDetectorBugFix {
    final List<TrackerEvent> events= parseEvents();

    /**
     * @param env
     * @throws Exception
     */
    public DefaultSiblingDetectorSiblingIssueBugFix() throws Exception {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetectorBugFix#parseData()
     */
    @Override
    protected void parseData() throws Exception {
        final Map<Long, Shipment> shipments = parseShipments();
        this.activeShipments.putAll(shipments);

        //register lists for tracker events
        for (final Shipment s : shipments.values()) {
            trackerEvents.put(s.getId(), new LinkedList<TrackerEvent>());
        }
    }
    /**
     * @return
     */
    private List<TrackerEvent> parseEvents() throws Exception {
        final List<TrackerEvent> events = new LinkedList<>();
        final DateFormat df = new SimpleDateFormat("\"yyyy-MM-dd HH:mm:ss\"");

        final String[] rows = getContent("events.csv").split("\n");
        for (int i = 1; i < rows.length; i++) {
            final String row = rows[i].trim();
            if (!row.isEmpty()) {
//                id,time,createdon,latitude,longitude,shipment,device
//                2780,"2016-01-10 05:54:50","2016-01-10 05:54:50",-33.8871,151.1647,198,354188048733088
                final String[] data = row.split(",");

                final TrackerEvent e = new TrackerEvent();
                //id
                e.setId(Long.parseLong(data[0]));
                //time
                e.setTime(df.parse(data[1]));
                //created on
                e.setCreatedOn(df.parse(data[2]));
                //latitude
                final String latitude = data[3];
                if (!latitude.equals("NULL")) {
                    e.setLatitude(Double.parseDouble(latitude));
                }
                //latitude
                final String longitude = data[4];
                if (!longitude.equals("NULL")) {
                    e.setLongitude(Double.parseDouble(longitude));
                }
                //shipment
                e.setShipment(activeShipments.get(Long.parseLong(data[5])));
                //device
                final String imei = data[6];
                e.setDevice(getDevice(imei, true));

                events.add(e);
            }
        }
        return events;
    }
    /**
     * @return
     * @throws IOException
     */
    private Map<Long, Shipment> parseShipments() throws Exception {
        final Map<Long, Shipment> shipments = new HashMap<>();
        final DateFormat df = new SimpleDateFormat("\"yyyy-MM-dd HH:mm:ss\"");

        final String[] rows = getContent("shipments.csv").split("\n");
        for (int i = 1; i < rows.length; i++) {
            final String row = rows[i].trim();
            if (!row.isEmpty()) {
//              id,device,tripcount,shipmentdate,status
//              198,354188048733088,29,"2016-03-08 17:32:48",Default
                final String[] data = row.split(",");

                final Shipment s = new Shipment();
                //id
                s.setId(Long.parseLong(data[0]));
                //device
                s.setDevice(getDevice(data[1], true));
                //trip count
                s.setTripCount(Integer.parseInt(data[2]));
                //shipment date
                s.setShipmentDate(df.parse(data[3]));
                //status
                final String status = data[4];
                if (!status.isEmpty()) {
                    s.setStatus(ShipmentStatus.valueOf(status));
                }

                shipments.put(s.getId(), s);
            }
        }
        return shipments;
    }
    /**
     * @param resource
     * @return
     * @throws IOException
     */
    private String getContent(final String resource) throws IOException {
        return StringUtils.getContent(
                DefaultSiblingDetectorSiblingIssueBugFix.class.getResourceAsStream(resource),
                "UTF-8");
    }
    public void runTest() {
        while (!events.isEmpty()) {
            final TrackerEvent e = events.remove(0);
            //add event
            final List<TrackerEvent> es = this.trackerEvents.get(e.getShipment().getId());
            es.add(e);
        }

        //run sibling detection
        updateShipmentSiblingsForCompany(company);
    }
//    public void runTest() {
//        final Iterator<TrackerEvent> iter = events.iterator();
//        while (iter.hasNext()) {
//            final TrackerEvent e = iter.next();
//
//            final List<TrackerEvent> es = this.trackerEvents.get(e.getShipment().getId());
//            es.add(e);
//
//            if (e.getId().longValue() == 225420L) {
//                break;
//            }
//        }
//
//        final Shipment s = activeShipments.get(1496L);
//        final List<Shipment> shipments = new LinkedList<>(activeShipments.values());
//        shipments.remove(s);
//
//        final Map<Long, Set<Long>> siblingMap = new HashMap<>();
//        for (final Shipment shipment : activeShipments.values()) {
//            siblingMap.put(shipment.getId(), new HashSet<Long>());
//        }
//        findSiblings(s, shipments, siblingMap);
//        System.out.println(siblingMap.get(s.getId()));
//    }

    public static void main(final String[] args)throws Exception {
        final DefaultSiblingDetectorSiblingIssueBugFix bugFix = new DefaultSiblingDetectorSiblingIssueBugFix();
        bugFix.runTest();;
    }
}
