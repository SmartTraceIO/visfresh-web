/**
 *
 */
package com.visfresh.impl.services;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.impl.siblingdetect.CalculationDirection;
import com.visfresh.impl.siblingdetect.SiblingDetector;
import com.visfresh.impl.siblingdetect.StatefullSiblingDetector.State;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.tools.SingleShipmentUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class DetectingSiblingsTool {
    /**
     * Default constructor.
     */
    private DetectingSiblingsTool() {
        super();
    }

    /**
     * @param args program arguments.
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public static void main(final String[] args) throws JsonParseException, JsonMappingException, IOException {
        final List<TrackerEventDto> e2 = loadEvents("s1.json");
        final List<TrackerEventDto> e1 = loadEvents("s2.json");

        final boolean isSiblings = new SiblingDetector(CalculationDirection.LeftToRight).detectSiblingsState(
                e1.iterator(), e2.iterator()) == State.Siblings;
        System.out.println("Is siblings: " + isSiblings);
    }

    /**
     * @param resource
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private static List<TrackerEventDto> loadEvents(final String resource) throws JsonParseException, JsonMappingException, IOException {
        int totalEvents = 0;
        int eventsWithNullCoordinates = 0;

        final SingleShipmentData data = SingleShipmentUtils.parseSingleShipmentDataJson(
                DetectingSiblingsTool.class.getResource(resource));
        final List<TrackerEvent> events = SingleShipmentUtils.getTrackerEvents(data);
        final List<TrackerEventDto> list = new LinkedList<>();
        for (final TrackerEvent e : events) {
            final TrackerEventDto dto = new TrackerEventDto();
            dto.setBattery(e.getBattery());
            dto.setCreatedOn(e.getCreatedOn());
            dto.setDeviceImei(e.getDevice().getName());
            dto.setId(e.getId());
            dto.setLatitude(e.getLatitude());
            dto.setLongitude(e.getLongitude());
            dto.setShipmentId(e.getShipment().getId());
            dto.setTemperature(e.getTemperature());
            dto.setHumidity(e.getHumidity());
            dto.setTime(e.getTime());
            dto.setType(e.getType());

            totalEvents++;
            if (dto.getLatitude() == null || dto.getLongitude() == null) {
                eventsWithNullCoordinates++;
            } else {
                list.add(dto);
            }
        }

        System.out.println("Readen for resource " + resource + ": " + totalEvents
                + ", has not coordinates: " + eventsWithNullCoordinates);
        return list;
    }
}
