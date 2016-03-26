/**
 *
 */
package com.visfresh.dao.impl;

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
            final InterimStop stop = new InterimStop();
            stop.setId(((Number) row.get("id")).longValue());
            stop.setDate((Date) row.get("date"));
            stop.setLatitude(((Number) row.get("latitude")).doubleValue());
            stop.setLongitude(((Number) row.get("longitude")).doubleValue());
            stopToLocation.put(stop, ((Number) row.get("location")).longValue());
            stop.setTime(((Number) row.get("pause")).intValue());
            stops.add(stop);
        }

        resolveLocations(stopToLocation);
        return stops;
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
    public void add(final Shipment s, final InterimStop locs) {
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", s.getId());
        params.put("location", locs.getLocation().getId());
        params.put("latitude", locs.getLatitude());
        params.put("longitude", locs.getLongitude());
        params.put("pause", locs.getTime());
        params.put("date", locs.getDate());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update("insert into interimstops(shipment, location, latitude, longitude, pause, `date`)"
                + " values(:shipment, :location, :latitude, :longitude, :pause, :date)",
                new MapSqlParameterSource(params), keyHolder);
        if (keyHolder.getKey() != null) {
            locs.setId(keyHolder.getKey().longValue());
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
}
