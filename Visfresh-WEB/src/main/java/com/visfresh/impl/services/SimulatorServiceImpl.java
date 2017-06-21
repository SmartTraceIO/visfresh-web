/**
 *
 */
package com.visfresh.impl.services;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.visfresh.dao.SimulatorDao;
import com.visfresh.dao.SystemMessageDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Location;
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

    /* (non-Javadoc)
     * @see com.visfresh.services.SimulatorService#startSimulator(com.visfresh.entities.User, java.util.Date, java.util.Date, int)
     */
    @Override
    public void startSimulator(final User user, final Date startDate, final Date endDate,
            final int velosity) {
        final SimulatorDto dto = findSimulator(user);
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
        final List<ShortTrackerEvent> events = getTrackerEvents(dto.getSourceDevice(), startDate, endDate);
        if (events.size() > 0) {
            final long oldStart = events.get(0).getTime().getTime();
            final long newStart = System.currentTimeMillis();

            for (final ShortTrackerEvent te : events) {
                final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
                e.setBattery(te.getBattery());
                e.setImei(dto.getTargetDevice());
                e.setTemperature(te.getTemperature());
                if (te.getLatitude() != null && te.getLongitude() != null) {
                    final Location loc = new Location(te.getLatitude(), te.getLongitude());
                    e.setLocation(loc);
                }
                e.setType(te.getType().name());

                //calculate new date
                final long dt = (te.getTime().getTime() - oldStart) / velosity;
                e.setDate(new Date(newStart + dt));

                //send as new native event from device.
                sendSystemMessage(ser.toJson(e).toString(), SystemMessageType.Tracker, e.getDate());
                numEvents++;
            }
        }

        setSimulatorStarted(user, true);
        log.debug(numEvents + " events have been simulated for user " + user.getEmail());
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SimulatorService#stopSimulator(com.visfresh.entities.User)
     */
    @Override
    public void stopSimulator(final User user) {
        final SimulatorDto dto = findSimulator(user);
        if (dto == null) {
            log.debug("Simulator for user " + user.getEmail() + " is not created");
        }

        //select tracker events
        log.debug("Stopping simulator for user " + user.getEmail());

        int count = 0;
        final DeviceDcsNativeEventSerializer ser = new DeviceDcsNativeEventSerializer();
        for (final SystemMessage msg : findTrackerEventMessages()) {
            final DeviceDcsNativeEvent m = ser.parseDeviceDcsNativeEvent(
                    SerializerUtils.parseJson(msg.getMessageInfo()));
            if (m.getImei().equals(dto.getTargetDevice())) {
                deleteSystemMessage(msg);
                count++;
            }
        }

        setSimulatorStarted(user, false);
        log.debug("Finished of stoping simulator for user "
                + user.getEmail() + ". " + count + " device events is removed");
    }

    /**
     * @param sourceDevice
     * @param startDate
     * @param endDate
     * @return
     */
    protected List<ShortTrackerEvent> getTrackerEvents(final String sourceDevice,
            final Date startDate, final Date endDate) {
        return eventDao.findBy(sourceDevice, startDate, endDate);
    }
    /**
     * @param user user.
     * @param started started flag.
     */
    protected void setSimulatorStarted(final User user, final boolean started) {
        dao.setSimulatorStarted(user, started);
    }
    /**
     * @param msg
     */
    protected void deleteSystemMessage(final SystemMessage msg) {
        systemMessageDao.delete(msg);
    }
    /**
     * @return list of tracker system messages
     */
    protected List<SystemMessage> findTrackerEventMessages() {
        return systemMessageDao.findTrackerEvents(false);
    }
    /**
     * @param payload
     * @param type
     * @param time
     */
    protected void sendSystemMessage(final String payload, final SystemMessageType type, final Date time) {
        this.dispatcher.sendSystemMessage(payload, type, time);
    }
    /**
     * @param user
     * @return
     */
    protected SimulatorDto findSimulator(final User user) {
        return dao.findSimulatorDto(user);
    }
}
