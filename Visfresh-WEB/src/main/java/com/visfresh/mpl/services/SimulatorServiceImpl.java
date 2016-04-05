/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.visfresh.dao.Filter;
import com.visfresh.dao.SimulatorDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.SystemMessageDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.impl.SystemMessageDaoImpl;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.User;
import com.visfresh.io.SimulatorDto;
import com.visfresh.io.json.DeviceDcsNativeEventSerializer;
import com.visfresh.services.SimulatorService;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Controller
public class SimulatorServiceImpl implements SimulatorService {
    private static final Logger log = LoggerFactory.getLogger(SimulatorServiceImpl.class);

    @Autowired
    private SimulatorDao dao;
    @Autowired
    private MainSystemMessageDispatcher dispatcher;
    @Autowired
    private TrackerEventDao eventDao;
    @Autowired
    private SystemMessageDao systemMessageDao;

    /**
     * Default constructor.
     */
    public SimulatorServiceImpl() {
        super();
    }

    @PostConstruct
    public void setUp() {

    }
    @PreDestroy
    public void shutDown() {

    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SimulatorService#startSimulator(com.visfresh.entities.User, java.util.Date, java.util.Date, int)
     */
    @Override
    public void startSimulator(final User user, final Date startDate, final Date endDate,
            final int velosity) {
        final SimulatorDto dto = dao.findSimulatorDto(user);
        if (dto == null) {
            throw new RuntimeException("Simulator for user " + user.getEmail() + " is not created");
        }
        if (dto.isStarted()) {
            log.debug("Simulator for user " + user.getEmail() + " already started");
            return;
        }

        log.debug("Start simulator for " + user.getEmail() + ", virtual device: " + dto.getTargetDevice());
        final DeviceDcsNativeEventSerializer ser = new DeviceDcsNativeEventSerializer();

        int numEvents = 0;
        final List<ShortTrackerEvent> events = eventDao.findBy(dto.getSourceDevice(), startDate, endDate);
        if (events.size() > 0) {
            final long oldStart = events.get(0).getTime().getTime();
            final long newStart = System.currentTimeMillis();

            for (final ShortTrackerEvent te : events) {
                final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
                e.setBattery(te.getBattery());
                e.setImei(dto.getTargetDevice());
                e.setTemperature(te.getTemperature());
                e.getLocation().setLatitude(te.getLatitude());
                e.getLocation().setLongitude(te.getLongitude());
                e.setType(te.getType().name());

                //calculate new date
                final long dt = (te.getTime().getTime() - oldStart) / velosity;
                e.setDate(new Date(newStart + dt));

                //send as new native event from device.
                this.dispatcher.sendSystemMessage(
                        ser.toJson(e).toString(), SystemMessageType.Tracker, e.getTime());
                numEvents++;
            }
        }

        dao.setSimulatorStarted(user, true);
        log.debug(numEvents + " events have been simulated for user " + user.getEmail());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SimulatorService#stopSimulator(com.visfresh.entities.User)
     */
    @Override
    public void stopSimulator(final User user) {
        final SimulatorDto dto = dao.findSimulatorDto(user);
        if (dto == null) {
            log.debug("Simulator for user " + user.getEmail() + " is not created");
        }

        //select tracker events
        final Filter filter = new Filter();
        filter.addFilter(SystemMessageDaoImpl.TYPE_FIELD, SystemMessageType.Tracker.name());
        final Sorting sorting = new Sorting(false, SystemMessageDaoImpl.ID_FIELD);

        log.debug("Stopping simulator for user " + user.getEmail());

        int count = 0;
        final DeviceDcsNativeEventSerializer ser = new DeviceDcsNativeEventSerializer();
        for (final SystemMessage msg : systemMessageDao.findAll(filter, sorting, null)) {
            final DeviceDcsNativeEvent m = ser.parseDeviceDcsNativeEvent(
                    SerializerUtils.parseJson(msg.getMessageInfo()));
            if (m.getImei().equals(dto.getTargetDevice())) {
                systemMessageDao.delete(msg);
                count++;
            }
        }

        dao.setSimulatorStarted(user, false);
        log.debug("Finished of stoping simulator for user "
                + user.getEmail() + ". " + count + " device events is removed");
    }
}
