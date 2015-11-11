/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.LocationConstants;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LocationProfileDaoImpl extends EntityWithCompanyDaoImplBase<LocationProfile, Long>
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

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public LocationProfileDaoImpl() {
        super();
        propertyToDbFields.put(LocationConstants.PROPERTY_END_FLAG, STOP_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_INTERIM_FLAG, INTERIM_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_START_FLAG, START_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_RADIUS_METERS, RADIUS_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_LON, LONGITUDE_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_LAT, LATITUDE_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_ADDRESS, ADDRESS_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_NOTES, NOTES_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_COMPANY_NAME, COMPANY_DESCRIPTION_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_LOCATION_NAME, NAME_FIELD);
        propertyToDbFields.put(LocationConstants.PROPERTY_LOCATION_ID, ID_FIELD);
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
        paramMap.put(COMPANY_DESCRIPTION_FIELD, lp.getCompanyName());
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
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID_FIELD;
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
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected LocationProfile createEntity(final Map<String, Object> map) {
        final LocationProfile no = new LocationProfile();
        no.setId(((Number) map.get(ID_FIELD)).longValue());

        no.setAddress((String) map.get(ADDRESS_FIELD));
        no.setCompanyName((String) map.get(COMPANY_DESCRIPTION_FIELD));
        no.setInterim((Boolean) map.get(INTERIM_FIELD));
        no.setName((String) map.get(NAME_FIELD));
        no.setNotes((String) map.get(NOTES_FIELD));
        no.setRadius(((Number) map.get(RADIUS_FIELD)).intValue());
        no.setStart((Boolean) map.get(START_FIELD));
        no.setStop((Boolean) map.get(STOP_FIELD));
        no.getLocation().setLatitude(((Number) map.get(LATITUDE_FIELD)).doubleValue());
        no.getLocation().setLongitude(((Number) map.get(LONGITUDE_FIELD)).doubleValue());

        return no;
    }
}
