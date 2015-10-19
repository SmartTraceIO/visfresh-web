/**
 *
 */
package com.visfresh.controllers;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultAccessController implements AccessController {
    /**
     * Default constructor.
     */
    public DefaultAccessController() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanGetUserInfo(com.visfresh.entities.User, java.lang.String)
     */
    @Override
    public void checkCanGetUserInfo(final User user, final String username) throws RestServiceException {
        if (!hasOneRoleFrom(user, Role.GlobalAdmin, Role.CompanyAdmin) && !user.getLogin().equals(username)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not privileges for get user info for " + username);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanSendCommandToDevice(com.visfresh.entities.User)
     */
    @Override
    public void checkCanSendCommandToDevice(final User user)
            throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for send command to device");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanGetShipmentData(com.visfresh.entities.User)
     */
    @Override
    public void checkCanGetShipmentData(final User user) throws RestServiceException {
        if (!hasOneRoleFrom(user, Role.GlobalAdmin, Role.CompanyAdmin, Role.Dispatcher, Role.ReportViewer)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for get shipment data");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanGetShipments(com.visfresh.entities.User)
     */
    @Override
    public void checkCanGetShipments(final User user) throws RestServiceException {
        if (!hasOneRoleFrom(user, Role.GlobalAdmin, Role.CompanyAdmin, Role.Dispatcher, Role.ReportViewer)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for view reports");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanSaveShipment(com.visfresh.entities.User)
     */
    @Override
    public void checkCanSaveShipment(final User user) throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for save shipment");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanGetDevices(com.visfresh.entities.User)
     */
    @Override
    public void checkCanGetDevices(final User user) throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for get device info");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanSaveDevice(com.visfresh.entities.User)
     */
    @Override
    public void checkCanSaveDevice(final User user) throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for save device");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanGetShipmentTemplates(com.visfresh.entities.User)
     */
    @Override
    public void checkCanGetShipmentTemplates(final User user)
            throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for get shipment templates");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanSaveShipmentTemplate(com.visfresh.entities.User)
     */
    @Override
    public void checkCanSaveShipmentTemplate(final User user)
            throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for save shipment template");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanGetNotificationSchedules(com.visfresh.entities.User)
     */
    @Override
    public void checkCanGetNotificationSchedules(final User user)
            throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for get notification schedules");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanSaveNotificationSchedule(com.visfresh.entities.User)
     */
    @Override
    public void checkCanSaveNotificationSchedule(final User user)
            throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for save notification schedule");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanGetLocationProfiles(com.visfresh.entities.User)
     */
    @Override
    public void checkCanGetLocationProfiles(final User user)
            throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for get location profiles");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanSaveLocationProfile(com.visfresh.entities.User)
     */
    @Override
    public void checkCanSaveLocationProfile(final User user)
            throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for location profile");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanGetAlertProfiles(com.visfresh.entities.User)
     */
    @Override
    public void checkCanGetAlertProfiles(final User user) throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for get alert profiles");
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkCanSaveAlertProfile(com.visfresh.entities.User)
     */
    @Override
    public void checkCanSaveAlertProfile(final User user) throws RestServiceException {
        if (!canDispatch(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    "User has not permissions for save alert profiles");
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkGetProfile(com.visfresh.entities.User)
     */
    @Override
    public void checkGetProfile(final User user) throws RestServiceException {
        //nothing, each user can get its profile.
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.AccessController#checkSaveProfile(com.visfresh.entities.User)
     */
    @Override
    public void checkSaveProfile(final User user) throws RestServiceException {
        //nothing, each user can save its own profile.
    }

    /**
     * @param user the user.
     * @return true if have dispatcher privileges.
     */
    private boolean canDispatch(final User user) {
        return hasOneRoleFrom(user, Role.GlobalAdmin, Role.CompanyAdmin, Role.Dispatcher);
    }

    /**
     * @param user user.
     */
    private boolean hasOneRoleFrom(final User user, final Role... roles) {
        final Set<Role> r = user.getRoles();

        //check contains role.
        for (final Role role : roles) {
            if (r.contains(role)) {
                return true;
            }
        }

        return false;
    }
}
