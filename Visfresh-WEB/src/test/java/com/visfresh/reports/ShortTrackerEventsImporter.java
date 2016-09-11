/**
 *
 */
package com.visfresh.reports;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.reports.shipment.ReadingsHandler;
import com.visfresh.reports.shipment.ReadingsParser;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShortTrackerEventsImporter {
    private long shipmentId;
    private String device;

    /**
     * @param shipmentId shipment ID.
     * @param device device.
     *
     */
    public ShortTrackerEventsImporter(final long shipmentId, final String device) {
        this.shipmentId = shipmentId;
        this.device = device;
    }

    /**
     * @param in
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public List<ShortTrackerEvent> importEvents(final Reader r) throws IOException, ParseException {
        final List<ShortTrackerEvent> readings = new LinkedList<>();

        final ReadingsParser p = new ReadingsParser();
        p.setHandler(new ReadingsHandler() {
            @Override
            public void handleEvent(final ShortTrackerEvent e, final AlertType[] alerts) {
                e.setDeviceImei(device);
                readings.add(e);
            }

            @Override
            public Long getShipmentId(final String sn, final int tripCount) {
                return shipmentId;
            }
        });

        p.parse(r);
        return readings;
    }

}
