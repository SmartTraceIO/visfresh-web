/**
 *
 */
package com.visfresh.controllers;

import com.visfresh.entities.User;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AccessController {
    /**
     * @param user current user.
     * @param username user name which info should be get.
     * @throws RestServiceException
     */
    void checkCanGetUserInfo(User user, String username) throws RestServiceException;
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
    void checkCanSaveDevice(User user) throws RestServiceException;
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
     * @param r request for create another user..
     */
    void checkCanCreateUser(User user, CreateUserRequest r) throws RestServiceException;
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
}
