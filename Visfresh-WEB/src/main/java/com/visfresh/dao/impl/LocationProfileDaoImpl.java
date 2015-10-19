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

import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LocationProfileDaoImpl extends DaoImplBase<LocationProfile, Long>
    implements LocationProfileDao {

    public static final String TABLE = "locationprofiles";

    //field columns
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String COMPANY_DESCRIPTION_FIELD = "companydetails";
    private static final String NOTES_FIELD = "notes";
    private static final String ADDRESS_FIELD = "address";
    private static final String START_FIELD = "start";
    private static final String STOP_FIELD = "stop";
    private static final String INTERIM_FIELD = "interim";
    private static final String LATITUDE_FIELD = "latitude";
    private static final String LONGITUDE_FIELD = "longitude";
    private static final String RADIUS_FIELD = "radius";
    private static final String COMPANY_FIELD = "company";

    private static final String ID_PLACEHOLDER = "32_497803_29475";

    @Autowired
    private CompanyDao companyDao;
    /**
     * Default constructor.
     */
    public LocationProfileDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends LocationProfile> S save(final S lp) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (lp.getId() == null) {
            //insert
            sql = "insert into " + TABLE + " (" + combine(
                    NAME_FIELD
                    , COMPANY_DESCRIPTION_FIELD
                    , NOTES_FIELD
                    , ADDRESS_FIELD
                    , START_FIELD
                    , STOP_FIELD
                    , INTERIM_FIELD
                    , LATITUDE_FIELD
                    , LONGITUDE_FIELD
                    , RADIUS_FIELD
                    , COMPANY_FIELD
                ) + ")" + " values("
                    + ":"+ NAME_FIELD
                    + ", :" + COMPANY_DESCRIPTION_FIELD
                    + ", :" + NOTES_FIELD
                    + ", :" + ADDRESS_FIELD
                    + ", :" + START_FIELD
                    + ", :" + STOP_FIELD
                    + ", :" + INTERIM_FIELD
                    + ", :" + LATITUDE_FIELD
                    + ", :" + LONGITUDE_FIELD
                    + ", :" + RADIUS_FIELD
                    + ", :" + COMPANY_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + NAME_FIELD + "=:" + NAME_FIELD
                + "," + COMPANY_DESCRIPTION_FIELD + "=:" + COMPANY_DESCRIPTION_FIELD
                + "," + NOTES_FIELD + "=:" + NOTES_FIELD
                + "," + ADDRESS_FIELD + "=:" + ADDRESS_FIELD
                + "," + START_FIELD + "=:" + START_FIELD
                + "," + STOP_FIELD + "=:" + STOP_FIELD
                + "," + INTERIM_FIELD + "=:" + INTERIM_FIELD
                + "," + LATITUDE_FIELD + "=:" + LATITUDE_FIELD
                + "," + LONGITUDE_FIELD + "=:" + LONGITUDE_FIELD
                + "," + RADIUS_FIELD + "=:" + RADIUS_FIELD
                + "," + COMPANY_FIELD + "=:" + COMPANY_FIELD
                + " where " + ID_FIELD + " = :" + ID_FIELD
            ;
        }

        paramMap.put(ID_FIELD, lp.getId());
        paramMap.put(NAME_FIELD, lp.getName());
        paramMap.put(COMPANY_DESCRIPTION_FIELD, lp.getCompanyDescription());
        paramMap.put(NOTES_FIELD, lp.getNotes());
        paramMap.put(ADDRESS_FIELD, lp.getAddress());
        paramMap.put(START_FIELD, lp.isStart());
        paramMap.put(STOP_FIELD, lp.isStop());
        paramMap.put(INTERIM_FIELD, lp.isInterim());
        paramMap.put(LATITUDE_FIELD, lp.getLocation().getLatitude());
        paramMap.put(LONGITUDE_FIELD, lp.getLocation().getLongitude());
        paramMap.put(RADIUS_FIELD, lp.getRadius());
        paramMap.put(COMPANY_FIELD, lp.getCompany().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            lp.setId(keyHolder.getKey().longValue());
        }

        return lp;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public LocationProfile findOne(final Long id) {
        if (id == null) {
            return null;
        }

        final List<Map<String, Object>> list = runSelectScript(id);
        return list.size() == 0 ? null : createLocationProfile(list.get(0), new HashMap<Long, Company>());
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
                + (id == null ? "" : " where " + ID_FIELD + " = :" + ID_PLACEHOLDER),
                params);
        return list;
    }
    /**
     * @return
     */
    private Map<String, String> createSelectAsMapping() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(ID_FIELD,  ID_FIELD);
        map.put(NAME_FIELD, NAME_FIELD);
        map.put(COMPANY_DESCRIPTION_FIELD, COMPANY_DESCRIPTION_FIELD);
        map.put(NOTES_FIELD, NOTES_FIELD);
        map.put(ADDRESS_FIELD, ADDRESS_FIELD);
        map.put(START_FIELD, START_FIELD);
        map.put(STOP_FIELD, STOP_FIELD);
        map.put(INTERIM_FIELD, INTERIM_FIELD);
        map.put(LATITUDE_FIELD, LATITUDE_FIELD);
        map.put(LONGITUDE_FIELD, LONGITUDE_FIELD);
        map.put(RADIUS_FIELD, RADIUS_FIELD);
        map.put(COMPANY_FIELD, COMPANY_FIELD);
        return map;
    }
    /**
     * @param map
     * @return
     */
    private LocationProfile createLocationProfile(final Map<String, Object> map,
            final Map<Long, Company> userCache) {
        final LocationProfile no = new LocationProfile();
        no.setId(((Number) map.get(ID_FIELD)).longValue());

        no.setAddress((String) map.get(ADDRESS_FIELD));
        no.setCompanyDescription((String) map.get(COMPANY_DESCRIPTION_FIELD));
        no.setInterim((Boolean) map.get(INTERIM_FIELD));
        no.setName((String) map.get(NAME_FIELD));
        no.setNotes((String) map.get(NOTES_FIELD));
        no.setRadius(((Number) map.get(RADIUS_FIELD)).intValue());
        no.setStart((Boolean) map.get(START_FIELD));
        no.setStop((Boolean) map.get(STOP_FIELD));
        no.getLocation().setLatitude(((Number) map.get(LATITUDE_FIELD)).doubleValue());
        no.getLocation().setLongitude(((Number) map.get(LONGITUDE_FIELD)).doubleValue());

        final long companyId = ((Number) map.get(COMPANY_FIELD)).longValue();
        Company company = userCache.get(companyId);
        if (company == null) {
            company = companyDao.findOne(companyId);
            userCache.put(companyId, company);
        }
        no.setCompany(company);

        return no;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<LocationProfile> findAll() {
        final List<Map<String, Object>> list = runSelectScript(null);

        final Map<Long, Company> userCache = new HashMap<Long, Company>();
        final List<LocationProfile> result = new LinkedList<LocationProfile>();
        for (final Map<String,Object> map : list) {
            result.add(createLocationProfile(map, userCache));
        }
        return result;
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
