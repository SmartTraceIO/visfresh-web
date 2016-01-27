/**
 *
 */
package com.visfresh.controllers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GetShipmentsStats {
    private long startTime;
    private long endTime;

    private final Map<Shipment, Long> alertStarts = new LinkedHashMap<>();
    private final Map<Shipment, Long> alertEnds = new HashMap<>();

    private final Map<Shipment, Long> siblingStarts = new HashMap<>();
    private final Map<Shipment, Long> siblingEnds = new HashMap<>();
    private long getShipmentsStart;
    private long getShipmentEnd;

    /**
     * Default constructor.
     */
    public GetShipmentsStats() {
        super();
    }

    /**
     *
     */
    public void totalStart() {
        startTime = System.currentTimeMillis();
    }

    /**
     *
     */
    public void startGetShipmentsByFilter() {
        getShipmentsStart = System.currentTimeMillis();
    }

    /**
     * @param shipments
     */
    public void endGetShipmentsByFilter(final List<Shipment> shipments) {
        getShipmentEnd = System.currentTimeMillis();
    }

    /**
     * @param s shipment.
     */
    public void startAlerts(final Shipment s) {
        alertStarts.put(s, System.currentTimeMillis());
    }
    /**
     * @param s shipment.
     */
    public void endAlerts(final Shipment s) {
        alertEnds.put(s, System.currentTimeMillis());
    }
    /**
     * @param s shipment.
     */
    public void startSiblings(final Shipment s) {
        siblingStarts.put(s, System.currentTimeMillis());
    }
    /**
     * @param s shipment.
     */
    public void endSiblings(final Shipment s) {
        siblingEnds.put(s, System.currentTimeMillis());
    }

    /**
     *
     */
    public void totalEnd() {
        endTime = System.currentTimeMillis();
    }

    /**
     * @return
     */
    public String buildStats() {
        final StringBuilder sb = new StringBuilder(">>> Statistics for getShipments method:\n");
        final long shipmentsTotal = this.getShipmentEnd - this.getShipmentsStart;
        sb.append("fetch shipments by filter: total=");
        sb.append(shipmentsTotal).append(" ms, avg=");
        if (alertStarts.isEmpty()) {
            sb.append(0);
        } else {
            sb.append(shipmentsTotal / alertStarts.size());
        }
        sb.append(" ms\n");

        sb.append("get alerts: ");
        buildStats(sb, alertStarts, alertEnds);
        sb.append('\n');

        sb.append("get siblings: ");
        buildStats(sb, siblingStarts, siblingEnds);
        sb.append('\n');

        sb.append("total time: ").append(endTime - startTime).append(" ms");
        return sb.toString();
    }

    /**
     * @param sb target buffer.
     * @param starts start times for shipments.
     * @param ends end times for shipments.
     */
    private void buildStats(final StringBuilder sb, final Map<Shipment, Long> starts,
            final Map<Shipment, Long> ends) {
        long total = 0;
        for (final Map.Entry<Shipment, Long> e : starts.entrySet()) {
            total += ends.get(e.getKey()) - e.getValue();
        }

        double avg = 0;
        if (!starts.isEmpty()) {
            avg = (double) total / starts.size();
        }

        sb.append("total=").append(total);
        sb.append(" ms, avg=").append(avg).append(" ms");
    }
}
