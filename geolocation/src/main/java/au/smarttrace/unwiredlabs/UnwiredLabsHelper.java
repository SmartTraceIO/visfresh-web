/**
 *
 */
package au.smarttrace.unwiredlabs;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.GeoLocationRequestDao;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.json.ObjectMapperFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UnwiredLabsHelper {
    private GeoLocationRequestDao dao;
    private ObjectMapper mapper = ObjectMapperFactory.craeteObjectMapper();

    protected UnwiredLabsHelper(final NamedParameterJdbcTemplate jdbc) {
        super();
        dao = GeoLocationRequestDao.Factory.create(jdbc);
    }

    public void saveRequest(final String sender, final String userData, final GsmLocationResolvingRequest gsm) {
        final GeoLocationRequest req = new GeoLocationRequest();
        try {
            req.setBuffer(mapper.writeValueAsString(gsm));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        req.setSender(sender);
        req.setType(ServiceType.UnwiredLabs);
        req.setUserData(userData);
        dao.saveRequest(req);
    }
    public List<GeoLocationRequest> getProcessedRequests(final String sender) {
        return dao.getProcessedRequests(sender);
    }
}
