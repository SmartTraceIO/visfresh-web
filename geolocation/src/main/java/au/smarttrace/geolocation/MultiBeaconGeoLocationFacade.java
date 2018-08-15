/**
 *
 */
package au.smarttrace.geolocation;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.geolocation.impl.BeaconChannelUnlocker;
import au.smarttrace.geolocation.impl.dao.BeaconChannelLockDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MultiBeaconGeoLocationFacade extends FastXmlGeoLocationFacade<MultiBeaconMessage>{
    private static final Logger log = LoggerFactory.getLogger(MultiBeaconGeoLocationFacade.class);
    private final BeaconChannelLockDao beaconChannelLockDao;

    /**
     * @param jdbc
     * @param type
     * @param sender
     */
    public MultiBeaconGeoLocationFacade(final NamedParameterJdbcTemplate jdbc,
            final ServiceType type, final String sender) {
        super(jdbc, type, MultiBeaconMessage.class, sender);
        beaconChannelLockDao = new BeaconChannelLockDao(jdbc);
    }
    public void processResolvedLocations() {
        super.processResolvedLocations((msg, loc, status) -> {processResolvedMessage(msg, loc, status);});
    }
    /**
     * @param msg
     * @param loc
     * @param status TODO
     * @return
     */
    public void processResolvedMessage(final MultiBeaconMessage msg,
            final Location loc, final RequestStatus status) {
        //set location to gateway device
        final DeviceMessage gateway = msg.getGatewayMessage();
        if (gateway != null) {
            gateway.setLocation(loc);
        }

        //set location to beacon.
        for (final DeviceMessage m : msg.getBeacons()) {
            m.setLocation(loc);
        }

        processResolvedMessage(msg, status);
    }
    /**
     * @param msg
     * @param status
     */
    public void processResolvedMessage(final MultiBeaconMessage msg, final RequestStatus status) {
        //send gateway message.
        final DeviceMessage gateway = msg.getGatewayMessage();
        if (gateway != null) {
            sendResolvedMessagesFor(gateway);
        }

        final Set<String> beacons = lockChannels(getOnlyBeaconImeis(msg.getBeacons()), msg.getGateway());
        for (final DeviceMessage m : msg.getBeacons()) {
            if (m.getGateway() == null || beacons.contains(m.getImei())) {
                sendResolvedMessagesFor(m);
            } else {
                log.debug("Beacon " + m.getImei() + " channel has locked by another gateway");
            }
        }
    }
    /**
     * @param msgs messages.
     * @return set of IMEI.
     */
    private Set<String> getOnlyBeaconImeis(final List<DeviceMessage> msgs) {
        final Set<String> imeis = new HashSet<>();
        for (final DeviceMessage m : msgs) {
            if (m.getGateway() != null) {
                imeis.add(m.getImei());
            }
        }
        return imeis;
    }
    /**
     * @param jdbc JDBC helper.
     * @param type service type.
     * @param sender request sender.
     * @return facade.
     */
    public static MultiBeaconGeoLocationFacade createFacade(final NamedParameterJdbcTemplate jdbc,
            final ServiceType type, final String sender) {
        return new MultiBeaconGeoLocationFacade(jdbc, type, sender);
    }
    /**
     * @param beacons set of beacon IMEI.
     * @param gateway gateway device.
     * @return set of successfully locked beacon channels.
     */
    public Set<String> lockChannels(final Set<String> beacons, final String gateway) {
        final Date t = new Date(System.currentTimeMillis() + BeaconChannelUnlocker.CHANNEL_IDLE_TIME);
        beaconChannelLockDao.createOrUpdateLocks(beacons, gateway, t);
        return beaconChannelLockDao.getLocked(gateway, new Date(t.getTime() - 3000l));
    }
}
