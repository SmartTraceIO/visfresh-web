/**
 *
 */
package au.smarttrace.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import au.smarttrace.security.AccessToken;
import au.smarttrace.security.AuthInfo;
import au.smarttrace.security.ClientSessionsDao;
import au.smarttrace.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ClientSessionsDaoImpl extends AbstractDao implements ClientSessionsDao {
    /**
     * Default constructor.
     */
    public ClientSessionsDaoImpl() {
        super();
    }
    /* (non-Javadoc)
     * @see demo.music.dao.UserDao#deleteSession(java.lang.String)
     */
    @Override
    public void deleteSession(final String token) {
        final Map<String, Object> params = new HashMap<>();
        params.put("token", token);
        jdbc.update("delete from restsessions where token = :token", params);
    }
    /* (non-Javadoc)
     * @see demo.music.dao.UserDao#getAuthInfo(java.lang.String)
     */
    @Override
    public AuthInfo getAuthInfo(final String token) {
        final Map<String, Object> params = new HashMap<>();
        params.put("token", token);
        final List<Map<String, Object>> rows = jdbc.queryForList(
            "select u.*, s.token as tkntoken, s.expiredon as tknexpiredon from users u"
            + " join restsessions s on u.id = s.user and s.token = :token", params);

        if (rows.size() > 0) {
            return createAuthInfo(rows.get(0));
        }

        return null;
    }
    /* (non-Javadoc)
     * @see demo.music.dao.UserDao#createSession(java.lang.String, demo.music.auth.AccessToken)
     */
    @Override
    public void createSession(final Long userId, final AccessToken token) {
        final Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("user", userId);
        paramMap.put("token", token.getToken());
        paramMap.put("client", "st-admin");
        paramMap.put("expiredon", token.getExpirationTime());
        paramMap.put("createdon", new Date());

        final List<String> fields = new LinkedList<>(paramMap.keySet());
        final String sql = "insert into restsessions(" + StringUtils.combine(fields, ",") + ") values (:"
                + StringUtils.combine(fields, ",:") + ")";

        jdbc.update(sql, paramMap);
    }
    /**
     * @param row DB data row.
     * @return
     */
    private AuthInfo createAuthInfo(final Map<String, Object> row) {
        final AuthInfo info = new AuthInfo();
        info.setToken(createToken(row));
        info.setUser(UsersDaoImpl.createUserFromDbRow(row));
        return info;
    }
    /**
     * @param row data row from DB.
     * @return access token.
     */
    private AccessToken createToken(final Map<String, Object> row) {
        final AccessToken t = new AccessToken((String) row.get("tkntoken"));
        t.setExpirationTime((Date) row.get("tknexpiredon"));
        return t;
    }
}
