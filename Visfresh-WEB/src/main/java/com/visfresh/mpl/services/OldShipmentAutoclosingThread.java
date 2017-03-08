/**
 *
 */
package com.visfresh.mpl.services;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.visfresh.dao.OldShipmentsAutoclosingDao;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class OldShipmentAutoclosingThread {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(OldShipmentAutoclosingThread.class);

    @Autowired
    private OldShipmentsAutoclosingDao dao;
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
    public void doAutoclose() {
        log.debug("Autoclosing old shipments scheduled task has startd");

        //close shipments with inactive devices
        List<Long> ids;
        while((ids= dao.findNotClosedShipmentsWithInactiveDevices(20)).size() > 0) {
            dao.closeShipments(ids);
            log.debug("Redundand shipments (" + StringUtils.combine(ids, ", ") + ") have been autoclosed");
        }

        log.debug("Autoclosing old shipments scheduled task has stoped");
    }
}
