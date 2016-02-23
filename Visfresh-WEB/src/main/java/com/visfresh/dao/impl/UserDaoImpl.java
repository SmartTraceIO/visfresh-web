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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.UserConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Language;
import com.visfresh.entities.MeasurementUnits;
import com.visfresh.entities.Role;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.json.AbstractJsonSerializer;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UserDaoImpl extends EntityWithCompanyDaoImplBase<User, Long> implements UserDao {
    private static final String FIRSTNAME_FIELD = "firstname";
    private static final String LASTNAME_FIELD = "lastname";
    private static final String EXTERNALCOMPANY_FIELD = "externalcompany";
    private static final String POSITION_FIELD = "position";
    private static final String EMAIL_FIELD = "email";
    private static final String PHONE_FIELD = "phone";
    public static final String TABLE = "users";
    public static final String PROFILE_TABLE = "userprofiles";
    public static final String USER_SHIPMENTS = "usershipments";

    private static final String ID_FIELD = "id";
    private static final String PASSWORD_FIELD = "password";
    private static final String COMPANY_FIELD = "company";
    private static final String ROLES_FIELD = "roles";
    private static final String TIME_ZONE_FIELD = "timezone";
    private static final String TEMPERATURE_UNITS = "tempunits";
    private static final String DEVICEGROUP_FIELD = "devicegroup";
    private static final String LANGUAGE_FIELD = "language";
    private static final String MEASUREUNITS_FIELD = "measureunits";
    private static final String TITLE_FIELD = "title";
    private static final String ACTIVE_FIELD = "active";
    private static final String EXTERNAL_FIELD = "external";
    private static final String SETTINGS_FIELD = "settings";

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
        propertyToDbFields.put(UserConstants.PROPERTY_ID, ID_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_PHONE, PHONE_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_EMAIL, EMAIL_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_EXTERNAL_COMPANY, EXTERNALCOMPANY_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_POSITION, POSITION_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_LAST_NAME, LASTNAME_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_FIRST_NAME, FIRSTNAME_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_ACTIVE, ACTIVE_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_INTERNAL_COMPANY_ID, COMPANY_FIELD);
        propertyToDbFields.put(UserConstants.PROPERTY_EXTERNAL, EXTERNAL_FIELD);
    }

    public String convertToDatabaseColumn(final Collection<Role> roles) {
        final StringBuilder sb = new StringBuilder();

        if (roles != null) {
            for (final Role role : roles) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(role);
            }
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

        final List<String> fields = getFields();
        if (findOne(user.getId()) == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        paramMap.put(ID_FIELD, user.getId());
        paramMap.put(PASSWORD_FIELD, user.getPassword());
        paramMap.put(FIRSTNAME_FIELD, user.getFirstName());
        paramMap.put(LASTNAME_FIELD, user.getLastName());
        paramMap.put(EXTERNALCOMPANY_FIELD, user.getExternalCompany());
        paramMap.put(POSITION_FIELD, user.getPosition());
        paramMap.put(EMAIL_FIELD, user.getEmail());
        paramMap.put(PHONE_FIELD, user.getPhone());
        paramMap.put(ROLES_FIELD, convertToDatabaseColumn(user.getRoles()));
        paramMap.put(COMPANY_FIELD, user.getCompany().getId());
        paramMap.put(TIME_ZONE_FIELD, user.getTimeZone().getID());
        paramMap.put(TEMPERATURE_UNITS, user.getTemperatureUnits().toString());
        paramMap.put(DEVICEGROUP_FIELD, user.getDeviceGroup());
        paramMap.put(LANGUAGE_FIELD, user.getLanguage().toString());
        paramMap.put(MEASUREUNITS_FIELD, user.getMeasurementUnits().toString());
        paramMap.put(TITLE_FIELD, user.getTitle());
        paramMap.put(ACTIVE_FIELD, !Boolean.FALSE.equals(user.getActive()));
        paramMap.put(EXTERNAL_FIELD, Boolean.TRUE.equals(user.getExternal()));
        paramMap.put(SETTINGS_FIELD, AbstractJsonSerializer.toJson(user.getSettings()).toString());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().longValue());
        }

        return user;
    }
    /**
     * @return
     */
    private List<String> getFields() {
        final List<String> fields = new LinkedList<String>();
        fields.add(ID_FIELD);
        fields.add(PASSWORD_FIELD);
        fields.add(FIRSTNAME_FIELD);
        fields.add(LASTNAME_FIELD);
        fields.add(EXTERNALCOMPANY_FIELD);
        fields.add(POSITION_FIELD);
        fields.add(EMAIL_FIELD);
        fields.add(PHONE_FIELD);
        fields.add(ROLES_FIELD);
        fields.add(TIME_ZONE_FIELD);
        fields.add(TEMPERATURE_UNITS);
        fields.add(COMPANY_FIELD);
        fields.add(DEVICEGROUP_FIELD);
        fields.add(LANGUAGE_FIELD);
        fields.add(MEASUREUNITS_FIELD);
        fields.add(TITLE_FIELD);
        fields.add(ACTIVE_FIELD);
        fields.add(EXTERNAL_FIELD);
        fields.add(SETTINGS_FIELD);
        return fields;
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
        return ID_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected User createEntity(final Map<String, Object> row) {
        final User u = new User();
        u.setId(((Number) row.get(ID_FIELD)).longValue());
        u.setFirstName((String) row.get(FIRSTNAME_FIELD));
        u.setLastName((String) row.get(LASTNAME_FIELD));
        u.setExternalCompany((String) row.get(EXTERNALCOMPANY_FIELD));
        u.setPosition((String) row.get(POSITION_FIELD));
        u.setEmail((String) row.get(EMAIL_FIELD));
        u.setPhone((String) row.get(PHONE_FIELD));
        u.setPassword((String) row.get(PASSWORD_FIELD));
        u.setTimeZone(TimeZone.getTimeZone((String) row.get(TIME_ZONE_FIELD)));
        u.setTemperatureUnits(TemperatureUnits.valueOf((String) row.get(TEMPERATURE_UNITS)));
        u.setRoles(convertToEntityAttribute((String) row.get(ROLES_FIELD)));
        u.setDeviceGroup((String) row.get(DEVICEGROUP_FIELD));
        u.setLanguage(Language.valueOf((String) row.get(LANGUAGE_FIELD)));
        u.setMeasurementUnits(MeasurementUnits.valueOf((String) row.get(MEASUREUNITS_FIELD)));
        u.setTitle((String) row.get(TITLE_FIELD));
        u.setActive(!Boolean.FALSE.equals(row.get(ACTIVE_FIELD)));
        u.setExternal((Boolean) row.get(EXTERNAL_FIELD));
        u.getSettings().putAll(AbstractJsonSerializer.parseStringMap(
                SerializerUtils.parseJson((String) row.get(SETTINGS_FIELD))));
        return u;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.UserDao#findByEmail(java.lang.String)
     */
    @Override
    public User findByEmail(final String email) {
        final Filter f = new Filter();
        final String key = "filterPrefix_email";
        final SynteticFilter sf = new SynteticFilter() {
            @Override
            public Object[] getValues() {
                return new Object[]{email.toLowerCase()};
            }

            @Override
            public String[] getKeys() {
                return new String[] {key};
            }

            @Override
            public String getFilter() {
                return "LCASE(" + EMAIL_FIELD + ") = :" + key;
            }
        };

        f.addFilter(EMAIL_FIELD, sf);

        final List<User> all = findAll(f, null, null);
        return all.size() == 0 ? null : all.get(0);
    }
}
