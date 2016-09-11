/**
 *
 */
package com.visfresh.reports.shipment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.services.EventsNullCoordinatesCorrector;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentReportBuilderTest extends ShipmentReportBuilder {
    private EventsNullCoordinatesCorrector corrector = new EventsNullCoordinatesCorrector();

    /**
     * Default constructor.
     */
    public ShipmentReportBuilderTest() {
        super();
    }

    @Test
    public void testLoadAlertImage() {
        assertNotNull(loadAlertImages());
    }
    @Test
    public void testCorrectReadingsLocation() {
        final List<ShortTrackerEvent> events = new LinkedList<>();
        events.add(createReading(10., 10.));
        events.add(createReading(null, null));
        events.add(createReading(null, null));
        events.add(createReading(12., 12.));
        events.add(createReading(null, null));
        events.add(createReading(14., 14.));

        corrector.correct(events);

        //not touched
        assertEquals(10., events.get(0).getLatitude(), 0.001);
        assertEquals(10., events.get(0).getLongitude(), 0.001);

        assertEquals(12., events.get(3).getLatitude(), 0.001);
        assertEquals(12., events.get(3).getLongitude(), 0.001);

        //moved to 12
        assertEquals(12., events.get(1).getLatitude(), 0.001);
        assertEquals(12., events.get(1).getLongitude(), 0.001);

        assertEquals(12., events.get(2).getLatitude(), 0.001);
        assertEquals(12., events.get(2).getLongitude(), 0.001);

        //moved to 14
        assertEquals(14., events.get(4).getLatitude(), 0.001);
        assertEquals(14., events.get(4).getLongitude(), 0.001);
    }
    @Test
    public void testCorrectReadingsLocationLastIsNull() {
        final List<ShortTrackerEvent> events = new LinkedList<>();
        events.add(createReading(10., 10.));
        events.add(createReading(11., 11.));
        events.add(createReading(null, null));

        corrector.correct(events);

        //not touched
        assertEquals(11., events.get(2).getLatitude(), 0.001);
        assertEquals(11., events.get(2).getLongitude(), 0.001);
    }

    /**
     * @param lat
     * @param lon
     * @return
     */
    private ShortTrackerEvent createReading(final Double lat, final Double lon) {
        final ShortTrackerEvent e = new ShortTrackerEvent();
        e.setLatitude(lat);
        e.setLongitude(lon);
        return e;
    }
}
