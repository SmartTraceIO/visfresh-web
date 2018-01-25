/**
 *
 */
package au.smarttrace.tt18.st;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.smarttrace.tt18.RawMessage;
import au.smarttrace.tt18.RawMessageHandler;

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

    /* (non-Javadoc)
     * @see au.smarttrace.tt18.RawMessageHandler#handleMessage(au.smarttrace.tt18.RawMessage)
     */
    @Override
    public void handleMessage(final RawMessage msg) {
        final String messageStr = toHexString(msg.getRawData());
        if (msg.getTemperature() == null) {
            log.warn("Message for " + msg.getImei()
                    + " have null temperature and will ignored " + messageStr);
        } else {
            log.debug("New message for " + msg.getImei() + " has received: " + messageStr);
            final DeviceMessage m = convert(msg);
            if (dao.checkDevice(m.getImei())) {
                dao.saveForNextProcessingInDcs(m);
            }
        }
    }

    /**
     * @param rawData
     * @return
     */
    private String toHexString(final byte[] rawData) {
        final StringBuilder sb = new StringBuilder();
        for (final byte b : rawData) {
            final String s = Integer.toHexString(0xFF & b);
            if (s.length() < 2) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * @param raw raw message.
     * @return
     */
    protected DeviceMessage convert(final RawMessage raw) {
        final DeviceMessage msg = new DeviceMessage();
        msg.setBattery(raw.getBattery());
        msg.setImei(raw.getImei());
        msg.setRetryOn(new Date());
        msg.setTemperature(raw.getTemperature());
        msg.setTime(raw.getTime());
        msg.setType(DeviceMessageType.AUT);
        msg.setTypeString(DeviceMessageType.AUT.name());

        //add station signal
        final StationSignal s = new StationSignal();
        s.setCi(raw.getCellId());
        s.setLac(raw.getLac());
        s.setLevel(raw.getSignalLevel());
        s.setMcc(raw.getMcc());
        s.setMnc(raw.getMnc());

        msg.getStations().add(s);

        return msg;
    }
}
