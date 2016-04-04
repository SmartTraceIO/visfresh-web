/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Simulator;
import com.visfresh.io.SimulatorDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SimulatorDao extends DaoBase<Simulator, Long> {
    /**
     * @param userId user Id.
     * @return simulator DTO.
     */
    SimulatorDto findSimulatorDto(Long userId);
}
