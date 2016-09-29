/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class InterimStopDaoImpl implements InterimStopDao {
    /**
     * JDBC template.
     */
    @Autowired(required = true)
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private LocationProfileDao locationProfileDao;

    /**
     * Default constructor.
     */
    public InterimStopDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.InterimStopDao#getByShipment(com.visfresh.entities.Shipment)
     */
    @Override
    public List<InterimStop> getByShipment(final Shipment s) {
        if (s == null) {
            return new LinkedList<>();
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", s.getId());

        final Map<InterimStop, Long> stopToLocation = new HashMap<>();
        final List<InterimStop> stops = new LinkedList<>();
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from interimstops where shipment = :shipment order by `date`, id", params);
        for (final Map<String, Object> row : rows) {
            final InterimStop stop = createInterimStop(row);
            stopToLocation.put(stop, ((Number) row.get("location")).longValue());
            stops.add(stop);
        }

        resolveLocations(stopToLocation);
        return stops;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.InterimStopDao#getByShipment(com.visfresh.entities.Shipment)
     */
    @Override
    public Map<Long, List<InterimStop>> getByShipmentIds(final Collection<Long> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        final Map<Long, List<InterimStop>> stops = new HashMap<>();
        for (final Long id : ids) {
            stops.put(id, new LinkedList<InterimStop>());
        }

        final Map<InterimStop, Long> stopToLocation = new HashMap<>();
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from interimstops where shipment in ("
                        + StringUtils.combine(ids, ",") + ") order by `date`, id", new HashMap<String, Object>());

        for (final Map<String, Object> row : rows) {
            final InterimStop stop = createInterimStop(row);
            stopToLocation.put(stop, ((Number) row.get("location")).longValue());

            final Long shipment = ((Number) row.get("shipment")).longValue();
            stops.get(shipment).add(stop);
        }

        resolveLocations(stopToLocation);
        return stops;
    }

    /**
     * @param row
     * @return
     */
    private InterimStop createInterimStop(final Map<String, Object> row) {
        final InterimStop stop = new InterimStop();
        stop.setId(((Number) row.get("id")).longValue());
        stop.setDate((Date) row.get("date"));
        stop.setLatitude(((Number) row.get("latitude")).doubleValue());
        stop.setLongitude(((Number) row.get("longitude")).doubleValue());
        stop.setTime(((Number) row.get("pause")).intValue());
        return stop;
    }

    /**
     * @param stopToLocation
     */
    private void resolveLocations(final Map<InterimStop, Long> stopToLocation) {
        final List<LocationProfile> locs = locationProfileDao.findAll(stopToLocation.values());
        //create map by ID.
        final Map<Long, LocationProfile> locMap = new HashMap<>();
        for (final LocationProfile loc : locs) {
            locMap.put(loc.getId(), loc);
        }

        //assign to interim stops
        for (final Map.Entry<InterimStop, Long> e : stopToLocation.entrySet()) {
            e.getKey().setLocation(locMap.get(e.getValue()));
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.InterimStopDao#add(com.visfresh.entities.Shipment, com.visfresh.entities.InterimStop)
     */
    @Override
    public void add(final Shipment s, final InterimStop stop) {
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", s.getId());
        params.put("location", stop.getLocation().getId());
        params.put("latitude", stop.getLatitude());
        params.put("longitude", stop.getLongitude());
        params.put("pause", stop.getTime());
        params.put("date", stop.getDate());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update("insert into interimstops(shipment, location, latitude, longitude, pause, `date`)"
                + " values(:shipment, :location, :latitude, :longitude, :pause, :date)",
                new MapSqlParameterSource(params), keyHolder);
        if (keyHolder.getKey() != null) {
            stop.setId(keyHolder.getKey().longValue());
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.InterimStopDao#updateTime(java.lang.Long, int)
     */
    @Override
    public void updateTime(final Long id, final int minutes) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("pause", minutes);

        jdbc.update("update interimstops set pause = :pause where id = :id", params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.InterimStopDao#delete(com.visfresh.entities.InterimStop)
     */
    @Override
    public void delete(final InterimStop stp) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", stp.getId());

        jdbc.update("delete from interimstops where id = :id", params);
    }
}
