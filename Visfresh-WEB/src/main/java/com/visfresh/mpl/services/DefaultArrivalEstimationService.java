/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.mpl.services.arrival.ArrivalEstimationCalculator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {ArrivalEstimationCalculator.class})
public class DefaultArrivalEstimationService {
    private static final Logger log = LoggerFactory.getLogger(DefaultArrivalEstimationService.class);
    private long timeOut = -1;
    private int numThreads = 1;
    /**
     * Stopped flag.
     */
    private final AtomicBoolean stopped = new AtomicBoolean();
    @Autowired
    private CompanyDao companyDao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    protected ArrivalEstimationCalculator calculator;

    /**
     * Default constructor.
     */
    public DefaultArrivalEstimationService() {
        super();
    }
    /**
     * Default constructor.
     */
    @Autowired
    public DefaultArrivalEstimationService(final Environment env) {
        super();
        timeOut = Integer.parseInt(env.getProperty("eta.calculation.timeOut", "180")) * 1000l;
        numThreads = Integer.parseInt(env.getProperty("sibling.detect.numThreads", "1"));
    }

    /**
     * Initializes the service.
     */
    @PostConstruct
    public void startUp() {
        if (timeOut > -1) {
            log.debug("Starting eta calculation thread with " + (timeOut / 1000l)
                    + " sec time out");

            stopped.set(false);
            final Thread t = new Thread("Eta calculation thread") {
                /* (non-Javadoc)
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    while (true) {
                        synchronized (stopped) {
                            if (stopped.get()) {
                                break;
                            }
                        }

                        calculateEta();

                        synchronized (stopped) {
                            if (timeOut > 0) {
                                try {
                                    stopped.wait(timeOut);
                                } catch (final InterruptedException e) {
                                    stopped.set(true);
                                    log.debug("Eta calculation thread is interrupted. Will cancaled");
                                }
                            }
                        }
                    }

                    log.debug("Eta calculation thread has finished");
                }
            };

            t.start();
        } else {
            log.warn("Eta calculation time out is negatieve " + (timeOut / 1000l)
                    + ". Detection thread will not started");
        }
    }
    /**
     *
     */
    protected void calculateEta() {
        log.debug("ETA calculation has started");

        final ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        try {
            //TODO add pagination
            final List<Company> compaines = companyDao.findAll(null, null, null);
            for (final Company company : compaines) {
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final List<Shipment> shipments = shipmentDao.findActiveShipments(company);
                        for (final Shipment s : shipments) {
                            updateEtaForShipment(s);
                        }
                    }
                });
            }

            pool.shutdown();
            if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
                log.warn("End of ETA calculation is not reached");
            }
        } catch (final Throwable t) {
            log.error("Failed to calculate ETA", t);
        } finally {
            pool.shutdown();
        }

        log.debug("ETA calculation has finished");
    }
    /**
     * @param s shipment.
     */
    protected void updateEtaForShipment(final Shipment s) {
        final Location loc = getCurrentLocation(s);
        if (loc != null) {
            final Date eta = calculator.estimateArrivalDate(s, loc, s.getShipmentDate(), new Date());

            if (eta != null) {
                s.setEta(eta);
                updateEta(s, eta);
            }
        }
    }
    /**
     * @param s shipment.
     * @return current shipment location.
     */
    protected Location getCurrentLocation(final Shipment s) {
        final TrackerEvent e = trackerEventDao.getLastEvent(s);
        if (e != null) {
            return new Location(e.getLatitude(), e.getLongitude());
        }
        return null;
    }
    /**
     * @param s shipment.
     * @param eta ETA date.
     */
    protected void updateEta(final Shipment s, final Date eta) {
        shipmentDao.updateEta(s, eta);
    }
    /**
     * Destroys the service.
     */
    @PreDestroy
    public void shutDown() {
        synchronized (stopped) {
            stopped.set(true);
            stopped.notify();
        }
    }
}
