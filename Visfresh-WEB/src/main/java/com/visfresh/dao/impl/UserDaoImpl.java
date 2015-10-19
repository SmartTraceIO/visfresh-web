/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UserDaoImpl extends DaoImplBase<User, String> implements UserDao {
    public static final String TABLE = "users";
    public static final String PROFILE_TABLE = "userprofiles";
    public static final String USER_SHIPMENTS = "usershipments";

    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
    private static final String FULLNAME_FIELD = "fullname";
    private static final String COMPANY_FIELD = "company";
    private static final String ROLES_FIELD = "roles";

    @Autowired
    private ShipmentDao shipmentDao;
    /**
     * Default constructor.
     */
    public UserDaoImpl() {
        super();
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
            paramMap.put("id", user.getId());
            sql = "insert into " + TABLE + " (" + combine(
                    USERNAME_FIELD,
                    PASSWORD_FIELD,
                    FULLNAME_FIELD,
                    ROLES_FIELD,
                    COMPANY_FIELD
                ) + ")" + " values("
                    + ":"+ USERNAME_FIELD
                    + ", :" + PASSWORD_FIELD
                    + ", :" + FULLNAME_FIELD
                    + ", :" + ROLES_FIELD
                    + ", :" + COMPANY_FIELD
                    + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + PASSWORD_FIELD + "=:" + PASSWORD_FIELD
                + "," + FULLNAME_FIELD + "=:" + FULLNAME_FIELD
                + "," + ROLES_FIELD + "=:" + ROLES_FIELD
                + "," + COMPANY_FIELD + "=:" + COMPANY_FIELD
                + " where " + USERNAME_FIELD + " = :" + USERNAME_FIELD
            ;
        }

        paramMap.put(USERNAME_FIELD, user.getLogin());
        paramMap.put(PASSWORD_FIELD, user.getPassword());
        paramMap.put(FULLNAME_FIELD, user.getFullName());
        paramMap.put(ROLES_FIELD, convertToDatabaseColumn(user.getRoles()));
        paramMap.put(COMPANY_FIELD, user.getCompany().getId());

        jdbc.update(sql, paramMap);

        return user;
    }
    @Override
    public User findOne(final String id) {
        final String entityName = "u";
        final String companyEntityName = "c";
        final String resultPrefix = "u_";
        final String companyResultPrefix = "c_";

        final List<Map<String, Object>> list = runSelect(id, entityName,
                companyEntityName, resultPrefix, companyResultPrefix);

        return list.size() == 0 ? null : createUser(resultPrefix, companyResultPrefix, list.get(0));
    }

    /**
     * @param id
     * @param entityName
     * @param companyEntityName
     * @param resultPrefix
     * @param companyResultPrefix
     * @return
     */
    private List<Map<String, Object>> runSelect(final String id,
            final String entityName, final String companyEntityName,
            final String resultPrefix, final String companyResultPrefix) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAME_FIELD, id);

        final Map<String, String> fields = createSelectAsMapping(entityName, resultPrefix);
        final Map<String, String> companyFields = CompanyDaoImpl.createSelectAsMapping(
                companyEntityName, companyResultPrefix);
        params.putAll(fields);
        params.putAll(companyFields);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + ", " + buildSelectAs(companyFields)
                + " from "
                + TABLE + " " + entityName
                + ", " + CompanyDaoImpl.TABLE + " " + companyEntityName
                + " where "
                + entityName + "." + COMPANY_FIELD + " = " + companyEntityName + "." + CompanyDaoImpl.ID_FIELD
                + (id == null ? "" : " and " + entityName + "." + USERNAME_FIELD + " = :" + USERNAME_FIELD),
                params);
        return list;
    }

    /**
     * @param resultPrefix
     * @param companyResultPrefix
     * @param map
     * @return
     */
    public User createUser(final String resultPrefix,
            final String companyResultPrefix, final Map<String, Object> map) {
        final User u = new User();
        u.setCompany(CompanyDaoImpl.createCompany(companyResultPrefix, map));
        u.setLogin((String) map.get(resultPrefix + USERNAME_FIELD));
        u.setFullName((String) map.get(resultPrefix + FULLNAME_FIELD));
        u.setPassword((String) map.get(resultPrefix + PASSWORD_FIELD));
        u.getRoles().addAll(convertToEntityAttribute((String) map.get(resultPrefix + ROLES_FIELD)));
        return u;
    }

    /**
     * @param entityName
     * @param resultPrefix
     * @return
     */
    public static Map<String, String> createSelectAsMapping(final String entityName,
            final String resultPrefix) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(entityName + "." + USERNAME_FIELD, resultPrefix + USERNAME_FIELD);
        map.put(entityName + "." + PASSWORD_FIELD, resultPrefix + PASSWORD_FIELD);
        map.put(entityName + "." + ROLES_FIELD, resultPrefix + ROLES_FIELD);
        map.put(entityName + "." + FULLNAME_FIELD, resultPrefix + FULLNAME_FIELD);
        return map ;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<User> findAll() {
        final String entityName = "d";
        final String companyEntityName = "c";
        final String resultPrefix = "d_";
        final String companyResultPrefix = "c_";

        final List<Map<String, Object>> list = runSelect(null,
                entityName, companyEntityName, resultPrefix, companyResultPrefix);

        final List<User> result = new LinkedList<User>();
        for (final Map<String,Object> map : list) {
            result.add(createUser(resultPrefix, companyResultPrefix, map));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public void delete(final String id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        jdbc.update("delete from " + TABLE + " where " + USERNAME_FIELD + " = :id", paramMap);
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

        if (getProfile(user) == null) {
            //create  profile record
            jdbc.update("insert into " + PROFILE_TABLE + "(user) values (:user)", params);
        } else {
            //clear shipment links
            jdbc.update("delete from " + USER_SHIPMENTS + " where user = :user", params);
        }

        //link with shipments
        for (final Shipment s : profile.getShipments()) {
            params.put("shipment", s.getId());
            jdbc.update("insert into " + USER_SHIPMENTS + "(user,shipment) values(:user, : shipment)", params);
        }
    }
}
