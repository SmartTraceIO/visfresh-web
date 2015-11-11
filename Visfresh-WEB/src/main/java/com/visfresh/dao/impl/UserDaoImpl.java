/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.constants.UserConstants;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UserDaoImpl extends EntityWithCompanyDaoImplBase<User, String> implements UserDao {
    public static final String TABLE = "users";
    public static final String PROFILE_TABLE = "userprofiles";
    public static final String USER_SHIPMENTS = "usershipments";

    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
    private static final String FULLNAME_FIELD = "fullname";
    private static final String COMPANY_FIELD = "company";
    private static final String ROLES_FIELD = "roles";
    private static final String TIME_ZONE_FIELD = "timezone";
    private static final String TEMPERATURE_UNITS = "tempunits";

    @Autowired
    private ShipmentDao shipmentDao;
    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public UserDaoImpl() {
        super();
        propertyToDbFields.put(UserConstants.PROPERTY_ROLES, ROLES_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_TEMPERATURE_UNITS, TEMPERATURE_UNITS);
        propertyToDbFields.put(UserConstants.PROPERTY_TIME_ZONE, TIME_ZONE_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_FULL_NAME, FULLNAME_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_LOGIN, USERNAME_FIELD);
    }

    public String convertToDatabaseColumn(final Collection<Role> roles) {
        final StringBuilder sb = new StringBuilder();

        for (final Role role : roles) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(role);
        }

        return sb.toString();
    }
    public Collection<Role> convertToEntityAttribute(final String dbData) {
        final List<Role> list = new LinkedList<Role>();

        if (dbData != null && dbData.length() > 0) {
            final String[] split = dbData.split(", *");
            for (int i = 0; i < split.length; i++) {
                list.add(Role.valueOf(split[i]));
            }
        }

        return list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends User> S save(final S user) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (findOne(user.getId()) == null) {
            //insert
            sql = "insert into " + TABLE + " (" + combine(
                    USERNAME_FIELD,
                    PASSWORD_FIELD,
                    FULLNAME_FIELD,
                    ROLES_FIELD,
                    TIME_ZONE_FIELD,
                    TEMPERATURE_UNITS,
                    COMPANY_FIELD
                ) + ")" + " values("
                    + ":"+ USERNAME_FIELD
                    + ", :" + PASSWORD_FIELD
                    + ", :" + FULLNAME_FIELD
                    + ", :" + ROLES_FIELD
                    + ", :" + TIME_ZONE_FIELD
                    + ", :" + TEMPERATURE_UNITS
                    + ", :" + COMPANY_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + PASSWORD_FIELD + "=:" + PASSWORD_FIELD
                + "," + FULLNAME_FIELD + "=:" + FULLNAME_FIELD
                + "," + ROLES_FIELD + "=:" + ROLES_FIELD
                + "," + COMPANY_FIELD + "=:" + COMPANY_FIELD
                + "," + TIME_ZONE_FIELD + "=:" + TIME_ZONE_FIELD
                + "," + TEMPERATURE_UNITS + "=:" + TEMPERATURE_UNITS
                + " where " + USERNAME_FIELD + " = :" + USERNAME_FIELD
            ;
        }

        paramMap.put(USERNAME_FIELD, user.getLogin());
        paramMap.put(PASSWORD_FIELD, user.getPassword());
        paramMap.put(FULLNAME_FIELD, user.getFullName());
        paramMap.put(ROLES_FIELD, convertToDatabaseColumn(user.getRoles()));
        paramMap.put(COMPANY_FIELD, user.getCompany().getId());
        paramMap.put(TIME_ZONE_FIELD, user.getTimeZone().getID());
        paramMap.put(TEMPERATURE_UNITS, user.getTemperatureUnits().toString());
        jdbc.update(sql, paramMap);

        return user;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.UserDao#getProfile(com.visfresh.entities.User)
     */
    @Override
    public UserProfile getProfile(final User user) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("user", user.getLogin());

        final String sql = "select * from " + PROFILE_TABLE + " where user = :user";
        final List<Map<String, Object>> list = jdbc.queryForList(sql, params);
        if (list.size() > 0) {
            final UserProfile p = new UserProfile();
            //load shipments

            final List<Map<String, Object>> shipmentIds = jdbc.queryForList("select shipment from "
                    + USER_SHIPMENTS + " where user = :user", params);
            for (final Map<String, Object> map : shipmentIds) {
                final Long id = ((Number) map.get("shipment")).longValue();
                final Shipment s = shipmentDao.findOne(id);
                p.getShipments().add(s);
            }
            return p;
        }

        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.UserDao#saveProfile(com.visfresh.entities.UserProfile)
     */
    @Override
    public void saveProfile(final User user, final UserProfile profile) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("user", user.getLogin());

        if (profile == null) {
            //create  profile record
            jdbc.update("delete from " + PROFILE_TABLE + " where user = :user", params);
        } else if (getProfile(user) == null) {
            jdbc.update("insert into " + PROFILE_TABLE + "(user) values (:user)", params);
        }

        //clear shipment links
        jdbc.update("delete from " + USER_SHIPMENTS + " where user = :user", params);

        if (profile != null) {
            //link with shipments
            for (final Shipment s : profile.getShipments()) {
                params.put("shipment", s.getId());
                jdbc.update("insert into " + USER_SHIPMENTS + "(user,shipment) values(:user, :shipment)", params);
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
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return USERNAME_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected User createEntity(final Map<String, Object> map) {
        final User u = new User();
        u.setLogin((String) map.get(USERNAME_FIELD));
        u.setFullName((String) map.get(FULLNAME_FIELD));
        u.setPassword((String) map.get(PASSWORD_FIELD));
        u.setTimeZone(TimeZone.getTimeZone((String) map.get(TIME_ZONE_FIELD)));
        u.setTemperatureUnits(TemperatureUnits.valueOf((String) map.get(TEMPERATURE_UNITS)));
        u.getRoles().addAll(convertToEntityAttribute((String) map.get(ROLES_FIELD)));
        return u;
    }
}
