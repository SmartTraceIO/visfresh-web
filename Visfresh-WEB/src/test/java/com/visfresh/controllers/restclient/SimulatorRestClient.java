/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.visfresh.entities.User;
import com.visfresh.io.SimulatorDto;
import com.visfresh.io.StartSimulatorRequest;
import com.visfresh.io.json.SimulatorSerializer;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SimulatorRestClient extends RestClient {
    private SimulatorSerializer serializer;
    private final DateFormat fmt;

    /**
     * @param user user.
     */
    public SimulatorRestClient(final User user) {
        serializer = new SimulatorSerializer(user);
        fmt = DateTimeUtils.createIsoFormat(user);
    }

    /**
     * @param dto simulator.
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public String saveSimulator(final SimulatorDto dto) throws IOException, RestServiceException {
        return parseStringId(sendPostRequest(getPathWithToken("saveSimulator"),
                serializer.toJson(dto)).getAsJsonObject());
    }

    /**
     * @param user user.
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteSimulator(final User user) throws IOException, RestServiceException {
        final Map<String, String> params = new HashMap<>();
        if (user != null) {
            params.put("user", user.getEmail());
        }
        sendGetRequest(getPathWithToken("deleteSimulator"), params);
    }

    /**
     * @param user the user.
     * @throws RestServiceException
     * @throws IOException
     */
    public void stopSimulator(final User user) throws IOException, RestServiceException {
        final Map<String, String> params = new HashMap<>();
        if (user != null) {
            params.put("user", user.getEmail());
        }
        sendGetRequest(getPathWithToken("stopSimulator"), params);
    }

    /**
     * @param user user.
     * @param startDate start date.
     * @param endDate end date.
     * @param velosity TODO
     * @throws RestServiceException
     * @throws IOException
     */
    public void startSimulator(final User user, final Date startDate, final Date endDate, final int velosity)
            throws IOException, RestServiceException {
        final StartSimulatorRequest req = new StartSimulatorRequest();
        if (user != null) {
            req.setUser(user.getEmail());;
        }
        if (startDate != null) {
            req.setStartDate(formatDate(startDate));
        }
        if (endDate != null) {
            req.setEndDate(formatDate(endDate));
        }
        req.setVelosity(velosity);
        sendPostRequest(getPathWithToken("startSimulator"), serializer.toJson(req));
     }

    /**
     * @param date
     * @return
     */
    public String formatDate(final Date date) {
        return fmt.format(date);
    }
}
