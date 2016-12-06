/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.rules.AbstractRuleEngine;
import com.visfresh.rules.AutoDetectEndLocationRule;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.AutoStartShipmentService;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AutoStartShipmentServiceImpl implements AutoStartShipmentService {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AutoStartShipmentServiceImpl.class);

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private AutoStartShipmentDao autoStartShipmentDao;
    @Autowired
    private AlternativeLocationsDao altLocDao;
    @Autowired
    private AbstractRuleEngine ruleEngine;
    @Autowired
    private ShipmentSessionDao shipmentSessionDao;

    private static class ShipmentInit {
        private AutoStartShipment autoStart;
        private final List<LocationProfile> from = new LinkedList<>();
        private LocationProfile to;

        /**
         * Default constructor.
         */
        public ShipmentInit() {
            super();
        }

        /**
         * @return the autoStart
         */
        public AutoStartShipment getAutoStart() {
            return autoStart;
        }

        /**
         * @param autoStart the autoStart to set
         */
        public void setAutoStart(final AutoStartShipment autoStart) {
            this.autoStart = autoStart;
        }
        /**
         * @return the from
         */
        public List<LocationProfile> getFrom() {
            return from;
        }
        /**
        /**
         * @return the to
         */
        public LocationProfile getTo() {
            return to;
        }
        /**
         * @param to the to to set
         */
        public void setTo(final LocationProfile to) {
            this.to = to;
        }
    }
    /**
     * Default constructor.
     */
    public AutoStartShipmentServiceImpl() {
        super();
    }

    @Override
    public Shipment autoStartNewShipment(final Device device, final Double latitude,
            final Double longitude, final Date shipmentDate) {

        final Shipment last = shipmentDao.findLastShipment(device.getImei());

        ShipmentInit init = null;
        //first of all attempt to select autostart shipment template
        final List<AutoStartShipment> autoStarts = autoStartShipmentDao.findByCompany(
                device.getCompany(), null, null, null);
        Collections.sort(autoStarts);

        if (!autoStarts.isEmpty()) {
            init = createForBestStartLocation(
                    autoStarts, latitude, longitude, device);
            //if not found, create new shipment from most priority template
            if (init == null) {
                init = getFromTemplate(autoStarts.get(0), null, device);
            }
        }

        Shipment shipment;
        if (init != null) {
            shipment = createShipment(init, device);
        } else {
            log.debug("Create new shipment for device " + device.getImei());
            shipment = createNewDefaultShipment(device);
        }

        shipment.setShipmentDate(shipmentDate);

        shipmentDao.save(shipment);
        if (init != null) {
            final AlternativeLocations v = new AlternativeLocations();
            if(init.getFrom().size() > 0) {
                final List<LocationProfile> variants = new LinkedList<>(init.getFrom());
                variants.remove(0);
                v.getFrom().addAll(variants);
            }

            v.getTo().addAll(init.getAutoStart().getShippedTo());

            if (!(v.getFrom().isEmpty() && v.getTo().isEmpty())) {
                altLocDao.save(shipment, v);
            }
            if (!init.getAutoStart().getInterimStops().isEmpty()) {
                ruleEngine.setInterimLocations(shipment, init.getAutoStart().getInterimStops());
            }

            shipmentDao.markAsAutostarted(shipment);
        }

        //close old shipment if need
        if (last != null && !last.hasFinalStatus()) {
            log.debug("Close old active shipment " + last.getShipmentDescription()
                    + " for device " + device.getImei());
            closeOldShipment(last);
        }

        if (init != null) {
            final ShipmentSession session = getShipmentSession(shipment);
            AutoDetectEndLocationRule.needAutodetect(init.getAutoStart(), session);
            shipmentSessionDao.saveSession(session);
        }

        return shipment;
    }

    /**
     * @param shipment
     * @return
     */
    protected ShipmentSession getShipmentSession(final Shipment shipment) {
        ShipmentSession ss = shipmentSessionDao.getSession(shipment);
        if (ss == null) {
            ss = new ShipmentSession(shipment.getId());
            shipmentSessionDao.saveSession(ss);
        }
        return ss;
    }

    /**
     * @param autoStarts list of autostart templates.
     * @param latitude latitude.
     * @param longitude longitude.
     * @param device device.
     * @return shipment.
     */
    private ShipmentInit createForBestStartLocation(final List<AutoStartShipment> autoStarts,
            final Double latitude, final Double longitude, final Device device) {
        //if autostart is assigned to device
        final Long autostartId = device.getAutostartTemplateId();
        if (autostartId != null) {
            //move autostart for given device to front.
            AutoStartShipment auto = null;
            for (final AutoStartShipment a : autoStarts) {
                if (a.getId().equals(autostartId)) {
                    auto = a;
                    break;
                }
            }

            //if found autostart with given ID.
            if (auto != null) {
                return getFromTemplate(auto,
                        getSortedMatchedLocations(auto, latitude, longitude),
                        device);
            }
        }

        //if autostart is not assigned to device.
        //old schema
        for (final AutoStartShipment auto : autoStarts) {
            //if autostart not assigned to device or assigned to given device
            final List<LocationProfile> best = getSortedMatchedLocations(auto, latitude, longitude);
            if (!best.isEmpty()) {
                return getFromTemplate(auto, best, device);
            }
        }

        return null;
    }

    /**
     * @param auto
     * @param startLocation
     * @param device
     * @return
     */
    private ShipmentInit getFromTemplate(final AutoStartShipment auto,
            final List<LocationProfile> startLocation, final Device device) {
        final ShipmentInit init = new ShipmentInit();
        init.setAutoStart(auto);
        if (startLocation != null) {
            init.getFrom().addAll(startLocation);
        }

        //end location detection
        if (auto.getShippedTo().size() == 1) {
            init.setTo(auto.getShippedTo().get(0));
        } else if (auto.getShippedTo().size() > 0) {
            //TODO enable autodetect
        }

        return init;
    }
    /**
     * @param auto
     * @param startLocation
     * @param device
     * @param deviceState
     * @return
     */
    private Shipment createShipment(final ShipmentInit init, final Device device) {
        final ShipmentTemplate tpl = init.getAutoStart().getTemplate();

        final Shipment s = shipmentDao.createNewFrom(tpl);
        s.setStatus(ShipmentStatus.Default);
        s.setDevice(device);
        if (!init.getFrom().isEmpty()) {
            s.setShippedFrom(init.getFrom().get(0));
        }
        s.setShippedTo(init.getTo());
        s.setStartDate(new Date());
        s.setCreatedBy("AutoStart shipment rule");

        if (tpl.getShipmentDescription() != null) {
            s.setShipmentDescription(tpl.getShipmentDescription());
        } else if (tpl.getName() != null) {
            s.setShipmentDescription("Auto created from '" + tpl.getName() + "'");
        } else {
            s.setShipmentDescription("Created by autostart shipment rule");
        }

        if (tpl.isAddDateShipped()) {
            s.setShipmentDescription(s.getShipmentDescription()
                    + " " + DateTimeUtils.formatShipmentDate(tpl.getCompany(), new Date()));
        }

        return s;
    }
    /**
     * @param auto
     * @return
     */
    private List<LocationProfile> getSortedMatchedLocations(final AutoStartShipment auto,
            final Double latitude,
            final Double longitude) {
        if (latitude == null || longitude == null) {
            return new LinkedList<>();
        }

        //get all available location profiles.
        final List<LocationProfile> profiles = new LinkedList<LocationProfile>();
        profiles.addAll(auto.getShippedFrom());
        if (auto.getTemplate().getShippedFrom() != null) {
            profiles.add(auto.getTemplate().getShippedFrom());
        }

        //find best location
        LocationProfile best = null;
        int minDistance = Integer.MAX_VALUE;
        final List<LocationProfile> matches = new LinkedList<>();

        for (final LocationProfile loc : profiles) {
            final int distance = (int) LocationUtils.getDistanceMeters(
                loc.getLocation().getLatitude(),
                loc.getLocation().getLongitude(),
                latitude,
                longitude);

            if (Math.max(0, distance - loc.getRadius()) == 0) {
                matches.add(loc);

                if (distance < minDistance) {
                    best = loc;
                    minDistance = distance;
                }
            }
        }

        //move best location to front
        if (matches.size() > 1) {
            final Iterator<LocationProfile> iter = matches.iterator();
            while (iter.hasNext()) {
                if (iter.next() == best) {
                    iter.remove();
                    matches.add(0, best);
                    break;
                }
            }
        }

        return matches;
    }
    /**
     * @param shipment
     */
    private void closeOldShipment(final Shipment shipment) {
        shipment.setStatus(ShipmentStatus.Ended);
        shipmentDao.save(shipment);
    }
    /**
     * @param device
     */
    private Shipment createNewDefaultShipment(final Device device) {
        final Shipment s = new Shipment();
        s.setCompany(device.getCompany());
        s.setStatus(ShipmentStatus.Default);
        s.setDevice(device);
        s.setShipmentDescription("Created by autostart shipment rule");
        return s;
    }
}
