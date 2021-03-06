/**
 *
 */
package com.visfresh.rules;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.services.DeviceCommandService;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class RepeatShutdownRule implements TrackerEventRule {
    protected static final long CHECK_SHUTDOWN_TIMEOUT = 60 * 60 * 1000L;
    public static final String NAME = "RepeatShutdown";
    private static final Logger log = LoggerFactory.getLogger(RepeatShutdownRule.class);

    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private DeviceCommandService commandService;

    /**
     * Default constructor.
     */
    public RepeatShutdownRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#accept(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        if (context.isProcessed(this) || e.getType() == TrackerEventType.INIT) {
            return false;
        }

        final long time = e.getTime().getTime();

        //check already in processing
        final DeviceState deviceState = context.getDeviceState();
        final Date shutdownRepeatTime = getShutDownRepeatTime(deviceState);

        //if already sent shutdown
        if (shutdownRepeatTime != null) {
            return false;
        }

        //check is not in processing, but should be accepted
        final Shipment s = getLastShutDownedShipment(e);

        boolean accepted = false;
        if (s != null && time - s.getDeviceShutdownTime().getTime() > CHECK_SHUTDOWN_TIMEOUT) {
            accepted = true;
        }

        return accepted;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.TrackerEventRule#handle(com.visfresh.rules.RuleContext)
     */
    @Override
    public boolean handle(final RuleContext context) {
        context.setProcessed(this);

        final TrackerEvent e = context.getEvent();
        final Device device = e.getDevice();

        if (shouldRepeatShutdown(context)) {
            log.debug("Shutdown faulure has detected for " + device.getImei()
                    + ". Resending shutdown");
            final Date date = new Date();
            setShutDownRepeatTime(context.getDeviceState(), date);
            shutDownDevice(device, date);
        }

        return false;
    }

    /**
     * @param deviceState device state.
     * @param beacon TODO
     * @return
     */
    public static Date getShutDownRepeatTime(final DeviceState deviceState) {
        final String str = deviceState.getProperty(createKey());
        try {
            return str == null ? null : createDateFormat().parse(str);
        } catch (final ParseException e) {
            log.error("Failed to parse date " + str);
            setShutDownRepeatTime(deviceState, null);
            return null;
        }
    }
    /**
     * @param deviceState
     * @param date
     */
    public static void setShutDownRepeatTime(final DeviceState deviceState, final Date date) {
        deviceState.setProperty(createKey(), date == null
                ? null : createDateFormat().format(date));
    }

    /**
     * @return date format by UTC time zone.
     */
    private static DateFormat createDateFormat() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(SerializerUtils.UT??);
        return sdf;
    }
    /**
     * @return
     */
    protected static String createKey() {
        return NAME + "_shutdownTime";
    }
    /**
     * @param e
     * @return
     */
    private Shipment getLastShutDownedShipment(final TrackerEvent e) {
        Shipment s = e.getShipment();
        if (s == null) {
            s = findLastShipment(e.getDevice().getImei());
        }

        if (s != null && !(s.hasFinalStatus() && s.getDeviceShutdownTime() != null)) {
            s = null;
        }

        return s;
    }

    /**
     * @param device
     * @param date
     */
    protected void shutDownDevice(final Device device, final Date date) {
        commandService.shutdownDevice(device, date);
    }
    /**
     * @param context
     * @return
     */
    private boolean shouldRepeatShutdown(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        final boolean should = getShutDownRepeatTime(context.getDeviceState()) == null;
        if (should) {
            final Shipment s = getLastShutDownedShipment(e);
            if (e.getTime().getTime() - s.getDeviceShutdownTime().getTime() > 2 * CHECK_SHUTDOWN_TIMEOUT) {
                return false;
            }
        }
        return should;
    }
    /**
     * @param s
     */
    protected void saveShipment(final Shipment s) {
        shipmentDao.save(s);
    }
    /**
     * @param imei device IMEI.
     * @return shipment.
     */
    protected Shipment findLastShipment(final String imei) {
        return shipmentDao.findLastShipment(imei);
    }
}
