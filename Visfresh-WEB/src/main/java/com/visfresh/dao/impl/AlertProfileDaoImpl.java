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
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TemperatureRule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertProfileDaoImpl extends EntityWithCompanyDaoImplBase<AlertProfile, Long> implements AlertProfileDao {
    public static final String TABLE = "alertprofiles";

    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    private static final String DESCRIPTION_FIELD = "description";

    private static final String ONENTERBRIGHT_FIELD = "onenterbright";
    private static final String ONENTERDARK_FIELD = "onenterdark";
    private static final String ONMOVEMENTSTART_FIELD = "onmovementstart";
    private static final String ONMOVEMENTSTOP_FIELD = "onmovementstop";
    private static final String ONBATTERYLOW_FIELD = "onbatterylow";
    private static final String UPPERTEMPLIMIT_FIELD = "uppertemplimit";
    private static final String LOWERTEMPLIMIT_FIELD = "lowertemplimit";
    private static final String COMPANY_FIELD = "company";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

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
    public <S extends AlertProfile> S save(final S ap) {
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
     * @param issues temperature issues.
     */
    private void updateTemperatureIssues(final Long id, final List<TemperatureRule> issues) {
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
        for (final TemperatureRule issue : issues) {
            final Long issueId = issue.getId();
            final Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("temperature", issue.getTemperature());
            paramMap.put("type", issue.getType().toString());
            paramMap.put("timeOut", issue.getTimeOutMinutes());
            paramMap.put("cumulative", issue.isCumulativeFlag());
            paramMap.put("apid", id);

            if (issueId != null) {
                paramMap.put("id", issueId);
                actual.remove(issueId);

                jdbc.update("update temperaturerules set"
                        + " type = :type,"
                        + " temp = :temperature,"
                        + " timeout = :timeOut,"
                        + " cumulative = :cumulative"
                        + " where id = :id", paramMap);
            } else {
                final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
                jdbc.update("insert into temperaturerules"
                        + "(type, temp, timeout, cumulative, alertprofile)"
                        + " values(:type, :temperature, :timeOut, :cumulative, :apid)",
                        new MapSqlParameterSource(paramMap), keyHolder);
                if (keyHolder.getKey() != null) {
                    issue.setId(keyHolder.getKey().longValue());
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

    /**
     * @param id alert profile ID.
     * @return list of temperature issues.
     */
    private List<TemperatureRule> loadTemperatureIssues(final Long id) {
        final List<TemperatureRule> list = new LinkedList<TemperatureRule>();

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("alertprofile", id);
        final List<Map<String, Object>> rows = jdbc.queryForList("select * from temperaturerules"
                + " where alertprofile = :alertprofile", paramMap);

        //create temperature issues from DB rows
        for (final Map<String, Object> row : rows) {
            final TemperatureRule issue = new TemperatureRule();
            issue.setId(((Number) row.get("id")).longValue());
            issue.setTemperature(((Number) row.get("temp")).doubleValue());
            issue.setCumulativeFlag(Boolean.TRUE.equals(row.get("cumulative")));
            issue.setTimeOutMinutes(((Number) row.get("timeout")).intValue());
            issue.setType(AlertType.valueOf((String) row.get("type")));

            list.add(issue);
        }

        return list;
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
    protected AlertProfile createEntity(final Map<String, Object> map) {
        final AlertProfile ap = new AlertProfile();

        ap.setId(((Number) map.get(ID_FIELD)).longValue());

        ap.setName((String) map.get(NAME_FIELD));
        ap.setDescription((String) map.get(DESCRIPTION_FIELD));

        ap.getAlertRules().addAll(loadTemperatureIssues(ap.getId()));

        ap.setWatchEnterBrightEnvironment((Boolean) map.get(ONENTERBRIGHT_FIELD));
        ap.setWatchEnterDarkEnvironment((Boolean) map.get(ONENTERDARK_FIELD));
        ap.setWatchMovementStart((Boolean) map.get(ONMOVEMENTSTART_FIELD));
        ap.setWatchMovementStop((Boolean) map.get(ONMOVEMENTSTOP_FIELD));
        ap.setWatchBatteryLow((Boolean) map.get(ONBATTERYLOW_FIELD));
        ap.setLowerTemperatureLimit(((Number) map.get(LOWERTEMPLIMIT_FIELD)).doubleValue());
        ap.setUpperTemperatureLimit(((Number) map.get(UPPERTEMPLIMIT_FIELD)).doubleValue());
        return ap;
    }
}
