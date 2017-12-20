/**
 *
 */
package au.smarttrace.user;

import au.smarttrace.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface UsersService {
    /**
     * Creates new user.
     * @param user user.
     * @param password password.
     */
    void createUser(final User user, final String password);
    /**
     * Deletes user by given email.
     * @param userId user's email.
     */
    void deleteUser(final Long userId);
    /**
     * @param email user's email.
     * @param password password.
     * @return user if found, null otherwise
     */
    User findUserByEmailPassword(String email, String password);
    /**
     * @param user user.
     * @param password password.
     */
    void updateUser(User user, String password);
    /**
     * Changes password for given user.
     * @param userId user's email.
     * @param password password.
     */
    void changePassword(Long userId, String password);
}
