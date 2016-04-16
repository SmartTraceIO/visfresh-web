/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlternativeLocationsDaoImpl implements AlternativeLocationsDao {
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
    public AlternativeLocationsDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.AlternativeLocationsDao#getAlternativeLocations(com.visfresh.entities.Shipment)
     */
    @Override
    public AlternativeLocations getByShipment(final Shipment s) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", s.getId());

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from alternativelocations where shipment=:shipment order by loctype, location", params);

        final AlternativeLocations a = new AlternativeLocations();

        final List<Long> froms = new LinkedList<>();
        final List<Long> tos = new LinkedList<>();
        final List<Long> interims = new LinkedList<>();

        for (final Map<String, Object> map : rows) {
            final String loctype = (String) map.get("loctype");
            final Long id = ((Number) map.get("location")).longValue();

            if (loctype.equals("from")) {
                froms.add(id);
            } else if (loctype.equals("to")) {
                tos.add(id);
            } else if (loctype.equals("interim")) {
                interims.add(id);
            }
        }

        final Set<Long> ids = new HashSet<>();
        ids.addAll(froms);
        ids.addAll(tos);
        ids.addAll(interims);

        final Map<Long, LocationProfile> locations = getLocationMap(ids);
        setLocations(froms, locations, a.getFrom());
        setLocations(tos, locations, a.getTo());
        setLocations(interims, locations, a.getInterim());

        return a;
    }
    /**
     * @param ids
     * @param source
     * @param locs
     */
    private void setLocations(final List<Long> ids,
            final Map<Long, LocationProfile> source, final List<LocationProfile> locs) {
        for (final Long id : ids) {
            locs.add(source.get(id));
        }
    }

    /**
     * @return
     */
    private Map<Long, LocationProfile> getLocationMap(final Set<Long> ids) {
        final List<LocationProfile> all = locationProfileDao.findAll(ids);
        final Map<Long, LocationProfile> map = new HashMap<>();
        for (final LocationProfile l : all) {
            map.put(l.getId(), l);
        }
        return map;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.AlternativeLocationsDao#saveAlternativeLocations(com.visfresh.entities.Shipment, com.visfresh.entities.AlternativeLocations)
     */
    @Override
    public void save(final Shipment s, final AlternativeLocations locs) {
        if (!(locs.getFrom().isEmpty() && locs.getTo().isEmpty() && locs.getInterim().isEmpty())) {
            final Map<String, Object> params = new HashMap<>();
            params.put("shipment", s.getId());

            //clear old alternative locations
            jdbc.update("delete from alternativelocations where shipment = :shipment", params);

            //add new alternative locations
            final StringBuilder sql = new StringBuilder();
            sql.append("insert into alternativelocations (shipment, location, loctype) values ");

            final List<String> values = new LinkedList<>();
            addLocations("from", locs.getFrom(), values, params);
            addLocations("to", locs.getTo(), values, params);
            addLocations("interim", locs.getInterim(), values, params);

            sql.append(StringUtils.combine(values, ","));

            jdbc.update(sql.toString(), params);
        }
    }

    /**
     * @param loctype
     * @param locs
     * @param values
     */
    private void addLocations(final String loctype, final List<LocationProfile> locs,
            final List<String> values, final Map<String, Object> params) {
        for (final LocationProfile l : locs) {
            final String key = loctype + "_" + l.getId();
            params.put(key, l.getId());
            values.add("(:shipment, :" + key + ", '" + loctype + "')");
        }
    }
}
