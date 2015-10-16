/**
 *
 */
package com.visfresh.controllers;

import com.visfresh.entities.User;
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
    void checkCanGetLocationProfiles(User user) throws RestServiceException;
    /**
     * @param user user to check permissions.
     * @throws RestServiceException
     */
    void checkCanSaveLocationProfile(User user) throws RestServiceException;
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
}
