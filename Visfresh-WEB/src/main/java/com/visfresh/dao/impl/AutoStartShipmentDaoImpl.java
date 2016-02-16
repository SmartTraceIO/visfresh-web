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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.AutoStartShipmentConstants;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AutoStartShipmentDaoImpl
    extends EntityWithCompanyDaoImplBase<AutoStartShipment, Long>
    implements AutoStartShipmentDao {

    public static final String TABLE = "autostartshipments";

    private static final String ID_FIELD = "id";
    private static final String COMPANY_FIELD = "company";
    private static final String TEMPLATE_FIELD = "template";
    private static final String PRIORITY_FIELD = "priority";

    private static final String LOCATION_REL_TABLE = "autostartlocations";
    private static final String LOCATION_DIRECTION = "direction";
    private static final String LOCATION_LOCATION = "location";
    private static final String LOCATION_CONFIG = "config";

    private Map<String, String> propertyToDbMap = new HashMap<>();

    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private ShipmentTemplateDao shipmentTemplateDao;
    /**
     * Default constructor.
     */
    public AutoStartShipmentDaoImpl() {
        super();
        propertyToDbMap.put(AutoStartShipmentConstants.ID, ID_FIELD);
        propertyToDbMap.put(AutoStartShipmentConstants.PRIORITY, PRIORITY_FIELD);
        propertyToDbMap.put(AutoStartShipmentConstants.TEMPLATE, TEMPLATE_FIELD);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <E extends AutoStartShipment> E save(final E aut) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.put(ID_FIELD, aut.getId());
        paramMap.put(COMPANY_FIELD, aut.getCompany().getId());
        paramMap.put(TEMPLATE_FIELD, aut.getTemplate().getId());
        paramMap.put(PRIORITY_FIELD, aut.getPriority());

        String sql;
        final List<String> fields = new LinkedList<String>(paramMap.keySet());

        if (aut.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            aut.setId(keyHolder.getKey().longValue());
        }

        mergeLocations(aut);
        return aut;
    }
    private void mergeLocations(final AutoStartShipment cfg) {
        final Set<Long> oldLocFrom = new HashSet<>();
        final Set<Long> oldLocTo = new HashSet<>();

        getLocationIds(cfg, oldLocFrom, oldLocTo);

        final Set<Long> newLocFrom = new HashSet<>();
        final Set<Long> newLocTo = new HashSet<>();

        merge(cfg.getShippedFrom(), oldLocFrom, newLocFrom);
        merge(cfg.getShippedTo(), oldLocTo, newLocTo);

        if (!(oldLocFrom.isEmpty() && oldLocTo.isEmpty())) {
            //disconnect redundant locations
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("config", cfg.getId());

            for (final Long id : oldLocFrom) {
                params.put("id_" + id, id);
            }

            for (final Long id : oldLocTo) {
                params.put("id_" + id, id);
            }

            final Set<Long> ids = new HashSet<>();
            ids.addAll(oldLocFrom);
            ids.addAll(oldLocTo);

            final String sql = "delete from " + LOCATION_REL_TABLE
                    + " where " + LOCATION_CONFIG + "=:config and "
                    + LOCATION_LOCATION + " in (:id_"
                    + StringUtils.combine(ids, ",:id_") + ")";
            jdbc.update(sql, params);
        }

        if (!(newLocFrom.isEmpty() && newLocTo.isEmpty())) {
            //connect new locations
            String sql = "insert into " + LOCATION_REL_TABLE
                + "(" + LOCATION_CONFIG + ", " + LOCATION_LOCATION + ", " + LOCATION_DIRECTION
                + ") values ";
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("config", cfg.getId());

            //from locations
            final List<String> inserts = new LinkedList<>();
            for (final Long id : newLocFrom) {
                params.put("id_" + id, id);
                inserts.add("(:config, :id_" + id + ", 'from')");
            }

            //to locations
            for (final Long id : newLocTo) {
                params.put("id_" + id, id);
                inserts.add("(:config, :id_" + id + ", 'to')");
            }

            sql += StringUtils.combine(inserts, ",");
            jdbc.update(sql, params);
        }
    }
    /**
     * @param locations full location list.
     * @param oldLocTo old locations.
     * @param newLocTo new locations.
     */
    private void merge(final List<LocationProfile> locations, final Set<Long> oldLocTo,
            final Set<Long> newLocTo) {
        for (final LocationProfile locationProfile : locations) {
            final Long id = locationProfile.getId();
            if (!oldLocTo.remove(id)) {
                newLocTo.add(id);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#getCompanyFieldName()
     */
    @Override
    protected String getCompanyFieldName() {
        return COMPANY_FIELD;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbMap;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID_FIELD;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected AutoStartShipment createEntity(final Map<String, Object> map) {
        final AutoStartShipment cfg = new AutoStartShipment();
        cfg.setId(((Number) map.get(ID_FIELD)).longValue());
        cfg.setPriority(((Number) map.get(PRIORITY_FIELD)).intValue());
        return cfg;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final AutoStartShipment t,
            final Map<String, Object> row, final Map<String, Object> cache) {
        super.resolveReferences(t, row, cache);
        resolveTemplate(t, row, cache);
        resolveLocations(t, row, cache);
    }

    /**
     * @param t shipment config.
     * @param row row.
     * @param cache cache.
     */
    private void resolveLocations(final AutoStartShipment t,
            final Map<String, Object> row, final Map<String, Object> cache) {
        final Set<Long> locFrom = new HashSet<>();
        final Set<Long> locTo = new HashSet<>();

        getLocationIds(t, locFrom, locTo);

        //get locations
        final List<Long> locationIds = new LinkedList<>();
        locationIds.addAll(locFrom);
        locationIds.addAll(locTo);

        final List<LocationProfile> locations = locationProfileDao.findAll(locationIds);

        addLocations(t.getShippedFrom(), locations, locFrom);
        addLocations(t.getShippedTo(), locations, locTo);
    }

    /**
     * @param t
     * @param locFrom
     * @param locTo
     */
    protected void getLocationIds(final AutoStartShipment t,
            final Set<Long> locFrom, final Set<Long> locTo) {
        final Map<String, Object> params = new HashMap<>();
        params.put("cfg", t.getId());

        //get location ID's
        final List<Map<String, Object>> rows = jdbc.queryForList("select * from "
                + LOCATION_REL_TABLE + " where " + LOCATION_CONFIG + " = :cfg", params);

        for (final Map<String,Object> map : rows) {
            final String direction = (String) map.get(LOCATION_DIRECTION);
            final Long locationId = ((Number) map.get(LOCATION_LOCATION)).longValue();
            if ("from".equals(direction)) {
                locFrom.add(locationId);
            } else if ("to".equals(direction)) {
                locTo.add(locationId);
            } else {
                throw new IllegalArgumentException("Undefined location direction " + direction);
            }
        }
    }
    /**
     * @param target target list of location.
     * @param source source list of location.
     * @param ids set of location ID to copy.
     */
    private void addLocations(final List<LocationProfile> target,
            final List<LocationProfile> source, final Set<Long> ids) {
        for (final LocationProfile l : source) {
            if (ids.contains(l.getId())) {
                target.add(l);
            }
        }
    }

    /**
     * @param t
     * @param row
     * @param cache
     */
    protected void resolveTemplate(final AutoStartShipment t,
            final Map<String, Object> row, final Map<String, Object> cache) {
        final String id = ((Number) row.get(TEMPLATE_FIELD)).toString();
        final String cacheKey = "template_" + id;
        ShipmentTemplate tpl = (ShipmentTemplate) cache.get(cacheKey);
        if (tpl == null) {
            tpl = shipmentTemplateDao.findOne(Long.valueOf(id));
            cache.put(cacheKey, tpl);
        }
        t.setTemplate(tpl);
    }
}
