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
import au.smarttrace.ctrl.req.Order;
import au.smarttrace.ctrl.res.ListResponse;
import au.smarttrace.user.GetUsersRequest;
import au.smarttrace.user.UsersDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UsersDaoImpl extends AbstractDao implements UsersDao {
    private final Map<String, String> fieldAliases = new HashMap<>();

    /**
     * Default constructor.
     */
    public UsersDaoImpl() {
        super();

        fieldAliases.put("firstName", "firstname");
        fieldAliases.put("id", "id");
        fieldAliases.put("lastName", "lastname");
        fieldAliases.put("externalCompany", "externalcompany");
        fieldAliases.put("position", "position");
        fieldAliases.put("email", "email");
        fieldAliases.put("phone", "phone");
        fieldAliases.put("internalCompanyId", "company");
        fieldAliases.put("timeZone", "timezone");
        fieldAliases.put("temperatureUnits", "tempunits");
        fieldAliases.put("deviceGroup", "devicegroup");
        fieldAliases.put("language", "language");
        fieldAliases.put("measurementUnits", "measureunits");
        fieldAliases.put("title", "title");
        fieldAliases.put("active", "active");
        fieldAliases.put("external", "external");
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
        final Map<String, Object> params = userToDbMapIgnoreId(user);
        params.put("password", passwordHash);

        final Set<String> fields = new HashSet<>(params.keySet());
        final String sql = "insert into users(" + String.join(",", fields) + ")"
                + " values(:" + String.join(",:", fields) + ")";

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
        final Map<String, Object> params = userToDbMapIgnoreId(user);

        final List<String> sets = new LinkedList<>();
        for (final String key : params.keySet()) {
            sets.add(key + " = :" + key);
        }
        final String sql = "update users set " + String.join(", ", sets) + " where id = :id";

        params.put("id", user.getId());
        jdbc.update(sql, params);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.user.UsersDao#getUsers(au.smarttrace.ctrl.req.GetUsersRequest)
     */
    @Override
    public ListResponse<User> getUsers(final GetUsersRequest req) {
        final Map<String, Object> params = new HashMap<>();

        final List<String> where = new LinkedList<>();
        //companies filter.
        if (req.getCompanyFilter().size() > 0) {
            where.add("company in (" + String.join(",",
                    GetListQueryUtils.toStringList(req.getCompanyFilter())) + ")");
        }
        //email filter.
        if (req.getEmailFilter() != null) {
            where.add("email like :email");
            params.put("email", "%" + req.getEmailFilter() + "%");
        }

        //first and last name filter
        if (req.getNameFilter() != null) {
            final List<String> or = new LinkedList<>();
            final String[] tokens = GetListQueryUtils.tokenize(req.getNameFilter());
            for (int i = 0; i < tokens.length; i++) {
                final String t = tokens[i];
                final String key = "name_" + i;
                or.add("firstname like :" + key);
                or.add("lastname like :" + key);

                params.put(key, "%" + t + "%");
            }

            if (or.size() > 0) {
                where.add("(" + String.join(" or ", or) + ")");
            }
        }

        //create query
        StringBuilder sql = new StringBuilder("select * from users");
        if (where.size() > 0) {
            sql.append(" where ");
            sql.append(String.join(" and ", where));
        }

        //add ordering
        final List<String> orders = new LinkedList<>();
        for (final Order order : req.getOrders()) {
            final String f = fieldAliases.get(order.getField());
            //if alias exists add field to order
            if (f != null) {
                orders.add(f + (order.isAscent() ? "" : " desc"));
            }
        }

        //if order list empty, add default ordering
        if(orders.size() == 0) {
            orders.add("id");
        }
        sql.append(" order by ");
        sql.append(String.join(",", orders));

        //add limitation
        sql.append(" limit " + (req.getPage() * req.getPageSize()) + "," + req.getPageSize());

        //request data
        final ListResponse<User> resp = new ListResponse<>();
        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params);
        for (final Map<String, Object> row : rows) {
            resp.getItems().add(createUserFromDbRow(row));
        }

        //add total count
        sql = new StringBuilder("select count(*) as totalCount from users");
        if (where.size() > 0) {
            sql.append(" where ");
            sql.append(String.join(" and ", where));
        }

        rows = jdbc.queryForList(sql.toString(), params);
        if (rows.size() > 0) {
            resp.setTotalCount(((Number) rows.get(0).get("totalCount")).intValue());
        }

        return resp;
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
    /**
     * @param user
     * @return
     */
    protected Map<String, Object> userToDbMapIgnoreId(final User user) {
        final Map<String, Object> params = new HashMap<>();
        params.put("firstname", user.getFirstName());
        params.put("lastname", user.getLastName());
        params.put("externalcompany", user.getExternalCompany());
        params.put("position", user.getPosition());
        params.put("email", user.getEmail());
        params.put("phone", user.getPhone());
        params.put("roles", String.join(",", user.getRoles()));
        params.put("company", user.getCompany());
        params.put("timezone", user.getTimeZone().getID());
        params.put("tempunits", user.getTemperatureUnits().toString());
        params.put("devicegroup", user.getDeviceGroup());
        params.put("language", user.getLanguage().toString());
        params.put("measureunits", user.getMeasurementUnits().toString());
        params.put("title", user.getTitle());
        params.put("active", user.isActive());
        params.put("external", user.isExternal());
        return params;
    }
}
