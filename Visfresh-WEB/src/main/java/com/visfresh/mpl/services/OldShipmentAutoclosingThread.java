/**
 *
 */
package com.visfresh.mpl.services;


import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.visfresh.dao.OldShipmentsAutoclosingDao;
import com.visfresh.services.EmailService;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class OldShipmentAutoclosingThread {
    /**
     *
     */
    private static final long ONE_DAY = 24 * 60 * 60 * 1000l;

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(OldShipmentAutoclosingThread.class);

    /**
     * DAO.
     */
    @Autowired
    private OldShipmentsAutoclosingDao dao;
    /**
     * Maximum inactivity time out in days.
     */
    private int maxInactivityTimeOutDays = 25;
    /**
     * Email service.
     */
    @Autowired
    private EmailService emailer;

    /**
     * Default constructor.
     */
    @Autowired
    public OldShipmentAutoclosingThread(final Environment env) {
        super();
    }
    /**
     * Default constructor.
     */
    protected OldShipmentAutoclosingThread() {
        super();
    }

    /**
     * Run autoclosing.
     */
    @Scheduled(fixedDelay = 4 * 60 * 60 * 1000l)
    public void autoCloseShipmentsOfInactiveDevices() {
        log.debug("Autoclosing old shipments scheduled task has startd");

        //close shipments with inactive devices
        List<Long> ids;
        while((ids= dao.findNotClosedShipmentsWithInactiveDevices(20)).size() > 0) {
            dao.closeShipments(ids);
            log.debug("Redundand shipments (" + StringUtils.combine(ids, ", ") + ") have been autoclosed");
        }

        log.debug("Autoclosing old shipments scheduled task has stoped");
    }
    /**
     * @return the maxInactivityTimeOutDays
     */
    public int getMaxInactivityTimeOutDays() {
        return maxInactivityTimeOutDays;
    }
    /**
     * @param maxInactivityTimeOutDays the maxInactivityTimeOutDays to set
     */
    public void setMaxInactivityTimeOutDays(final int maxInactivityTimeOutDays) {
        this.maxInactivityTimeOutDays = maxInactivityTimeOutDays;
    }
    /**
     * Checks too long inactive devices and notifies the support team.
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void checkTooLongInactiveDevices() {
        final Date date = new Date(System.currentTimeMillis() - getMaxInactivityTimeOutDays() * ONE_DAY);
        final List<String> devices = dao.findDevicesWithoutReadingsAfter(date);

        if (!devices.isEmpty()) {
            try {
                emailer.sendMessageToSupport("Inactive devices detected", "Found "
                        + devices.size() + " inactive devices (" + StringUtils.combine(devices, ", ") + ")."
                        + " Possible need to disable them.");
            } catch (final MessagingException e) {
                log.error("Failed to send inactive devices message to support");
            }
        }
    }
    @Scheduled(fixedDelay = 1 * 60 * 60 * 1000l)
    public void deleteTooLongInactiveShipments() {
        final Date date = new Date(System.currentTimeMillis() - getMaxInactivityTimeOutDays() * ONE_DAY);
        final List<String> devices = dao.findDevicesWithoutReadingsAfter(date);

        if (!devices.isEmpty()) {
            final Map<String, List<Long>> deviceShipments = dao.findActiveShipmentsFor(devices);
            for (final Map.Entry<String, List<Long>> e : deviceShipments.entrySet()) {
                log.debug("Found active shipments (" + StringUtils.combine(e.getValue(), ",")
                    + " for device " + e.getKey() + ". Will auto closed");
                dao.closeShipments(e.getValue());
            }
        }
    }
}
