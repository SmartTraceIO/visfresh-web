/**
 *
 */
package au.smarttrace.tt18.st.service;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;
import au.smarttrace.geolocation.DeviceMessageType;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;
import au.smarttrace.tt18.DeviceCommand;
import au.smarttrace.tt18.RawMessage;
import au.smarttrace.tt18.RawMessageHandler;
import au.smarttrace.tt18.st.db.DeviceCommandDao;
import au.smarttrace.tt18.st.db.MessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SmartTraceRawMessageHandler implements RawMessageHandler {
    /**
     * logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SmartTraceRawMessageHandler.class);
    @Autowired
    private MessageDao dao;
    @Autowired
    private DeviceCommandDao deviceCommandDao;
    @Autowired
    private LocationResolvingService locationResolver;

    /* (non-Javadoc)
     * @see au.smarttrace.tt18.RawMessageHandler#handleMessage(au.smarttrace.tt18.RawMessage)
     */
    @Override
    public List<String> handleMessage(final RawMessage msg) {
        if (msg.getTemperature() == null) {
            log.warn("Message for " + msg.getImei()
                    + " have null temperature and will ignored");
        } else {
            log.debug("New message for " + msg.getImei() + " has received");
            final DataWithGsmInfo<DeviceMessage> req = convert(msg);
            if (dao.checkDevice(req.getUserData().getImei())) {
                sendLocationResolvingRequest(req);
                //get commands
                final List<DeviceCommand> commands = deviceCommandDao.getFoDevice(
                        req.getUserData().getImei());

                final List<String> cmd = new LinkedList<>();
                for (final DeviceCommand c : commands) {
                    try {
                        deviceCommandDao.delete(c);
                        cmd.add(c.getCommand());
                    } catch (final Exception e) {
                    }
                }

                return cmd;
            }
        }

        return new LinkedList<String>();
    }
    /**
     * @param req
     */
    private void sendLocationResolvingRequest(final DataWithGsmInfo<DeviceMessage> req) {
        locationResolver.sendLocationResolvingRequest(req);
    }
    /**
     * @param raw raw message.
     * @return
     */
    protected DataWithGsmInfo<DeviceMessage> convert(final RawMessage raw) {
        final DataWithGsmInfo<DeviceMessage> req = new DataWithGsmInfo<DeviceMessage>();

        final DeviceMessage msg = new DeviceMessage();
        msg.setBattery(raw.getBattery());
        msg.setImei(raw.getImei());
        msg.setTemperature(raw.getTemperature());
        msg.setHumidity(raw.getHumidity());
        msg.setTime(raw.getTime());
        msg.setType(DeviceMessageType.AUT);
        req.setUserData(msg);

        //add station signal
        final GsmLocationResolvingRequest gsm = new GsmLocationResolvingRequest();
        gsm.setImei(msg.getImei());
        gsm.setRadio("gsm");

        req.setGsmInfo(gsm);

        final StationSignal s = new StationSignal();
        s.setCi(raw.getCellId());
        s.setLac(raw.getLac());
        s.setLevel(raw.getSignalLevel());
        s.setMcc(raw.getMcc());
        s.setMnc(raw.getMnc());

        gsm.getStations().add(s);

        return req;
    }
}
