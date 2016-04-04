/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.SimulatorDao;
import com.visfresh.entities.Simulator;
import com.visfresh.entities.User;
import com.visfresh.io.SimulatorDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SimulatorDaoImpl implements SimulatorDao {
    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public SimulatorDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.SimulatorDao#findSimulatorDto(com.visfresh.entities.User)
     */
    @Override
    public SimulatorDto findSimulatorDto(final User user) {
        return user == null ? null : findSimulatorDto(user.getId());
    }
    private SimulatorDto findSimulatorDto(final Long userId) {
        final Map<String, Object> params = new HashMap<>();
        params.put("user", userId);

        final List<Map<String, Object>> rows = jdbc.queryForList("select"
                + " s.source as source, u.email as user, s.target as target from simulators s"
                + " join users u on u.id = s.user where s.user = :user", params);
        if (rows.size() > 0) {
            final Map<String, Object> row = rows.get(0);
            final SimulatorDto s = new SimulatorDto();
            s.setSourceDevice((String) row.get("source"));
            s.setTargetDevice((String) row.get("target"));
            s.setUser((String) row.get("user"));
            return s;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.SimulatorDao#save(com.visfresh.entities.Simulator)
     */
    @Override
    public void save(final Simulator sim) {
        final Map<String, Object> params = new HashMap<>();
        params.put("source", sim.getSource().getImei());
        params.put("target", sim.getTarget().getImei());
        params.put("user", sim.getUser().getId());

        if (findSimulatorDto(sim.getUser()) != null) {
            //update. Only source device can be updated
            jdbc.update("update simulators set source = :source where user = :user", params);
        } else {
            //insert
            jdbc.update("insert into simulators(source, target, user) values (:source, :target, :user)", params);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.SimulatorDao#delete(com.visfresh.entities.User)
     */
    @Override
    public void delete(final User user) {
        final SimulatorDto dto = findSimulatorDto(user);

        final Map<String, Object> params = new HashMap<>();
        params.put("device", dto.getTargetDevice());

        if (dto != null) {
            jdbc.update("delete from devicecommands where device = :device", params);
            jdbc.update("delete from alerts where device = :device", params);
            jdbc.update("delete from arrivals where device = :device", params);
            jdbc.update("delete from trackerevents where device = :device", params);
            jdbc.update("delete from shipments where device = :device", params);
            jdbc.update("delete from devices where imei = :device", params);
            //simulator should be deleted cascade by target device
        }
    }
}
