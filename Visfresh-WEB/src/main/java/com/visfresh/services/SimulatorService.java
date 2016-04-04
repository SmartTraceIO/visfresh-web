/**
 *
 */
package com.visfresh.services;

import java.util.Date;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SimulatorService {
    /**
     * @param user user.
     */
    void startSimulator(User user, Date startDate, Date endDate, int velosity);
    /**
     * @param user user.
     */
    void stopSimulator(User user);
}
