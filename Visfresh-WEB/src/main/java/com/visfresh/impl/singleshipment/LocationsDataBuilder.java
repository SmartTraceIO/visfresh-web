/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.utils.StringUtils;

/**
 * Builder for start/stop location, interim stops and location alternatives.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationsDataBuilder implements SingleShipmentPartBuilder {
    protected final NamedParameterJdbcTemplate jdbc;
    protected final Long shipmentId;

    private final Map<Long, Map<Long, InterimStopBean>> interimStops = new HashMap<>();
    private final Map<Long, Map<Long, LocationProfileBean>> startLocationAlternatives = new HashMap<>();
    private final Map<Long, Map<Long, LocationProfileBean>> endLocationAlternatives = new HashMap<>();
    private final Map<Long, Map<Long, LocationProfileBean>> interimLocationAlternatives = new HashMap<>();
    private final Map<Long, LocationProfileBean> startLocations = new HashMap<>();
    private final Map<Long, LocationProfileBean> endLocations = new HashMap<>();

    private static final String QUERY = loadQuery();

    /**
     * @param jdbc JDBC template.
     * @param shipmentId shipment ID.
     */
    public LocationsDataBuilder(final NamedParameterJdbcTemplate jdbc, final Long shipmentId) {
        super();
        this.jdbc = jdbc;
        this.shipmentId = shipmentId;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#getPriority()
     */
    @Override
    public int getPriority() {
        return MIN_PRIORITY; //highest priority;
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#build(com.visfresh.impl.singleshipment.SingleShipmentBuildContext)
     */
    @Override
    public void build(final SingleShipmentBuildContext context) {
        final SingleShipmentData data = context.getData();
        if (data == null) {
            return;
        }

        addDataToBean(data.getBean());
        for (final SingleShipmentBean bean : data.getSiblings()) {
            addDataToBean(bean);
        }
    }

    /**
     * @param bean bean for add the data to.
     */
    private void addDataToBean(final SingleShipmentBean bean) {
        final Long id = bean.getShipmentId();
        if (this.endLocationAlternatives.containsKey(id)) {
            bean.getEndLocationAlternatives().addAll(endLocationAlternatives.get(id).values());
        }
        if (this.startLocationAlternatives.containsKey(id)) {
            bean.getStartLocationAlternatives().addAll(startLocationAlternatives.get(id).values());
        }
        if (this.interimLocationAlternatives.containsKey(id)) {
            bean.getInterimLocationAlternatives().addAll(interimLocationAlternatives.get(id).values());
        }
        if (this.interimStops.containsKey(id)) {
            bean.getInterimStops().addAll(interimStops.get(id).values());
        }
        bean.setStartLocation(startLocations.get(id));
        bean.setEndLocation(endLocations.get(id));
    }

    /**
     * Clears the builder.
     */
    private void clear() {
        interimStops.clear();
        startLocationAlternatives.clear();
        endLocationAlternatives.clear();
        interimLocationAlternatives.clear();
        startLocations.clear();
        endLocations.clear();
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.singleshipment.SingleShipmentPartBuilder#fetchData()
     */
    @Override
    public void fetchData() {
        clear();

        final String query = QUERY;
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", shipmentId);

        final List<Map<String, Object>> rows = jdbc.queryForList(query, params);
        for (final Map<String, Object> row : rows) {
            processRow(row);
        }
    }
    /**
     * @return
     */
    private static String loadQuery() {
        try {
            final String str = StringUtils.getContent(LocationsDataBuilder.class.getResource("getLocations.sql"),
                    "UTF-8");
            return str.replace("6343", ":shipment");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param row
     */
    private void processRow(final Map<String, Object> row) {
        final Long shipment = asLong(row.get("shipment"));
        final LocationProfileBean loc = createLocation(row);

        if (row.get("stopId") != null) {
            final InterimStopBean stop = createInterimStop(shipment, row);
            stop.setLocation(loc);
            getOrCreateMap(interimStops, shipment).put(stop.getId(), stop);
        } else if (row.get("altLocId") != null) {
            if (asBoolean(row.get("isStart"))) {
                getOrCreateMap(startLocationAlternatives, shipment).put(loc.getId(), loc);
            } else if (asBoolean(row.get("isStop"))) {
                getOrCreateMap(endLocationAlternatives, shipment).put(loc.getId(), loc);
            } else if (asBoolean(row.get("isInterim"))) {
                getOrCreateMap(interimLocationAlternatives, shipment).put(loc.getId(), loc);
            }
        } else if (asBoolean(row.get("isStart"))) {
            startLocations.put(shipment, loc);
        } else if (asBoolean(row.get("isStop"))) {
            endLocations.put(shipment, loc);
        }
    }

    /**
     * @param row DB row.
     * @return location profile bean.
     */
    private LocationProfileBean createLocation(final Map<String, Object> row) {
        final LocationProfileBean loc = new LocationProfileBean();
        loc.setAddress((String) row.get("address"));
        loc.setCompanyName((String) row.get("companyName"));
        loc.setId(asLong(row.get("id")));
        loc.setStart(asBoolean(row.get("forStart")));
        loc.setStop(asBoolean(row.get("forStop")));
        loc.setInterim(asBoolean(row.get("forInterim")));
        loc.setName((String) row.get("name"));
        loc.setNotes((String) row.get("notes"));
        loc.setRadius(asInteger(row.get("radius")));
        loc.getLocation().setLatitude(asDouble(row.get("latitude")));
        loc.getLocation().setLongitude(asDouble(row.get("longitude")));
        return loc;
    }

    /**
     * @param shipment
     * @param row
     * @return interim stop.
     */
    private InterimStopBean createInterimStop(final Long shipment, final Map<String, Object> row) {
        final InterimStopBean stop = new InterimStopBean();
        stop.setId(asLong(row.get("stopId")));
        stop.setStopDate((Date) row.get("stopDate"));
        stop.setTime(asInteger(row.get("stopTime")));
        return stop;
    }
    /**
     * @param map
     * @param shipment
     * @return
     */
    private <E> Map<Long, E> getOrCreateMap(final Map<Long, Map<Long, E>> map, final Long shipment) {
        Map<Long, E> list = map.get(shipment);
        if (list == null) {
            list = new HashMap<>();
            map.put(shipment, list);
        }
        return list;
    }
}
