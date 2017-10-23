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

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.entities.Location;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentLocationBean;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReadingsDataBuilder implements SingleShipmentPartBuilder {
    protected final NamedParameterJdbcTemplate jdbc;
    private Map<Long, SingleShipmentBean> beans = new HashMap<>();
    private final Long shipmentId;
    private List<TrackerEventDto> events = new LinkedList<>();

    /**
     * @param jdbc JDBC template.
     * @param shipmentId shipment ID.
     * @param companyId company ID.
     * @param siblings list of siblings.
     */
    public ReadingsDataBuilder(final NamedParameterJdbcTemplate jdbc,
            final Long shipmentId, final Set<Long> siblings) {
        super();
        this.jdbc = jdbc;
        this.shipmentId = shipmentId;

        beans.put(shipmentId, createBean());
        for (final Long sib : siblings) {
            beans.put(sib, createBean());
        }
    }

    /**
     * @return
     */
    protected SingleShipmentBean createBean() {
        final SingleShipmentBean bean = new SingleShipmentBean();
        bean.setMinTemp(Double.MAX_VALUE / 2.);
        bean.setMinTemp(Double.MIN_VALUE / 2.);
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

                    boolean foundReading = e.getId().equals(a.getTrackerEventId());
                    if (!foundReading) {
                        final SingleShipmentLocationBean oldEb = alertReadings.get(a);
                        if (oldEb != null
                                && timeDistance(a.getDate(), e.getTime()) > timeDistance(a.getDate(), eb.getTime())) {
                            foundReading = true;
                        }
                    }

                    if (foundReading) {
                        eb.getAlerts().add(a);
                        alertReadings.remove(a);
                        iter.remove();
                    } else {
                        alertReadings.put(a, eb);
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
        beans.clear();
        events.clear();
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
            final List<Map<String, Object>> rows = jdbc.queryForList(
                    "select * from trackerevents e where e.shipment in ("
                            + StringUtils.combine(beans.keySet(), ",") + ") order by e.time, e.id limit "
                            + (page * size) + "," + size,
                    new HashMap<String, Object>());
            for (final Map<String, Object> row : rows) {
                final Long id = DaoImplBase.dbLong(row.get("shipment"));
                processRow(beans.get(id), row, id.equals(shipmentId));
            }

            if(rows.size() < size) {
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
    private void processRow(final SingleShipmentBean bean, final Map<String, Object> row, final boolean isMainBean) {
        final TrackerEventDto e = createEvent(row);

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

    /**
     * @param row DB row.
     * @return tracker event.
     */
    private TrackerEventDto createEvent(final Map<String, Object> row) {
        final TrackerEventDto e = new TrackerEventDto();

        e.setBattery(DaoImplBase.dbInteger(row.get("battery")));
        e.setCreatedOn((Date) row.get("createdon"));
        e.setId(DaoImplBase.dbLong(row.get("id")));
        e.setLatitude(DaoImplBase.dbDouble(row.get("latitude")));
        e.setLongitude(DaoImplBase.dbDouble(row.get("longitude")));
        e.setTemperature(DaoImplBase.dbDouble(row.get("temperature")));
        e.setTime((Date) row.get("time"));
        e.setType(TrackerEventType.valueOf((String) row.get("type")));
        e.setShipmentId(DaoImplBase.dbLong(row.get("shipment")));
        e.setDeviceImei((String) row.get("device"));
        return e;
    }
}
