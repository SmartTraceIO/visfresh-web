/**
 *
 */
package com.visfresh.controllers;

import java.util.Set;

import com.visfresh.entities.Company;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AccessController {
    /**
     * @param user current user.
     * @param userId user name which info should be get.
     * @throws RestServiceException
     */
    void checkCanGetUserInfo(User user, Long userId) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanSendCommandToDevice(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanGetShipmentData(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanGetShipments(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanSaveShipment(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanGetDevices(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanManageDevices(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanGetShipmentTemplates(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanSaveShipmentTemplate(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanGetNotificationSchedules(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanSaveNotificationSchedule(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanGetLocations(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanSaveLocation(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanGetAlertProfiles(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanSaveAlertProfile(User user) throws RestServiceException;
    /**
     * @param user user.
     * @throws RestServiceException
     */
    void checkGetProfile(User user) throws RestServiceException;
    /**
     * @param user user.
     */
    void checkSaveProfile(User user) throws RestServiceException;
    /**
     * @param user current user.
     * @param company TODO
     */
    void checkCanManageUsers(User user, Company company) throws RestServiceException;
    /**
     * @param user user.
     * @param id company ID.
     * @throws RestServiceException
     */
    void checkCanGetCompany(User user, Long id) throws RestServiceException;
    /**
     * @param user user
     */
    void checkCanGetCompanies(User user) throws RestServiceException;
    /**
     * @param user
     * @param userId
     * @throws RestServiceException
     */
    void checkUpdateUserDetails(User user, Long userId) throws RestServiceException;
    /**
     * @param user user.
     */
    void checkCanListUsers(User user) throws RestServiceException;
    /**
     * @param user user.
     */
    void checkCanManageDeviceGroups(User user) throws RestServiceException;
    /**
     * @param user user.
     */
    void checkCanViewDeviceGroups(User user) throws RestServiceException;
    /**
     * @param user user.
     * @param group device group.
     * @throws RestServiceException
     */
    void checkCanViewDeviceGroup(User user, DeviceGroup group) throws RestServiceException;
    /**
     * @param user user to check.
     * @param roles
     * @throws RestServiceException
     */
    void checkCanAssignRoles(User user, Set<Role> roles) throws RestServiceException;
}
