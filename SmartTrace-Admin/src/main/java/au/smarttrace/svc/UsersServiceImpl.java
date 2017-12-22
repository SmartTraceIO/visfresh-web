/**
 *
 */
package au.smarttrace.svc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.smarttrace.Roles;
import au.smarttrace.User;
import au.smarttrace.ctrl.res.ListResponse;
import au.smarttrace.user.GetUsersRequest;
import au.smarttrace.user.UsersDao;
import au.smarttrace.user.UsersService;
import au.smarttrace.utils.HashGenerator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UsersServiceImpl implements UsersService {
    private static final Logger log = LoggerFactory.getLogger(UsersServiceImpl.class);
    @Autowired
    private UsersDao dao;

    /**
     * Default constructor.
     */
    public UsersServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see demo.music.user.UserService#createUser(demo.music.User, java.lang.String)
     */
    @Override
    public void createUser(final User user, final String password) {
        dao.createUser(user, HashGenerator.generateHash(password));
    }
    /* (non-Javadoc)
     * @see demo.music.auth.AbstractAccessService#findUserByEmailPassword(java.lang.String, java.lang.String)
     */
    @Override
    public User findUserByEmailPassword(final String email, final String password) {
        return dao.findUserByEmailAndPassword(email, HashGenerator.generateHash(password));
    }

    /* (non-Javadoc)
     * @see demo.music.user.UserService#deleteUser(java.lang.String)
     */
    @Override
    public void deleteUser(final Long userId) {
        dao.deleteUser(userId);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.user.UsersService#updateUser(au.smarttrace.User, java.lang.String)
     */
    @Override
    public void updateUser(final User user, final String password) {
        dao.saveUser(user);
        if (password != null) {
            changePassword(user.getId(), password);
        }
    }
    /* (non-Javadoc)
     * @see demo.music.user.UserService#changePassword(java.lang.String, java.lang.String)
     */
    @Override
    public void changePassword(final Long userId, final String password) {
        dao.updatePassword(userId, HashGenerator.generateHash(password));
    }
    /* (non-Javadoc)
     * @see au.smarttrace.user.UsersService#getUsers(au.smarttrace.ctrl.req.GetUsersRequest)
     */
    @Override
    public ListResponse<User> getUsers(final GetUsersRequest req) {
        return dao.getUsers(req);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.user.UsersService#getRoles()
     */
    @Override
    public List<String> getRoles() {
        final List<String> roles = new LinkedList<>();

        final Field[] fields = Roles.class.getFields();
        for (final Field field : fields) {
            if ((field.getModifiers() & Modifier.STATIC) != 0)
            try {
                roles.add((String) field.get(null));
            } catch (final Exception e) {
                log.error("Failed to get value of role " + field.getName());
            }
        }

        return roles;
    }
}
