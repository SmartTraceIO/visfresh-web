/**
 *
 */
package au.smarttrace.tt18.st.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;
import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.geolocation.SingleMessageGeoLocationFacade;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LocationResolvingService {
    private static final String SENDER = "tt18";
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    private SingleMessageGeoLocationFacade facade;

    /**
     * Default constructor.
     */
    public LocationResolvingService() {
        super();
    }

    @PostConstruct
    public void initialize() {
        this.facade = SingleMessageGeoLocationFacade.createFacade(jdbc, ServiceType.UnwiredLabs, SENDER);
    }

    @Scheduled(fixedDelay = 10000l)
    public void handleResolvedLocations() {
        facade.processResolvedLocations();
    }

    /**
     * @param req location resolving request.
     */
    public void sendLocationResolvingRequest(final DataWithGsmInfo<DeviceMessage> info) {
        final GeoLocationRequest req = facade.createRequest(info);
        facade.saveRequest(req);
    }
}
