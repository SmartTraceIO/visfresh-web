/**
 *
 */
package au.smarttrace.geolocation;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleMessageGeoLocationFacade extends FastXmlGeoLocationFacade<DeviceMessage>{
    /**
     * @param jdbc
     * @param type
     * @param sender
     */
    public SingleMessageGeoLocationFacade(final NamedParameterJdbcTemplate jdbc,
            final ServiceType type, final String sender) {
        super(jdbc, type, DeviceMessage.class, sender);
    }

    public void processResolvedLocations() {
        super.processResolvedLocations((msg, loc, status) -> processResolvedMessage(msg, loc));
    }
    /**
     * @param msg
     * @param loc
     * @return
     */
    private Void processResolvedMessage(final DeviceMessage msg, final Location loc) {
        msg.setLocation(loc);
        sendResolvedMessagesFor(msg);
        return null;
    }
    /**
     * @param jdbc JDBC helper.
     * @param type service type.
     * @param sender request sender.
     * @return facade.
     */
    public static SingleMessageGeoLocationFacade createFacade(final NamedParameterJdbcTemplate jdbc,
            final ServiceType type, final String sender) {
        return new SingleMessageGeoLocationFacade(jdbc, type, sender);
    }
}
