/**
 *
 */
package com.visfresh.mpl.services;


import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;
import com.visfresh.mpl.services.siblings.DefaultSiblingDetector;
import com.visfresh.mpl.services.siblings.TestSiblingDetector;
import com.visfresh.services.ShipmentSiblingService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {DefaultSiblingDetector.class})
public class ShipmentSiblingServiceImpl implements ShipmentSiblingService {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ShipmentSiblingServiceImpl.class);

    @Autowired
    private TestSiblingDetector testDetector;
    @Autowired
    private DefaultSiblingDetector siblingDetector;

    /**
     * Sibling detection time out.
     */
    private long detectSiblingsTimeOut = -1;
    /**
     * Stopped flag.
     */
    private final AtomicBoolean stopped = new AtomicBoolean();
    /**
     * Default constructor.
     */
    @Autowired
    public ShipmentSiblingServiceImpl(final Environment env) {
        super();
        detectSiblingsTimeOut = Integer.parseInt(env.getProperty("sibling.detect.timeOut", "15")) * 1000l;
    }
    /**
     * Default constructor.
     */
    protected ShipmentSiblingServiceImpl() {
        super();
    }

    /**
     * Initializes the service.
     */
    @PostConstruct
    public void startUp() {
        if (detectSiblingsTimeOut > -1) {
            log.debug("Starting sibling detection thread with " + (detectSiblingsTimeOut / 1000l)
                    + " sec time out");

            stopped.set(false);
            final Thread t = new Thread("Sibling detection thread") {
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

                        siblingDetector.detectSiblings();

                        synchronized (stopped) {
                            if (detectSiblingsTimeOut > 0) {
                                try {
                                    stopped.wait(detectSiblingsTimeOut);
                                } catch (final InterruptedException e) {
                                    stopped.set(true);
                                    log.debug("Sibbling detection thread is interrupted. Will cancaled");
                                }
                            }
                        }
                    }

                    log.debug("Sibling detector thread has finished");
                }
            };

            t.start();
        } else {
            log.warn("Sibling detection time out is negatieve " + (detectSiblingsTimeOut / 1000l)
                    + ". Detection thread will not started");
        }
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

    /* (non-Javadoc)
     * @see com.visfresh.services.SiblingDetectorService#getSiblings(com.visfresh.entities.Shipment)
     */
    @Override
    public List<Shipment> getSiblings(final Shipment shipment) {
        final List<Shipment> siblings = testDetector.getSiblings(shipment);
        if (!siblings.isEmpty()) {
            return siblings;
        }

        return siblingDetector.getSiblings(shipment);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SiblingDetectorService#getSiblingCount(com.visfresh.entities.Shipment)
     */
    @Override
    public int getSiblingCount(final Shipment s) {
        final int count = testDetector.getSiblingCount(s);
        if (count == 0) {
            return siblingDetector.getSiblingCount(s);
        }
        return count;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.ShipmentSiblingService#getSiblingColors(com.visfresh.entities.Shipment, java.util.List)
     */
    @Override
    public Map<Long, Color> getSiblingColors(final Shipment masterShipment, final List<Shipment> siblings) {
        // add master color
        final Map<Long, Color> map = new HashMap<>();
        map.put(masterShipment.getId(), Color.GREEN);

        //create color list
        final LinkedList<Color> colors = new LinkedList<Color>();
        colors.add(Color.BLUE);
        colors.add(Color.CYAN);
        colors.add(Color.GRAY);
        colors.add(Color.MAGENTA);
        colors.add(Color.ORANGE);
        colors.add(Color.PINK);
        colors.add(Color.RED);
        colors.add(Color.YELLOW);

        for (final Shipment s : siblings) {
            final Color color = colors.removeFirst();
            map.put(s.getId(), color);
            colors.add(color);
        }

        return map;
    }
}