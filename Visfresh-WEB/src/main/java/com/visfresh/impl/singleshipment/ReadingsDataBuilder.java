/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Location;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentLocationBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReadingsDataBuilder implements SingleShipmentPartBuilder {
    protected final TrackerEventDao dao;
    private Map<Long, SingleShipmentBean> beans = new HashMap<>();
    private final Long shipmentId;
    private List<TrackerEventDto> events = new LinkedList<>();

    /**
     * @param dao JDBC template.
     * @param shipmentId shipment ID.
     * @param companyId company ID.
     * @param siblings list of siblings.
     */
    public ReadingsDataBuilder(final TrackerEventDao dao,
            final Long shipmentId, final Set<Long> siblings) {
        super();
        this.dao = dao;
        this.shipmentId = shipmentId;

        beans.put(shipmentId, null);
        for (final Long id : siblings) {
            beans.put(id, null);
        }
    }

    /**
     * @return
     */
    protected SingleShipmentBean createBean() {
        final SingleShipmentBean bean = new SingleShipmentBean();
        bean.setMinTemp(1000);
        bean.setMaxTemp(-273);
        return bean;
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#getPriority()
     */
    @Override
    public int getPriority() {
        return MAX_PRIORITY; //highest priority;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#build(com.visfresh.impl.singleshipment.SingleShipmentBuildContext)
     */
    @Override
    public void build(final SingleShipmentBuildContext context) {
        final SingleShipmentData data = context.getData();
        final SingleShipmentBean mainBean = data.getBean();
        if (mainBean == null) {
            return;
        }

        applyData(mainBean, context);
        for (final SingleShipmentBean sib : data.getSiblings()) {
            applyData(sib, context);
        }

        if (events.size() > 0) {
            final List<AlertBean> alerts = new LinkedList<>(mainBean.getSentAlerts());
            final Map<AlertBean, SingleShipmentLocationBean> alertReadings = new HashMap<>();

            for (final TrackerEventDto e : events) {
                final SingleShipmentLocationBean eb = new SingleShipmentLocationBean(e);
                data.getLocations().add(eb);

                final Iterator<AlertBean> iter = alerts.iterator();
                while (iter.hasNext()) {
                    final AlertBean a = iter.next();

                    boolean foundReading = false;
                    if (e.getId().equals(a.getTrackerEventId())) {
                        eb.getAlerts().add(a);
                        alertReadings.remove(a);
                        iter.remove();
                        foundReading = true;
                    } else {
                        final SingleShipmentLocationBean oldEb = alertReadings.get(a);
                        if (oldEb != null
                                && timeDistance(a.getDate(), e.getTime()) > timeDistance(a.getDate(), oldEb.getTime())) {
                            oldEb.getAlerts().add(a);
                            alertReadings.remove(a);
                            iter.remove();
                            foundReading = true;
                        }
                    }

                    if (a.getTrackerEventId() == null && !foundReading){
                        alertReadings.put(a, eb);
                    }
                }
            }

            //add nearest alerts to readings if tracker event ID is not set
            for (final AlertBean a : alerts) {
                if (a.getTrackerEventId() == null){
                    final SingleShipmentLocationBean eb = alertReadings.get(a);
                    if (eb != null) {
                        eb.getAlerts().add(a);
                    }
                }
            }
        }
    }

    /**
     * @param d1 first time.
     * @param d2 second time.
     * @return distance between two dates in milliseconds.
     */
    private long timeDistance(final Date d1, final Date d2) {
        return Math.abs(d1.getTime() - d2.getTime());
    }
    /**
     * @param bean single shipment bean.
     * @param context build context.
     */
    private void applyData(final SingleShipmentBean bean, final SingleShipmentBuildContext context) {
        final SingleShipmentBean b = beans.get(bean.getShipmentId());

        bean.setFirstReadingTime(b.getFirstReadingTime());
        bean.setMaxTemp(b.getMaxTemp());
        bean.setMinTemp(b.getMinTemp());
        bean.setCurrentLocation(b.getCurrentLocation());
        if (b.getCurrentLocation() != null) {
            bean.setCurrentLocationDescription(
                    context.getLocationDescription(b.getCurrentLocation()));
        }
        bean.setBatteryLevel(b.getBatteryLevel());
        bean.setLastReadingTemperature(b.getLastReadingTemperature());
        bean.setLastReadingTime(b.getLastReadingTime());
    }
    /**
     * Clears the builder.
     */
    private void clear() {
        events.clear();
        for (final Long id : new LinkedList<>(beans.keySet())) {
            beans.put(id, createBean());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#fetchData()
     */
    @Override
    public void fetchData() {
        clear();

        int page = 0;
        final int size = 10000;

        //find first previous normal temperature.
        while (true) {
            final List<TrackerEventDto> events = dao.getEventPart(beans.keySet(), page, size);
            for (final TrackerEventDto e : events) {
                processRow(beans.get(e.getShipmentId()), e, e.getShipmentId().equals(shipmentId));
            }
            if(events.size() < size) {
                break;
            }
            page++;
        }
    }
    /**
     * @param bean single shipment bean.
     * @param row DB row.
     * @param isMainBean
     */
    private void processRow(final SingleShipmentBean bean, final TrackerEventDto e, final boolean isMainBean) {
        if (bean.getFirstReadingTime() == null) {
            bean.setFirstReadingTime(e.getTime());
        }

        final double t = e.getTemperature();
        bean.setMaxTemp(Math.max(bean.getMaxTemp(), t));
        bean.setMinTemp(Math.min(bean.getMinTemp(), t));

        if (e.getLatitude() != null && e.getLongitude() != null) {
            bean.setCurrentLocation(new Location(e.getLatitude(), e.getLongitude()));
        }

        //last reading data
        bean.setBatteryLevel(e.getBattery());
        bean.setLastReadingTemperature(e.getTemperature());
        bean.setLastReadingTime(e.getTime());

        if (isMainBean) {
            events.add(e);
        }
    }
}
