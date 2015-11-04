/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertProfileDaoImpl extends DaoImplBase<AlertProfile, Long> implements AlertProfileDao {
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

    private static final String ID_PLACEHOLDER = "32_497803_29475";

    @Autowired
    private CompanyDao companyDao;

    /**
     * Default constructor.
     */
    public AlertProfileDaoImpl() {
        super();
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

        paramMap.put(LOWTEMP_FIELD, ap.getLowTemperature());
        paramMap.put(LOWTEMPFORMORETHEN_FIELD, ap.getLowTemperatureForMoreThen());
        paramMap.put(LOWTEMP_FIELD_2, ap.getLowTemperature2());
        paramMap.put(LOWTEMPFORMORETHEN_FIELD_2, ap.getLowTemperatureForMoreThen2());

        paramMap.put(CRITICALLOWTEMP_FIELD, ap.getCriticalLowTemperature());
        paramMap.put(CRITICALLOWTEMPFORMORETHEN_FIELD, ap.getCriticalLowTemperatureForMoreThen());
        paramMap.put(CRITICALLOWTEMP_FIELD_2, ap.getCriticalLowTemperature2());
        paramMap.put(CRITICALLOWTEMPFORMORETHEN_FIELD_2, ap.getCriticalLowTemperatureForMoreThen2());

        paramMap.put(HIGHTEMP_FIELD, ap.getHighTemperature());
        paramMap.put(HIGHTEMPFORMORETHEN_FIELD, ap.getHighTemperatureForMoreThen());
        paramMap.put(HIGHTEMP_FIELD_2, ap.getHighTemperature2());
        paramMap.put(HIGHTEMPFORMORETHEN_FIELD_2, ap.getHighTemperatureForMoreThen2());

        paramMap.put(CRITICALHIGHTEMP_FIELD, ap.getCriticalHighTemperature());
        paramMap.put(CRITICALHIGHTEMPFORMORETHEN_FIELD, ap.getCriticalHighTemperatureForMoreThen());
        paramMap.put(CRITICALHIGHTEMP_FIELD_2, ap.getCriticalHighTemperature2());
        paramMap.put(CRITICALHIGHTEMPFORMORETHEN_FIELD_2, ap.getCriticalHighTemperatureForMoreThen2());

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

        return ap;
    }
    private List<String> getFields(final boolean includeId) {
        final List<String> fields = new LinkedList<String>();
        fields.add(NAME_FIELD);
        fields.add(DESCRIPTION_FIELD);

        fields.add(LOWTEMP_FIELD);
        fields.add(LOWTEMPFORMORETHEN_FIELD);
        fields.add(LOWTEMP_FIELD_2);
        fields.add(LOWTEMPFORMORETHEN_FIELD_2);

        fields.add(CRITICALLOWTEMP_FIELD);
        fields.add(CRITICALLOWTEMPFORMORETHEN_FIELD);
        fields.add(CRITICALLOWTEMP_FIELD_2);
        fields.add(CRITICALLOWTEMPFORMORETHEN_FIELD_2);

        fields.add(HIGHTEMP_FIELD);
        fields.add(HIGHTEMPFORMORETHEN_FIELD);
        fields.add(HIGHTEMP_FIELD_2);
        fields.add(HIGHTEMPFORMORETHEN_FIELD_2);

        fields.add(CRITICALHIGHTEMP_FIELD);
        fields.add(CRITICALHIGHTEMPFORMORETHEN_FIELD);
        fields.add(CRITICALHIGHTEMP_FIELD_2);
        fields.add(CRITICALHIGHTEMPFORMORETHEN_FIELD_2);

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
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public AlertProfile findOne(final Long id) {
        if (id == null) {
            return null;
        }

        final List<Map<String, Object>> list = runSelectScript(id);
        return list.size() == 0 ? null : createAlertProfile(list.get(0), new HashMap<Long, Company>());
    }
    /**
     * @param id
     * @return
     */
    private List<Map<String, Object>> runSelectScript(final Long id) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_PLACEHOLDER, id);

        final Map<String, String> fields = createSelectAsMapping();

        params.putAll(fields);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + " from "
                + TABLE
                + (id == null ? "" : " where " + ID_FIELD + " = :" + ID_PLACEHOLDER)
                + " order by " + ID_FIELD,
                params);
        return list;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<AlertProfile> findAll() {
        final List<Map<String, Object>> list = runSelectScript(null);

        final Map<Long, Company> userCache = new HashMap<Long, Company>();
        final List<AlertProfile> result = new LinkedList<AlertProfile>();
        for (final Map<String,Object> map : list) {
            result.add(createAlertProfile(map, userCache));
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.AlertProfileDao#findByCompany(com.visfresh.entities.Company)
     */
    @Override
    public List<AlertProfile> findByCompany(final Company company) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("company", company.getId());
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from "
                + TABLE
                + " where " + COMPANY_FIELD + " = :company",
                params);

        final List<AlertProfile> result = new LinkedList<AlertProfile>();

        final Map<Long, Company> companyCache = new HashMap<Long, Company>();
        companyCache.put(company.getId(), company);
        for (final Map<String, Object> row : rows) {
            result.add(createAlertProfile(row, companyCache));
        }
        return result;
    }
    /**
     * @param map
     * @return
     */
    private AlertProfile createAlertProfile(final Map<String, Object> map,
            final Map<Long, Company> cache) {
        final AlertProfile no = new AlertProfile();

        no.setId(((Number) map.get(ID_FIELD)).longValue());

        no.setName((String) map.get(NAME_FIELD));
        no.setDescription((String) map.get(DESCRIPTION_FIELD));

        no.setLowTemperature(((Number) map.get(LOWTEMP_FIELD)).doubleValue());
        no.setLowTemperatureForMoreThen(((Number) map.get(LOWTEMPFORMORETHEN_FIELD)).intValue());
        if (map.get(LOWTEMP_FIELD_2) != null) {
            no.setLowTemperature2(((Number) map.get(LOWTEMP_FIELD_2)).doubleValue());
        }
        if (map.get(LOWTEMPFORMORETHEN_FIELD_2) != null) {
            no.setLowTemperatureForMoreThen2(((Number) map.get(LOWTEMPFORMORETHEN_FIELD_2)).intValue());
        }

        no.setCriticalLowTemperature(((Number) map.get(CRITICALLOWTEMP_FIELD)).doubleValue());
        no.setCriticalLowTemperatureForMoreThen(
                ((Number) map.get(CRITICALLOWTEMPFORMORETHEN_FIELD)).intValue());
        if (map.get(CRITICALLOWTEMP_FIELD_2) != null) {
            no.setCriticalLowTemperature2(((Number) map.get(CRITICALLOWTEMP_FIELD_2)).doubleValue());
        }
        if (map.get(CRITICALLOWTEMPFORMORETHEN_FIELD_2) != null) {
            no.setCriticalLowTemperatureForMoreThen2(
                    ((Number) map.get(CRITICALLOWTEMPFORMORETHEN_FIELD_2)).intValue());
        }

        no.setHighTemperature(((Number) map.get(HIGHTEMP_FIELD)).doubleValue());
        no.setHighTemperatureForMoreThen(((Number) map.get(HIGHTEMPFORMORETHEN_FIELD)).intValue());
        if (map.get(HIGHTEMP_FIELD_2) != null) {
            no.setHighTemperature2(((Number) map.get(HIGHTEMP_FIELD_2)).doubleValue());
        }
        if (map.get(HIGHTEMPFORMORETHEN_FIELD_2) != null) {
            no.setHighTemperatureForMoreThen2(((Number) map.get(HIGHTEMPFORMORETHEN_FIELD_2)).intValue());
        }

        no.setCriticalHighTemperature(((Number) map.get(CRITICALHIGHTEMP_FIELD)).doubleValue());
        no.setCriticalHighTemperatureForMoreThen(
                ((Number) map.get(CRITICALHIGHTEMPFORMORETHEN_FIELD)).intValue());
        if (map.get(CRITICALHIGHTEMP_FIELD_2) != null) {
            no.setCriticalHighTemperature2(((Number) map.get(CRITICALHIGHTEMP_FIELD_2)).doubleValue());
        }
        if (map.get(CRITICALHIGHTEMPFORMORETHEN_FIELD_2) != null) {
            no.setCriticalHighTemperatureForMoreThen2(
                    ((Number) map.get(CRITICALHIGHTEMPFORMORETHEN_FIELD_2)).intValue());
        }

        no.setWatchEnterBrightEnvironment((Boolean) map.get(ONENTERBRIGHT_FIELD));
        no.setWatchEnterDarkEnvironment((Boolean) map.get(ONENTERDARK_FIELD));
        no.setWatchMovementStart((Boolean) map.get(ONMOVEMENTSTART_FIELD));
        no.setWatchMovementStop((Boolean) map.get(ONMOVEMENTSTOP_FIELD));
        no.setWatchBatteryLow((Boolean) map.get(ONBATTERYLOW_FIELD));

        final long companyId = ((Number) map.get(COMPANY_FIELD)).longValue();
        Company company = cache.get(companyId);
        if (company == null) {
            company = companyDao.findOne(companyId);
            cache.put(companyId, company);
        }
        no.setCompany(company);

        return no;
    }

    /**
     * @return
     */
    private Map<String, String> createSelectAsMapping() {
        final Map<String, String> map = new HashMap<String, String>();
        for (final String field : getFields(true)) {
            map.put(field, field);
        }
        return map;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public void delete(final Long id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        jdbc.update("delete from " + TABLE + " where " + ID_FIELD + " = :id", paramMap);
    }
}
