/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Simulator;
import com.visfresh.entities.User;
import com.visfresh.io.SimulatorDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SimulatorDao {
    /**
     * @param user user.
     * @return simulator DTO.
     */
    SimulatorDto findSimulatorDto(User user);
    /**
     * @param sim simulator.
     */
    void save(Simulator sim);
    /**
     * @param user user.
     */
    void delete(User user);
}
