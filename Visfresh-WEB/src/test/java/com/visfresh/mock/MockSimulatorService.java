/**
 *
 */
package com.visfresh.mock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.entities.User;
import com.visfresh.io.StartSimulatorRequest;
import com.visfresh.services.SimulatorService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSimulatorService implements SimulatorService {
    public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    /**
     * Simulator requests.
     */
    private final List<StartSimulatorRequest> requests = new LinkedList<>();

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
        final StartSimulatorRequest req = new StartSimulatorRequest();
        if (endDate != null) {
            req.setEndDate(FORMAT.format(endDate));
        }
        if (startDate != null) {
            req.setStartDate(FORMAT.format(startDate));
        }
        req.setUser(user.getEmail());
        req.setVelosity(velosity);

        requests.add(req);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SimulatorService#stopSimulator(com.visfresh.entities.User)
     */
    @Override
    public void stopSimulator(final User user) {
        final Iterator<StartSimulatorRequest> iter = requests.iterator();
        while (iter.hasNext()) {
            final StartSimulatorRequest req = iter.next();
            if (req.getUser().equals(user.getEmail())) {
                iter.remove();
                break;
            }
        }
    }
    /**
     * @return the requests
     */
    public List<StartSimulatorRequest> getRequests() {
        return requests;
    }
    public void clear() {
        requests.clear();
    }
}
