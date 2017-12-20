/**
 *
 */
package au.smarttrace.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import au.smarttrace.Language;
import au.smarttrace.MeasurementUnits;
import au.smarttrace.TemperatureUnits;
import au.smarttrace.User;
import au.smarttrace.user.UsersDao;
import au.smarttrace.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UsersDaoImpl extends AbstractDao implements UsersDao {
    /**
     * Default constructor.
     */
    public UsersDaoImpl() {
        super();
    }
    /* (non-Javadoc)
     * @see demo.music.dao.UserDao#findUserByEmailAndPassword(java.lang.String, java.lang.String)
     */
    @Override
    public User findUserByEmailAndPassword(final String email, final String encriptPassword) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        params.put("password", encriptPassword);
        final List<Map<String, Object>> rows = jdbc.queryForList("select * from users u"
                + " where u.email = :email and u.password = :password", params);

        if (rows.size() > 0) {
            final Map<String, Object> row = rows.get(0);
            return createUserFromDbRow(row);
        }

        return null;
    }
    /* (non-Javadoc)
     * @see demo.music.user.UserDao#findUserByEmail(java.lang.String)
     */
    @Override
    public User findUserByEmail(final String email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        final List<Map<String, Object>> rows = jdbc.queryForList("select * from users u"
                + " where u.email = :email", params);

        if (rows.size() > 0) {
            final Map<String, Object> row = rows.get(0);
            return createUserFromDbRow(row);
        }

        return null;
    }
    /* (non-Javadoc)
     * @see demo.music.dao.UserDao#createUser(demo.music.User, java.lang.String)
     */
    @Override
    public void createUser(final User user, final String passwordHash) {
        final Map<String, Object> params = new HashMap<>();
        params.put("password", passwordHash);
        params.put("firstname", user.getFirstName());
        params.put("lastname", user.getLastName());
        params.put("externalcompany", user.getExternalCompany());
        params.put("position", user.getPosition());
        params.put("email", user.getEmail());
        params.put("phone", user.getPhone());
        params.put("roles", StringUtils.combine(user.getRoles(), ","));
        params.put("company", user.getCompany());
        params.put("timezone", user.getTimeZone().getID());
        params.put("tempunits", user.getTemperatureUnits().toString());
        params.put("devicegroup", user.getDeviceGroup());
        params.put("language", user.getLanguage().toString());
        params.put("measureunits", user.getMeasurementUnits().toString());
        params.put("title", user.getTitle());
        params.put("active", user.isActive());
        params.put("external", user.isExternal());

        final Set<String> fields = new HashSet<>(params.keySet());
        final String sql = "insert into users(" + StringUtils.combine(fields, ",") + ")"
                + " values(:" + StringUtils.combine(fields, ",:") + ")";

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(params), keyHolder);
        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().longValue());
        }
    }
    /* (non-Javadoc)
     * @see demo.music.user.UserDao#deleteUser(java.lang.String)
     */
    @Override
    public void deleteUser(final Long userId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", userId);

        jdbc.update("delete from users where id = :id", params);
    }
    /* (non-Javadoc)
     * @see demo.music.user.UserDao#updatePassword(java.lang.String, java.lang.String)
     */
    @Override
    public void updatePassword(final Long userId, final String passwordHash) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", userId);
        params.put("password", passwordHash);

        jdbc.update("update users set password = :password where id = :id", params);
    }
    /* (non-Javadoc)
     * @see demo.music.user.UserDao#updateUser(demo.music.User)
     */
    @Override
    public void saveUser(final User user) {
        final Map<String, Object> params = new HashMap<>();
        params.put("firstname", user.getFirstName());
        params.put("lastname", user.getLastName());
        params.put("externalcompany", user.getExternalCompany());
        params.put("position", user.getPosition());
        params.put("email", user.getEmail());
        params.put("phone", user.getPhone());
        params.put("roles", StringUtils.combine(user.getRoles(), ","));
        params.put("company", user.getCompany());
        params.put("timezone", user.getTimeZone().getID());
        params.put("tempunits", user.getTemperatureUnits().toString());
        params.put("devicegroup", user.getDeviceGroup());
        params.put("language", user.getLanguage().toString());
        params.put("measureunits", user.getMeasurementUnits().toString());
        params.put("title", user.getTitle());
        params.put("active", user.isActive());
        params.put("external", user.isExternal());

        final List<String> sets = new LinkedList<>();
        for (final String key : params.keySet()) {
            sets.add(key + " = :" + key);
        }
        final String sql = "update users set " + StringUtils.combine(sets, ", ") + " where id = :id";

        params.put("id", user.getId());
        jdbc.update(sql, params);
    }
    /**
     * @param row
     * @return
     */
    static User createUserFromDbRow(final Map<String, Object> row) {
        final User user = new User();
        user.setId(((Number) row.get("id")).longValue());
        user.setFirstName((String) row.get("firstname"));
        user.setLastName((String) row.get("lastname"));
        user.setExternalCompany((String) row.get("externalcompany"));
        user.setPosition((String) row.get("position"));
        user.setEmail((String) row.get("email"));
        user.setPhone((String) row.get("phone"));
        user.getRoles().addAll(parseRoles((String) row.get("roles")));
        user.setCompany(((Number) row.get("company")).longValue());
        user.setTimeZone(TimeZone.getTimeZone((String) row.get("timezone")));
        user.setTemperatureUnits(TemperatureUnits.valueOf((String) row.get("tempunits")));
        user.setDeviceGroup((String) row.get("devicegroup"));
        user.setLanguage(Language.valueOf((String) row.get("language")));
        user.setMeasurementUnits(MeasurementUnits.valueOf((String) row.get("measureunits")));
        user.setTitle((String) row.get("title"));
        user.setActive(Boolean.TRUE.equals(row.get("active")));
        user.setExternal(Boolean.TRUE.equals(row.get("external")));
        return user;
    }
    /**
     * @param rolesStr string of comma separated roles.
     * @return set of roles.
     */
    private static Set<String> parseRoles(final String rolesStr) {
        final Set<String> roles = new HashSet<>();
        if (rolesStr != null && rolesStr.length() > 0) {
            for (final String role : rolesStr.split(", *")) {
                roles.add(role);
            }
        }
        return roles;
    }
}
