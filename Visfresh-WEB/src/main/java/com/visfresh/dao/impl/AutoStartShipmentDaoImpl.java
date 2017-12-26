/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.AutoStartShipmentConstants;
import com.visfresh.constants.ShipmentTemplateConstants;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.Filter;
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
    extends EntityWithCompanyDaoImplBase<AutoStartShipment, AutoStartShipment, Long>
    implements AutoStartShipmentDao {

    public static final String TABLE = "autostartshipments";

    protected static final String ID_FIELD = "id";
    protected static final String COMPANY_FIELD = "company";
    protected static final String TEMPLATE_FIELD = "template";
    protected static final String PRIORITY_FIELD = "priority";

    protected static final String LOCATION_REL_TABLE = "autostartlocations";
    protected static final String LOCATION_DIRECTION = "direction";
    protected static final String LOCATION_LOCATION = "location";
    protected static final String LOCATION_CONFIG = "config";
    protected static final String LOCATION_ORDER = "sortorder";
    protected static final String START_ON_MOVING_FIELD = "startonmoving";

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
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <E extends AutoStartShipment> E save(final E aut) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        paramMap.put(ID_FIELD, aut.getId());
        paramMap.put(COMPANY_FIELD, aut.getCompany().getId());
        paramMap.put(PRIORITY_FIELD, aut.getPriority());
        paramMap.put(START_ON_MOVING_FIELD, aut.isStartOnLeaveLocation());
        if (aut.getId() == null) {
            paramMap.put(TEMPLATE_FIELD, aut.getTemplate().getId());
        }

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
        final List<Long> oldLocFrom = new LinkedList<>();
        final List<Long> oldLocTo = new LinkedList<>();
        final List<Long> oldLocInterim = new LinkedList<>();

        getLocationIds(cfg, oldLocFrom, oldLocTo, oldLocInterim);

        final List<Long> newLocFrom = new LinkedList<>();
        final List<Long> newLocTo = new LinkedList<>();
        final List<Long> newLocInterim = new LinkedList<>();

        merge(cfg.getShippedFrom(), oldLocFrom, newLocFrom);
        merge(cfg.getShippedTo(), oldLocTo, newLocTo);
        merge(cfg.getInterimStops(), oldLocInterim, newLocInterim);

        if (!(oldLocFrom.isEmpty() && oldLocTo.isEmpty() && oldLocInterim.isEmpty())) {
            //disconnect redundant locations
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("config", cfg.getId());

            final List<String> conditions = new LinkedList<>();
            addToCondition("from", oldLocFrom, conditions, params);
            addToCondition("to", oldLocTo, conditions, params);
            addToCondition("interim", oldLocInterim, conditions, params);

            final String sql = "delete from " + LOCATION_REL_TABLE
                    + " where " + LOCATION_CONFIG + "=:config and ("
                    + StringUtils.combine(conditions, " or ") + ")";
            jdbc.update(sql, params);
        }

        if (!(newLocFrom.isEmpty() && newLocTo.isEmpty() && newLocInterim.isEmpty())) {
            //connect new locations
            String sql = "insert into " + LOCATION_REL_TABLE
                + "\n(" + LOCATION_CONFIG + ", " + LOCATION_LOCATION + ", " + LOCATION_DIRECTION
                + ", " + LOCATION_ORDER + ")\n values\n ";
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("config", cfg.getId());

            final List<String> inserts = new LinkedList<>();
            //from locations
            addToSql("from", newLocFrom, params, inserts);
            //to locations
            addToSql("to", newLocTo, params, inserts);
            //interim locations
            addToSql("interim", newLocInterim, params, inserts);

            sql += StringUtils.combine(inserts, ",");
            jdbc.update(sql, params);
        }
    }
    /**
     * @param type
     * @param locations
     * @param conditions
     * @param params
     */
    private void addToCondition(final String type, final List<Long> locations,
            final List<String> conditions, final Map<String, Object> params) {
        int index = 0;
        for (final Long id : locations) {
            final String idKey = type + "_" + index;
            params.put(idKey, id);

            conditions.add("(" + LOCATION_LOCATION
                    + "=:" + idKey + " and " + LOCATION_DIRECTION + "='"
                    + type + "')");
            index++;
        }
    }
    /**
     * @param type
     * @param locations
     * @param targetList
     */
    private void addToSql(final String type, final List<Long> locations,
            final Map<String, Object> params, final List<String> targetList) {
        int order = 0;
        for (final Long id : locations) {
            params.put("id_" + id, id);
            final String orderKey = "order" + type + "_" + order;
            targetList.add("(:config, :id_" + id + ", '" + type + "',:" + orderKey + ")\n");
            //order
            params.put(orderKey, order);
            order++;
        }
    }

    /**
     * @param locations full location list.
     * @param oldLocations old locations.
     * @param newLocTo new locations.
     */
    private void merge(final List<LocationProfile> locations, final List<Long> oldLocations,
            final List<Long> newLocTo) {
        for (final LocationProfile locationProfile : locations) {
            final Long id = locationProfile.getId();
            if (!removeFromList(oldLocations, id)) {
                newLocTo.add(id);
            }
        }
    }

    /**
     * @param oldLocations
     * @param id
     * @return
     */
    private boolean removeFromList(final List<Long> oldLocations, final Long id) {
        final Iterator<Long> iter = oldLocations.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals(id)) {
                iter.remove();
                return true;
            }
        }
        return false;
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
        cfg.setStartOnLeaveLocation((Boolean) map.get(START_ON_MOVING_FIELD));
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
        final List<Long> locFrom = new LinkedList<>();
        final List<Long> locTo = new LinkedList<>();
        final List<Long> locInterim = new LinkedList<>();

        getLocationIds(t, locFrom, locTo, locInterim);

        //get locations
        final Set<Long> locationIds = new HashSet<>();
        locationIds.addAll(locFrom);
        locationIds.addAll(locTo);
        locationIds.addAll(locInterim);

        final List<LocationProfile> locations = locationProfileDao.findAll(locationIds);

        addLocations(t.getShippedFrom(), locations, locFrom);
        addLocations(t.getShippedTo(), locations, locTo);
        addLocations(t.getInterimStops(), locations, locInterim);
    }

    /**
     * @param t
     * @param locFrom
     * @param locTo
     * @param locInterim TODO
     */
    protected void getLocationIds(final AutoStartShipment t,
            final List<Long> locFrom, final List<Long> locTo, final List<Long> locInterim) {
        final Map<String, Object> params = new HashMap<>();
        params.put("cfg", t.getId());

        //get location ID's
        final List<Map<String, Object>> rows = jdbc.queryForList("select * from "
                + LOCATION_REL_TABLE + " where " + LOCATION_CONFIG + " = :cfg", params);

        final Map<Long, Integer> fromOrders = new HashMap<>();
        final Map<Long, Integer> toOrders = new HashMap<>();
        final Map<Long, Integer> interimOrders = new HashMap<>();

        for (final Map<String,Object> map : rows) {
            final String direction = (String) map.get(LOCATION_DIRECTION);
            final Long locationId = ((Number) map.get(LOCATION_LOCATION)).longValue();
            if ("from".equals(direction)) {
                locFrom.add(locationId);
                fromOrders.put(locationId, ((Number) map.get(LOCATION_ORDER)).intValue());
            } else if ("to".equals(direction)) {
                locTo.add(locationId);
                toOrders.put(locationId, ((Number) map.get(LOCATION_ORDER)).intValue());
            } else if ("interim".equals(direction)) {
                locInterim.add(locationId);
                interimOrders.put(locationId, ((Number) map.get(LOCATION_ORDER)).intValue());
            } else {
                throw new IllegalArgumentException("Undefined location direction " + direction);
            }
        }

        sortLocations(locFrom, fromOrders);
        sortLocations(locTo, toOrders);
        sortLocations(locInterim, interimOrders);
    }
    /**
     * @param target target list of location.
     * @param source source list of location.
     * @param ids set of location ID to copy.
     */
    private void addLocations(final List<LocationProfile> target,
            final List<LocationProfile> source, final List<Long> ids) {
        final Map<Long, Integer> orders = new HashMap<>();
        int order = 0;
        for (final Long id : ids) {
            orders.put(id, order);
            order++;
        }

        for (final LocationProfile l : source) {
            if (orders.containsKey(l.getId())) {
                target.add(l);
            }
        }

        //sort target list
        Collections.sort(target, new Comparator<LocationProfile>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final LocationProfile o1, final LocationProfile o2) {
                return orders.get(o1.getId()).compareTo(orders.get(o2.getId()));
            }
        });
    }
    /**
     * @param locs
     * @param orders
     */
    private void sortLocations(final List<Long> locs, final Map<Long, Integer> orders) {
        Collections.sort(locs, new Comparator<Long>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final Long o1, final Long o2) {
                return orders.get(o1).compareTo(orders.get(o2));
            }
        });
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
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#delete(com.visfresh.entities.EntityWithId)
     */
    @Override
    public void delete(final AutoStartShipment entity) {
        //delete template, entity should be deleted by DB trigger
        shipmentTemplateDao.delete(entity.getTemplate().getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createSelectAllSupport()
     */
    @Override
    public SelectAllSupport createSelectAllSupport() {
        return new SelectAllSupport(getTableName()){
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.SelectAllSupport#buildSelectBlockForFindAll(com.visfresh.dao.Filter)
             */
            @Override
            protected String buildSelectBlockForFindAll(final Filter filter) {
                return "select "
                        //first group for sorting
                        + "lfrom.locationName as " + AutoStartShipmentConstants.START_LOCATIONS + ","
                        + "lto.locationName as " + AutoStartShipmentConstants.END_LOCATIONS + ","
                        + "ins.locationName as " + AutoStartShipmentConstants.INTERIM_STOPS + ","
                        + "lto.locationName as " + AutoStartShipmentConstants.END_LOCATIONS + ","
                        + "ap.alertProfileName as " + AutoStartShipmentConstants.ALERT_PROFILE_NAME + ",\n"
                        + "tpl.shipmentTemplateName as " + ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME + ",\n"
                        + "tpl.shipmentDescription as " + ShipmentTemplateConstants.SHIPMENT_DESCRIPTION + ",\n"
                        //for build entity
                        + getTableName() + ".*\n"
                        + "from " + getTableName() + "\n"
                        //shipment template name and description from shipments table
                        + "join (select "
                        + ShipmentTemplateDaoImpl.ALERT_PROFILE_FIELD + " as alertProfileId,\n"
                        + ShipmentTemplateDaoImpl.ID_FIELD + " as shipmentTemplateId,\n"
                        + ShipmentTemplateDaoImpl.NAME_FIELD + " as shipmentTemplateName,\n"
                        + ShipmentTemplateDaoImpl.DESCRIPTION_FIELD + " as shipmentDescription\n"
                        + "from " + ShipmentTemplateDaoImpl.TABLE
                        + ") tpl on tpl.shipmentTemplateId = " + TABLE + "." + TEMPLATE_FIELD + "\n"
                        //alert profile name from alert profiles table
                        + "left outer join (select "
                        + AlertProfileDaoImpl.ID_FIELD + " as alertProfileId,\n"
                        + AlertProfileDaoImpl.NAME_FIELD + " as alertProfileName\n"
                        + "from " + AlertProfileDaoImpl.TABLE
                        + ") ap on ap.alertProfileId = tpl.alertProfileId\n"
                        //location from
                        + "left outer join (select "
                        + "lr." + LOCATION_CONFIG + " as locationConfig,\n"
                        + "min(loc." + LocationProfileDaoImpl.NAME_FIELD + ") as locationName\n"
                        + "from " + LOCATION_REL_TABLE + " lr\n"
                        + "join " + LocationProfileDaoImpl.TABLE + " loc\n"
                        + "on loc." + LocationProfileDaoImpl.ID_FIELD + "=lr." + LOCATION_LOCATION + "\n"
                        + "where lr." + LOCATION_DIRECTION  + " = 'from'\n"
                        + "group by lr." + LOCATION_CONFIG
                        + ") lfrom on lfrom.locationConfig = " + TABLE + "." + ID_FIELD + "\n"
                        //location to
                        + "left outer join (select "
                        + "lr." + LOCATION_CONFIG + " as locationConfig,\n"
                        + "min(loc." + LocationProfileDaoImpl.NAME_FIELD + ") as locationName\n"
                        + "from " + LOCATION_REL_TABLE + " lr\n"
                        + "join " + LocationProfileDaoImpl.TABLE + " loc\n"
                        + "on loc." + LocationProfileDaoImpl.ID_FIELD + "=lr." + LOCATION_LOCATION + "\n"
                        + "where lr." + LOCATION_DIRECTION  + " = 'to'\n"
                        + "group by lr." + LOCATION_CONFIG
                        + ") lto on lto.locationConfig = " + TABLE + "." + ID_FIELD + "\n"
                        //location interim
                        + "left outer join (select "
                        + "lr." + LOCATION_CONFIG + " as locationConfig,\n"
                        + "min(loc." + LocationProfileDaoImpl.NAME_FIELD + ") as locationName\n"
                        + "from " + LOCATION_REL_TABLE + " lr\n"
                        + "join " + LocationProfileDaoImpl.TABLE + " loc\n"
                        + "on loc." + LocationProfileDaoImpl.ID_FIELD + "=lr." + LOCATION_LOCATION + "\n"
                        + "where lr." + LOCATION_DIRECTION  + " = 'interim'\n"
                        + "group by lr." + LOCATION_CONFIG
                        + ") ins on ins.locationConfig = " + TABLE + "." + ID_FIELD + "\n"
                        ;
            }
        };
    }
}

