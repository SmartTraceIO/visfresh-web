/**
 *
 */
package com.visfresh.services;

import java.io.StringReader;
import java.util.List;

import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.reports.ShortTrackerEventsImporter;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EventsOptimizerTool extends EventsOptimizer {
    private EventsNullCoordinatesCorrector corrector = new EventsNullCoordinatesCorrector();

    /**
     * Default constructor.
     */
    public EventsOptimizerTool() {
        super();
    }

    public void runOptimizer() throws Exception {
        final String data = StringUtils.getContent(EventsOptimizerTool.class.getResource(
                "eventsOptimizerTool.csv"), "UTF-8");
        final List<ShortTrackerEvent> readings = new ShortTrackerEventsImporter(7l).importEvents(
                new StringReader(data));
        corrector.correct(readings);
        optimize(readings);
    }

    public static void main(final String[] args) throws Exception {
        new EventsOptimizerTool().runOptimizer();
    }
}
