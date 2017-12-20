/**
 *
 */
package au.smarttrace.user;

import au.smarttrace.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface UsersDao {
    /**
     * @param email user's email.
     * @param encriptPassword encrypted password.
     * @return user with given email if password matches.
     */
    User findUserByEmailAndPassword(String email, String encriptPassword);
    /**
     * @param user the user.
     * @param passwordHash encrypted user's password.
     */
    void createUser(User user, String passwordHash);
    /**
     * @param userId email.
     * @param passwordHash password hash.
     */
    void updatePassword(Long userId, String passwordHash);
    /**
     * Updates user info, not affects password and roles.
     * @param user user.
     */
    void saveUser(User user);
    /**
     * Deletes user by given email.
     * @param userId email.
     */
    void deleteUser(Long userId);
    /**
     * @param email email.
     * @return user by given email.
     */
    User findUserByEmail(String email);
}
