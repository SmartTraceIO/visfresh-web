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

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.Filter;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.CorrectiveActionList;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.io.json.CorrectiveActionListSerializer;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertProfileDaoImpl extends EntityWithCompanyDaoImplBase<AlertProfile, AlertProfile, Long> implements AlertProfileDao {
    public static final String TABLE = "alertprofiles";

    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    private static final String DESCRIPTION_FIELD = "description";

    private static final String ONENTERBRIGHT_FIELD = "onenterbright";
    private static final String ONENTERDARK_FIELD = "onenterdark";
    private static final String ONMOVEMENTSTART_FIELD = "onmovementstart";
    private static final String ONMOVEMENTSTOP_FIELD = "onmovementstop";
    private static final String ONBATTERYLOW_FIELD = "onbatterylow";
    protected static final String UPPERTEMPLIMIT_FIELD = "uppertemplimit";
    protected static final String LOWERTEMPLIMIT_FIELD = "lowertemplimit";
    private static final String COMPANY_FIELD = "company";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();
    private CorrectiveActionListSerializer actionsSerializer = new CorrectiveActionListSerializer(null);

    /**
     * Default constructor.
     */
    public AlertProfileDaoImpl() {
        super();

        //build property to field map
        propertyToDbFields.put(AlertProfileConstants.WATCH_MOVEMENT_STOP, ONMOVEMENTSTOP_FIELD);
        propertyToDbFields.put(AlertProfileConstants.WATCH_MOVEMENT_START, ONMOVEMENTSTART_FIELD);
        propertyToDbFields.put(AlertProfileConstants.WATCH_ENTER_DARK_ENVIRONMENT, ONENTERDARK_FIELD);
        propertyToDbFields.put(AlertProfileConstants.WATCH_ENTER_BRIGHT_ENVIRONMENT, ONENTERBRIGHT_FIELD);
        propertyToDbFields.put(AlertProfileConstants.WATCH_BATTERY_LOW, ONBATTERYLOW_FIELD);
        propertyToDbFields.put(AlertProfileConstants.ALERT_PROFILE_DESCRIPTION, DESCRIPTION_FIELD);
        propertyToDbFields.put(AlertProfileConstants.ALERT_PROFILE_NAME, NAME_FIELD);
        propertyToDbFields.put(AlertProfileConstants.ALERT_PROFILE_ID, ID_FIELD);
        propertyToDbFields.put(AlertProfileConstants.LOWER_TEMPERATURE_LIMIT, LOWERTEMPLIMIT_FIELD);
        propertyToDbFields.put(AlertProfileConstants.UPPER_TEMPERATURE_LIMIT, UPPERTEMPLIMIT_FIELD);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends AlertProfile> S saveImpl(final S ap) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        final List<String> fields = getFields(false);

        String sql;
        if (ap.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        paramMap.put(ID_FIELD, ap.getId());
        paramMap.put(NAME_FIELD, ap.getName());
        paramMap.put(DESCRIPTION_FIELD, ap.getDescription());

        paramMap.put(ONENTERBRIGHT_FIELD, ap.isWatchEnterBrightEnvironment());
        paramMap.put(ONENTERDARK_FIELD, ap.isWatchEnterDarkEnvironment());
        paramMap.put(ONMOVEMENTSTART_FIELD, ap.isWatchMovementStart());
        paramMap.put(ONMOVEMENTSTOP_FIELD, ap.isWatchMovementStop());
        paramMap.put(ONBATTERYLOW_FIELD, ap.isWatchBatteryLow());
        paramMap.put(COMPANY_FIELD, ap.getCompany().getId());
        paramMap.put(LOWERTEMPLIMIT_FIELD, ap.getLowerTemperatureLimit());
        paramMap.put(UPPERTEMPLIMIT_FIELD, ap.getUpperTemperatureLimit());

        paramMap.put("batterylowactions", ap.getBatteryLowCorrectiveActions() == null
                ? null : ap.getBatteryLowCorrectiveActions().getId());
        paramMap.put("lightonactions", ap.getLightOnCorrectiveActions() == null
                ? null : ap.getLightOnCorrectiveActions().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            ap.setId(keyHolder.getKey().longValue());
        }

        updateTemperatureIssues(ap.getId(), ap.getAlertRules());
        return ap;
    }
    /**
     * @param id alert profile ID.
     * @param rules temperature rules.
     */
    private void updateTemperatureIssues(final Long id, final List<TemperatureRule> rules) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("apid", id);

        //get old issues
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select id from temperaturerules where alertprofile = :apid", params);
        final Set<Long> actual = new HashSet<Long>();
        for (final Map<String,Object> row : list) {
            actual.add(((Number) row.get("id")).longValue());
        }

        //process issues
        for (final TemperatureRule rule : rules) {
            final Long issueId = rule.getId();
            final Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("temperature", rule.getTemperature());
            paramMap.put("type", rule.getType().toString());
            paramMap.put("timeOut", rule.getTimeOutMinutes());
            paramMap.put("cumulative", rule.isCumulativeFlag());
            paramMap.put("maxrateminutes", rule.getMaxRateMinutes());
            paramMap.put("corractions", rule.getCorrectiveActions() == null
                    ? null : rule.getCorrectiveActions().getId());
            paramMap.put("apid", id);

            if (issueId != null) {
                paramMap.put("id", issueId);
                actual.remove(issueId);

                jdbc.update("update temperaturerules set"
                        + " type = :type,"
                        + " temp = :temperature,"
                        + " timeout = :timeOut,"
                        + " cumulative = :cumulative,"
                        + " maxrateminutes = :maxrateminutes,"
                        + " corractions = :corractions"
                        + " where id = :id", paramMap);
            } else {
                final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
                jdbc.update("insert into temperaturerules"
                        + "(type, temp, timeout, cumulative, maxrateminutes, alertprofile, corractions)"
                        + " values(:type, :temperature, :timeOut, :cumulative, :maxrateminutes, :apid, :corractions)",
                        new MapSqlParameterSource(paramMap), keyHolder);
                if (keyHolder.getKey() != null) {
                    rule.setId(keyHolder.getKey().longValue());
                }
            }
        }

        //delete old
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("alertprofile", id);

        for (final Long issueId : actual) {
            paramMap.put("id", issueId);
            jdbc.update("delete from temperaturerules where id = :id and alertprofile = :alertprofile",
                    paramMap);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final AlertProfile t, final Map<String, Object> row, final Map<String, Object> cache) {
        super.resolveReferences(t, row, cache);
        t.getAlertRules().addAll(loadTemperatureIssues(t.getId()));

        //update company for corrective actions
        for (final TemperatureRule r: t.getAlertRules()) {
            if (r.getCorrectiveActions() != null) {
                r.getCorrectiveActions().setCompany(t.getCompany());
            }
        }
        if (t.getBatteryLowCorrectiveActions() != null) {
            t.getBatteryLowCorrectiveActions().setCompany(t.getCompany());
        }
        if (t.getLightOnCorrectiveActions() != null) {
            t.getLightOnCorrectiveActions().setCompany(t.getCompany());
        }
    }

    /**
     * @param id alert profile ID.
     * @return list of temperature issues.
     */
    private List<TemperatureRule> loadTemperatureIssues(final Long id) {
        final List<TemperatureRule> list = new LinkedList<TemperatureRule>();

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("alertprofile", id);
        final List<Map<String, Object>> rows = jdbc.queryForList("select r.*"
                + ""
                + ",\n ca.id as actionsId"
                + ",\n ca.name as actionsName"
                + ",\n ca.description as actionsDescription"
                + ",\n ca.actions as actionsActions"
                + " from temperaturerules r"
                + "\nleft outer join correctiveactions ca on ca.id = r.corractions"
                + " where r.alertprofile = :alertprofile", paramMap);

        //create temperature issues from DB rows
        for (final Map<String, Object> row : rows) {
            final TemperatureRule r = new TemperatureRule();
            r.setId(((Number) row.get("id")).longValue());
            r.setTemperature(((Number) row.get("temp")).doubleValue());
            r.setCumulativeFlag(Boolean.TRUE.equals(row.get("cumulative")));
            r.setTimeOutMinutes(((Number) row.get("timeout")).intValue());
            r.setType(AlertType.valueOf((String) row.get("type")));
            final Number maxRateMinutes = (Number) row.get("maxrateminutes");
            if (maxRateMinutes != null) {
                r.setMaxRateMinutes(maxRateMinutes.intValue());
            }

            final Number listId = (Number) row.get("actionsId");
            if (listId != null) {
                final CorrectiveActionList a = new CorrectiveActionList();
                a.setId(listId.longValue());
                a.setName((String) row.get("actionsName"));
                a.setDescription((String) row.get("actionsDescription"));
                a.getActions().addAll(parseCorrectiveActions((String) row.get("actionsActions")));
                r.setCorrectiveActions(a);
            }

            list.add(r);
        }

        return list;
    }
    /**
     * @param actions
     * @return
     */
    private List<CorrectiveAction> parseCorrectiveActions(final String actions) {
        return actionsSerializer.parseActions(SerializerUtils.parseJson(actions).getAsJsonArray());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createSelectAllSupport()
     */
    @Override
    protected SelectAllSupport createSelectAllSupport() {
        return new SelectAllSupport(getTableName()) {
            /* (non-Javadoc)
             * @see com.visfresh.dao.impl.SelectAllSupport#buildSelectBlockForFindAll(com.visfresh.dao.Filter)
             */
            @Override
            protected String buildSelectBlockForFindAll(final Filter filter) {
                final String table = getTableName();
                return "select " + table + ".*"
                        + ",\n loa.id as loaActionsId"
                        + ",\n loa.name as loaActionsName"
                        + ",\n loa.description as loaActionsDescription"
                        + ",\n loa.actions as loaActionsActions"
                        + ",\n bla.id as blaActionsId"
                        + ",\n bla.name as blaActionsName"
                        + ",\n bla.description as blaActionsDescription"
                        + ",\n bla.actions as blaActionsActions"
                        + " from " + table
                        + "\nleft outer join correctiveactions bla on bla.id = " + table + ".batterylowactions"
                        + "\nleft outer join correctiveactions loa on loa.id = " + table + ".lightonactions"
                        ;
            }
        };
    }
    private List<String> getFields(final boolean includeId) {
        final List<String> fields = new LinkedList<String>();
        fields.add(NAME_FIELD);
        fields.add(DESCRIPTION_FIELD);

        fields.add(ONENTERBRIGHT_FIELD);
        fields.add(ONENTERDARK_FIELD);
        fields.add(ONMOVEMENTSTART_FIELD);
        fields.add(ONMOVEMENTSTOP_FIELD);
        fields.add(ONBATTERYLOW_FIELD);
        fields.add(COMPANY_FIELD);
        fields.add(LOWERTEMPLIMIT_FIELD);
        fields.add(UPPERTEMPLIMIT_FIELD);
        fields.add("batterylowactions");
        fields.add("lightonactions");

        if (includeId) {
            fields.add(ID_FIELD);
        }
        return fields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID_FIELD;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbFields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#getCompanyFieldName()
     */
    @Override
    protected String getCompanyFieldName() {
        return COMPANY_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected AlertProfile createEntity(final Map<String, Object> row) {
        final AlertProfile ap = new AlertProfile();

        ap.setId(((Number) row.get(ID_FIELD)).longValue());

        ap.setName((String) row.get(NAME_FIELD));
        ap.setDescription((String) row.get(DESCRIPTION_FIELD));
        ap.setWatchEnterBrightEnvironment((Boolean) row.get(ONENTERBRIGHT_FIELD));
        ap.setWatchEnterDarkEnvironment((Boolean) row.get(ONENTERDARK_FIELD));
        ap.setWatchMovementStart((Boolean) row.get(ONMOVEMENTSTART_FIELD));
        ap.setWatchMovementStop((Boolean) row.get(ONMOVEMENTSTOP_FIELD));
        ap.setWatchBatteryLow((Boolean) row.get(ONBATTERYLOW_FIELD));
        ap.setLowerTemperatureLimit(((Number) row.get(LOWERTEMPLIMIT_FIELD)).doubleValue());
        ap.setUpperTemperatureLimit(((Number) row.get(UPPERTEMPLIMIT_FIELD)).doubleValue());

        //light on corrective actions
        if (row.get("loaActionsId") != null) {
            final CorrectiveActionList l = new CorrectiveActionList();
            l.setId(((Number) row.get("loaActionsId")).longValue());
            l.setName((String) row.get("loaActionsName"));
            l.setDescription((String) row.get("loaActionsDescription"));
            l.getActions().addAll(parseCorrectiveActions((String) row.get("loaActionsActions")));
            ap.setLightOnCorrectiveActions(l);
        }

        //battery low corrective actions
        if (row.get("blaActionsId") != null) {
            final CorrectiveActionList l = new CorrectiveActionList();
            l.setId(((Number) row.get("blaActionsId")).longValue());
            l.setName((String) row.get("blaActionsName"));
            l.setDescription((String) row.get("blaActionsDescription"));
            l.getActions().addAll(parseCorrectiveActions((String) row.get("blaActionsActions")));
            ap.setBatteryLowCorrectiveActions(l);
        }

        return ap;
    }
}
