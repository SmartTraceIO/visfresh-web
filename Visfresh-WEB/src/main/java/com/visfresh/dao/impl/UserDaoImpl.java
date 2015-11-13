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
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UserDaoImpl extends EntityWithCompanyDaoImplBase<User, String> implements UserDao {
    private static final String FIRSTNAME_FIELD = "firstname";
    private static final String LASTNAME_FIELD = "lastname";
    private static final String POSITION_FIELD = "position";
    private static final String EMAIL_FIELD = "email";
    private static final String PHONE_FIELD = "phone";
    public static final String TABLE = "users";
    public static final String PROFILE_TABLE = "userprofiles";
    public static final String USER_SHIPMENTS = "usershipments";

    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
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
        propertyToDbFields.put(UserConstants.PROPERTY_LOGIN, USERNAME_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_PHONE, PHONE_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_EMAIL, EMAIL_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_POSITION, POSITION_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_LAST_NAME, LASTNAME_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_FIRST_NAME, FIRSTNAME_FIELD);
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
                    FIRSTNAME_FIELD,
                    LASTNAME_FIELD,
                    POSITION_FIELD,
                    EMAIL_FIELD,
                    PHONE_FIELD,
                    ROLES_FIELD,
                    TIME_ZONE_FIELD,
                    TEMPERATURE_UNITS,
                    COMPANY_FIELD
                ) + ")" + " values("
                    + ":"+ USERNAME_FIELD
                    + ", :" + PASSWORD_FIELD
                    + ", :" + FIRSTNAME_FIELD
                    + ", :" + LASTNAME_FIELD
                    + ", :" + POSITION_FIELD
                    + ", :" + EMAIL_FIELD
                    + ", :" + PHONE_FIELD
                    + ", :" + ROLES_FIELD
                    + ", :" + TIME_ZONE_FIELD
                    + ", :" + TEMPERATURE_UNITS
                    + ", :" + COMPANY_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + PASSWORD_FIELD + "=:" + PASSWORD_FIELD
                + "," + FIRSTNAME_FIELD + "=:" + FIRSTNAME_FIELD
                + "," + LASTNAME_FIELD + "=:" + LASTNAME_FIELD
                + "," + POSITION_FIELD + "=:" + POSITION_FIELD
                + "," + EMAIL_FIELD + "=:" + EMAIL_FIELD
                + "," + PHONE_FIELD + "=:" + PHONE_FIELD
                + "," + ROLES_FIELD + "=:" + ROLES_FIELD
                + "," + COMPANY_FIELD + "=:" + COMPANY_FIELD
                + "," + TIME_ZONE_FIELD + "=:" + TIME_ZONE_FIELD
                + "," + TEMPERATURE_UNITS + "=:" + TEMPERATURE_UNITS
                + " where " + USERNAME_FIELD + " = :" + USERNAME_FIELD
            ;
        }

        paramMap.put(USERNAME_FIELD, user.getLogin());
        paramMap.put(PASSWORD_FIELD, user.getPassword());
        paramMap.put(FIRSTNAME_FIELD, user.getFirstName());
        paramMap.put(LASTNAME_FIELD, user.getLastName());
        paramMap.put(POSITION_FIELD, user.getPosition());
        paramMap.put(EMAIL_FIELD, user.getEmail());
        paramMap.put(PHONE_FIELD, user.getPhone());
        paramMap.put(ROLES_FIELD, convertToDatabaseColumn(user.getRoles()));
        paramMap.put(COMPANY_FIELD, user.getCompany().getId());
        paramMap.put(TIME_ZONE_FIELD, user.getTimeZone().getID());
        paramMap.put(TEMPERATURE_UNITS, user.getTemperatureUnits().toString());
        jdbc.update(sql, paramMap);

        return user;
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
        u.setFirstName((String) map.get(FIRSTNAME_FIELD));
        u.setLastName((String) map.get(LASTNAME_FIELD));
        u.setPosition((String) map.get(POSITION_FIELD));
        u.setEmail((String) map.get(EMAIL_FIELD));
        u.setPhone((String) map.get(PHONE_FIELD));
        u.setPassword((String) map.get(PASSWORD_FIELD));
        u.setTimeZone(TimeZone.getTimeZone((String) map.get(TIME_ZONE_FIELD)));
        u.setTemperatureUnits(TemperatureUnits.valueOf((String) map.get(TEMPERATURE_UNITS)));
        u.getRoles().addAll(convertToEntityAttribute((String) map.get(ROLES_FIELD)));
        return u;
    }
}
