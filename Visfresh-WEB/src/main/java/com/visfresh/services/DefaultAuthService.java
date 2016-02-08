/**
 *
 */
package com.visfresh.services;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.UserDao;
import com.visfresh.entities.User;
import com.visfresh.utils.HashGenerator;
import com.visfresh.utils.Messages;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultAuthService implements AuthService {
    /**
     *
     */
    private static final String RESET_ON_LOGIN = "resetOnLogin";
    @Autowired
    private UserDao userDao;
    @Autowired
    private EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(DefaultAuthService.class);

    private static final long DEFAULT_TOKEN_ACTIVE_TIMEOUT = 4 * 60 * 60 * 1000l; //one hour
    public static final int USER_LOGIN_LIMIT = 1000;

    private static final long TIMEOUT = 60000L;

    private final AtomicBoolean isStopped = new AtomicBoolean();

    private final Map<String, UserInfo> users = new HashMap<String, UserInfo>();
    private final Map<String, PasswordResetRequest> passwordResets = new HashMap<String, PasswordResetRequest>();
    private static final Random random = new Random();

    /**
     * Default constructor.
     */
    public DefaultAuthService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#login(java.lang.String, java.lang.String)
     */
    @Override
    public AuthToken login(final String email, final String password)
            throws AuthenticationException {
        final User user = userDao.findByEmail(email);
        if (user == null) {
            throw new AuthenticationException("Unknown user " + email);
        }

        if (user.getPassword().equals(generateHash(password))) {
            final String resetPasswordOnLogin = user.getSettings().remove(RESET_ON_LOGIN);
            if ("true".equals(resetPasswordOnLogin)) {
                //set any random password and save. It flushes the current user password
                user.setPassword(generateRandomPassword());
                userDao.save(user);
            }

            synchronized (users) {
                final UserInfo u = new UserInfo();
                u.setUser(user);
                u.setToken(generateNewToken(user));
                users.put(u.getToken().getToken(), u);
                log.debug("Registering new token " + u);

                removeExpiredUsers(email);
                return u.getToken();
            }
        }

        throw new AuthenticationException("Authentication failed");
    }

    /**
     * @param email user email.
     */
    private void removeExpiredUsers(final String email) {
        final List<UserInfo> list = new LinkedList<UserInfo>();

        for (final UserInfo ui : users.values()) {
            if (ui.getUser().getEmail().equals(email)) {
                list.add(ui);
            }
        }

        if (list.size() > USER_LOGIN_LIMIT) {
            //sort users and remove old
            Collections.sort(list);
            while (list.size() > USER_LOGIN_LIMIT) {
                final UserInfo toRemove = list.remove(0);
                users.remove(toRemove.getToken().getToken());
                log.debug("Old auth token for " + toRemove.getUser().getEmail()
                        + " has removed according of max logged in users limit");
            }
        }
    }

    /**
     * @return
     */
    private String generateRandomPassword() {
        final Random r = new Random(System.currentTimeMillis());

        final char[] chars = new char[20];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('0' + r.nextInt(10));
        }

        return generateHash(new String(chars));
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#saveUser(com.visfresh.entities.User, java.lang.String, boolean)
     */
    @Override
    public final void saveUser(final User user, final String password, final boolean resetOnLogin) {
        if (password != null) {
            user.setPassword(generateHash(password));
        }
        if (resetOnLogin) {
            user.getSettings().put(RESET_ON_LOGIN, "true");
        }

        userDao.save(user);
    }
    /**
     * @param user user.
     * @return
     */
    protected AuthToken generateNewToken(final User user) {
        final String token = user.getId() + "-" + generateHash(Long.toString(random.nextLong()));
        final AuthToken t = new AuthToken(token);
        t.setExpirationTime(new Date(System.currentTimeMillis() + DEFAULT_TOKEN_ACTIVE_TIMEOUT));
        return t;
    }

    @PostConstruct
    public void start() {
        startCheckTokensThread();
        startPasswordResetsExpirationThread();
    }

    /**
     *
     */
    private void startPasswordResetsExpirationThread() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    removeExpiredPasswordResets();

                    synchronized (isStopped) {
                        try {
                            isStopped.wait(TIMEOUT);
                        } catch (final InterruptedException e) {
                            log.error("Check password reset expiration thread has interrupted", e);
                            return;
                        }
                        if (isStopped.get()) {
                            log.debug("Check password reset expiration thread has stopped");
                            return;
                        }
                    }
                }
            }
        }.start();
    }
    /**
     *
     */
    protected void startCheckTokensThread() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    removeExpiredTokens();

                    synchronized (isStopped) {
                        try {
                            isStopped.wait(TIMEOUT);
                        } catch (final InterruptedException e) {
                            log.error("Check token expiration thread has interrupted", e);
                            return;
                        }
                        if (isStopped.get()) {
                            log.debug("Check token expiration thread has stopped");
                            return;
                        }
                    }
                }
            }
        }.start();
    }

    /**
     *
     */
    protected void removeExpiredTokens() {
        final long time = System.currentTimeMillis();

        synchronized (users) {
            final Iterator<UserInfo> userIterator = users.values().iterator();
            while (userIterator.hasNext()) {
                final UserInfo ui  = userIterator.next();
                if (ui.getToken().getExpirationTime().getTime() < time) {
                    log.debug("Access token for user " + ui.getUser().getEmail()
                            + " has expired and will removed");
                    userIterator.remove();
                }
            }
        }
    }
    /**
     *
     */
    protected void removeExpiredPasswordResets() {
        final long time = System.currentTimeMillis();

        synchronized (passwordResets) {
            final Iterator<Map.Entry<String, PasswordResetRequest>> iter
                = passwordResets.entrySet().iterator();
            while (iter.hasNext()) {
                final Map.Entry<String, PasswordResetRequest> e = iter.next();
                if (e.getValue().getExpirationTime().getTime() < time) {
                    log.debug("Password reset request for user " + e.getKey()
                            + " has expired and will removed");
                    iter.remove();
                }
            }
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#startResetPassword(java.lang.String, java.lang.String)
     */
    @Override
    public void startResetPassword(final String email, final String baseUrl) throws AuthenticationException {
        final Map<String, String> replacements = new HashMap<>();
        replacements.put("email", email);

        final User user = userDao.findByEmail(email);
        if (user == null) {
            throw new AuthenticationException(Messages.getMessage("passwordreset.userNotFound", replacements));
        }

        PasswordResetRequest reset;
        synchronized(passwordResets) {
            reset = passwordResets.get(email);
            if (reset == null) {
                reset = new PasswordResetRequest(generateSecureString());
                passwordResets.put(email, reset);
                log.debug("Passwort reset request has created for user " + email);
            } else {
                reset.resetExpiration();
                log.debug("Found unexpired passwort reset request for user " + email);
            }
        }

        //send email to user
        replacements.put("url", baseUrl + "token=" + reset.getSecureString());
        try {
            emailService.sendMessage(new String[]{email},
                    Messages.getMessage("passwordreset.reset.subject", replacements),
                    Messages.getMessage("passwordreset.reset.text", replacements));
            log.debug("Reset password URL has send to " + email + " and contains the token "
                    + reset.getSecureString());
        } catch (final MessagingException e) {
            throw new AuthenticationException("Failed to send password reset message to user " + email, e);
        }
    }
    /**
     * @return secure string.
     */
    private static String generateSecureString() {
        final StringBuilder sb = new StringBuilder(Integer.toString(1 + random.nextInt(8)));
        while (sb.length() < 5) {
            sb.append(random.nextInt(9));
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#resetPassword(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void resetPassword(final String email, final String password, final String token)
            throws AuthenticationException {
        final Map<String, String> replacements = new HashMap<>();
        replacements.put("email", email);

        PasswordResetRequest reset;
        synchronized (passwordResets) {
            reset = passwordResets.get(email);
        }

        //check request found
        if (reset == null) {
            throw new AuthenticationException(Messages.getMessage(
                    "passwordreset.reset.requestNotFound", replacements));
        }

        //check token matches
        if (!reset.getSecureString().equals(token)) {
            throw new AuthenticationException(Messages.getMessage(
                    "passwordreset.reset.tokenNotMatches", replacements));
        }

        passwordResets.remove(email);
        final User user = userDao.findByEmail(email);
        if (user == null) {
            throw new AuthenticationException(Messages.getMessage("passwordreset.userNotFound", replacements));
        }

        saveUser(user, password, false);
        log.debug("New password has reset for user " + email);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#refreshToken(java.lang.String)
     */
    @Override
    public AuthToken refreshToken(final String oldToken)
            throws AuthenticationException {
        final UserInfo info = getUserInfoForToken(oldToken);
        if (info == null) {
            throw new AuthenticationException("Not authorized or token expired");
        }

        info.setToken(generateNewToken(info.getUser()));
        return info.getToken();
    }
    /**
     * @param authToken
     * @return
     */
    private UserInfo getUserInfoForToken(final String authToken) {
        synchronized (users) {
            return users.get(authToken);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#getUserForToken(java.lang.String)
     */
    @Override
    public User getUserForToken(final String authToken) {
        final UserInfo info = getUserInfoForToken(authToken);
        return info == null ? null : info.getUser();
    }

    /**
     * Stops the service.
     */
    @PreDestroy
    public void stop() {
        synchronized (isStopped) {
            isStopped.set(true);
            isStopped.notifyAll();
        }
    }
    /**
     * @param password password.
     * @return password hash.
     */
    protected String generateHash(final String password) {
        return HashGenerator.createMd5Hash(password);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#logout(java.lang.String)
     */
    @Override
    public void logout(final String authToken) {
        final User user = getUserForToken(authToken);
        if (user != null) {
            synchronized (users) {
                users.remove(user.getEmail());
            }
        }
    }
}
