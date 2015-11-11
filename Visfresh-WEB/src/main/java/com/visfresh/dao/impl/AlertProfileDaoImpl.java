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

import com.visfresh.constants.AlertProfileConstants;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TemperatureIssue;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertProfileDaoImpl extends EntityWithCompanyDaoImplBase<AlertProfile, Long> implements AlertProfileDao {
    public static final String TABLE = "alertprofiles";

    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    public static final String DESCRIPTION_FIELD = "description";

    public static final String LOWTEMP_FIELD = "lowtemp";
    public static final String LOWTEMPFORMORETHEN_FIELD = "lowtempformorethen";
    public static final String LOWTEMP_FIELD_2 = "lowtemp2";
    public static final String LOWTEMPFORMORETHEN_FIELD_2 = "lowtempformorethen2";

    public static final String CRITICALLOWTEMP_FIELD = "criticallowtem";
    public static final String CRITICALLOWTEMPFORMORETHEN_FIELD = "criticallowtempformorethen";
    public static final String CRITICALLOWTEMP_FIELD_2 = "criticallowtem2";
    public static final String CRITICALLOWTEMPFORMORETHEN_FIELD_2 = "criticallowtempformorethen2";

    public static final String HIGHTEMP_FIELD = "hightemp";
    public static final String HIGHTEMPFORMORETHEN_FIELD = "hightempformorethen";
    public static final String HIGHTEMP_FIELD_2 = "hightemp2";
    public static final String HIGHTEMPFORMORETHEN_FIELD_2 = "hightempformorethen2";

    public static final String CRITICALHIGHTEMP_FIELD = "criticalhightemp";
    public static final String CRITICALHIGHTEMPFORMORETHEN_FIELD = "criticalhightempformorethen";
    public static final String CRITICALHIGHTEMP_FIELD_2 = "criticalhightemp2";
    public static final String CRITICALHIGHTEMPFORMORETHEN_FIELD_2 = "criticalhightempformorethen2";

    public static final String ONENTERBRIGHT_FIELD = "onenterbright";
    public static final String ONENTERDARK_FIELD = "onenterdark";
    public static final String ONMOVEMENTSTART_FIELD = "onmovementstart";
    public static final String ONMOVEMENTSTOP_FIELD = "onmovementstop";
    public static final String ONBATTERYLOW_FIELD = "onbatterylow";
    public static final String COMPANY_FIELD = "company";

    @Autowired
    private CompanyDao companyDao;
    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public AlertProfileDaoImpl() {
        super();

        //build property to field map
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_STOP, ONMOVEMENTSTOP_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_START, ONMOVEMENTSTART_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_WATCH_ENTER_DARK_ENVIRONMENT, ONENTERDARK_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_WATCH_ENTER_BRIGHT_ENVIRONMENT, ONENTERBRIGHT_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_WATCH_BATTERY_LOW, ONBATTERYLOW_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_CRITICAL_LOW_TEMPERATURE_MINUTES2,
                CRITICALHIGHTEMPFORMORETHEN_FIELD_2);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_CRITICAL_LOW_TEMPERATURE2, CRITICALLOWTEMP_FIELD_2);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_CRITICAL_LOW_TEMPERATURE_MINUTES,
                CRITICALLOWTEMPFORMORETHEN_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_CRITICAL_LOW_TEMPERATURE,
                CRITICALLOWTEMP_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_LOW_TEMPERATURE_MINUTES2,
                LOWTEMPFORMORETHEN_FIELD_2);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_LOW_TEMPERATURE2,
                LOWTEMP_FIELD_2);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_LOW_TEMPERATURE_MINUTES,
                LOWTEMPFORMORETHEN_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_LOW_TEMPERATURE,
                LOWTEMP_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_CRITICAL_HIGH_TEMPERATURE_MINUTES2,
                CRITICALHIGHTEMPFORMORETHEN_FIELD_2);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_CRITICAL_HIGH_TEMPERATURE2,
                CRITICALHIGHTEMP_FIELD_2);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_CRITICAL_HIGH_TEMPERATURE_MINUTES,
            CRITICALHIGHTEMPFORMORETHEN_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_CRITICAL_HIGH_TEMPERATURE,
                CRITICALHIGHTEMP_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_HIGH_TEMPERATURE_MINUTES2,
                HIGHTEMPFORMORETHEN_FIELD_2);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_HIGH_TEMPERATURE2,
                HIGHTEMP_FIELD_2);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_HIGH_TEMPERATURE_MINUTES,
                HIGHTEMPFORMORETHEN_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_HIGH_TEMPERATURE,
                HIGHTEMP_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_ALERT_PROFILE_DESCRIPTION,
                DESCRIPTION_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_ALERT_PROFILE_NAME,
                NAME_FIELD);
        propertyToDbFields.put(
                AlertProfileConstants.PROPERTY_ALERT_PROFILE_ID,
                ID_FIELD);
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

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            ap.setId(keyHolder.getKey().longValue());
        }

        updateTemperatureIssues(ap.getId(), ap.getTemperatureIssues());
        return ap;
    }
    /**
     * @param id alert profile ID.
     * @param issues temperature issues.
     */
    private void updateTemperatureIssues(final Long id, final List<TemperatureIssue> issues) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("apid", id);

        //get old issues
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select id from allerttemperatures where alertprofile = :apid", params);
        final Set<Long> actual = new HashSet<Long>();
        for (final Map<String,Object> row : list) {
            actual.add(((Number) row.get("id")).longValue());
        }

        //process issues
        for (final TemperatureIssue issue : issues) {
            final Long issueId = issue.getId();
            final Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("temperature", issue.getTemperature());
            paramMap.put("type", issue.getType().toString());
            paramMap.put("timeOut", issue.getTimeOutMinutes());
            paramMap.put("apid", id);

            if (issueId != null) {
                paramMap.put("id", issueId);
                actual.remove(issueId);

                jdbc.update("update allerttemperatures set"
                        + " type = :type,"
                        + " temp = :temperature,"
                        + " timeout = :timeOut"
                        + " where id = :id", paramMap);
            } else {
                final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
                jdbc.update("insert into allerttemperatures"
                        + "(type, temp, timeout, alertprofile)"
                        + " values(:type, :temperature, :timeOut, :apid)",
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
            jdbc.update("delete from allerttemperatures where id = :id and alertprofile = :alertprofile",
                    paramMap);
        }
    }

    /**
     * @param id alert profile ID.
     * @return list of temperature issues.
     */
    private List<TemperatureIssue> loadTemperatureIssues(final Long id) {
        final List<TemperatureIssue> list = new LinkedList<TemperatureIssue>();

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("alertprofile", id);
        final List<Map<String, Object>> rows = jdbc.queryForList("select * from allerttemperatures"
                + " where alertprofile = :alertprofile", paramMap);

        //create temperature issues from DB rows
        for (final Map<String, Object> row : rows) {
            final TemperatureIssue issue = new TemperatureIssue();
            issue.setId(((Number) row.get("id")).longValue());
            issue.setTemperature(((Number) row.get("temp")).doubleValue());
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
        final AlertProfile no = new AlertProfile();

        no.setId(((Number) map.get(ID_FIELD)).longValue());

        no.setName((String) map.get(NAME_FIELD));
        no.setDescription((String) map.get(DESCRIPTION_FIELD));

        no.getTemperatureIssues().addAll(loadTemperatureIssues(no.getId()));

        no.setWatchEnterBrightEnvironment((Boolean) map.get(ONENTERBRIGHT_FIELD));
        no.setWatchEnterDarkEnvironment((Boolean) map.get(ONENTERDARK_FIELD));
        no.setWatchMovementStart((Boolean) map.get(ONMOVEMENTSTART_FIELD));
        no.setWatchMovementStop((Boolean) map.get(ONMOVEMENTSTOP_FIELD));
        no.setWatchBatteryLow((Boolean) map.get(ONBATTERYLOW_FIELD));
        return no;
    }
}
