/**
 *
 */
package com.visfresh.mock;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.visfresh.entities.User;
import com.visfresh.services.SimulatorService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSimulatorService implements SimulatorService {
    /**
     * Default constructor.
     */
    public MockSimulatorService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SimulatorService#startSimulator(com.visfresh.entities.User, java.util.Date, java.util.Date, int)
     */
    @Override
    public void startSimulator(final User user, final Date startDate, final Date endDate,
            final int velosity) {
        // nothing
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SimulatorService#stopSimulator(com.visfresh.entities.User)
     */
    @Override
    public void stopSimulator(final User user) {
        // nothing
    }
}
