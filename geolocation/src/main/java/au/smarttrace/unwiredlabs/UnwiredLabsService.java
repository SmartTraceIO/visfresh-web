/**
 *
 */
package au.smarttrace.unwiredlabs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import au.smarttrace.geolocation.GeoLocationDispatcher;
import au.smarttrace.geolocation.GeoLocationService;
import au.smarttrace.geolocation.GeoLocationServiceException;
import au.smarttrace.geolocation.Location;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;
import au.smarttrace.json.ObjectMapperFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UnwiredLabsService implements GeoLocationService {
    private static final Logger log = LoggerFactory.getLogger(UnwiredLabsService.class);

    private String url;
    private String token;
    private static final ObjectMapper objectMapper = ObjectMapperFactory.craeteObjectMapper();

    @Autowired
    private GeoLocationDispatcher dispatcher;

    /**
     * Default constructor.
     */
    @Autowired
    public UnwiredLabsService(final Environment env) {
        super();
        setUrl(env.getProperty("unwiredlabs.url"));
        setToken(env.getProperty("unwiredlabs.token"));
    }
    /**
     * Default constructor.
     */
    protected UnwiredLabsService() {
        super();
    }

    @PostConstruct
    public void initialize() {
        dispatcher.setGeoLocationService(ServiceType.UnwiredLabs, this);
        log.debug("UnwiredLabs service has initialized");
    }
    @PreDestroy
    public void destroy() {
        dispatcher.setGeoLocationService(ServiceType.UnwiredLabs, null);
    }


    /* (non-Javadoc)
     * @see au.smarttrace.geolocation.GeoLocationService#requestLocation(java.lang.String)
     */
    @Override
    public String requestLocation(final String request) throws GeoLocationServiceException {
        try {
            final GsmLocationResolvingRequest req = parseRequest(request);
            final Location loc = getLocation(req.getImei(), req.getRadio(), req.getStations());
            return objectMapper.writeValueAsString(loc);
        } catch (final IOException e) {
            throw new GeoLocationServiceException("Serialization or parsing failed for " + request, e);
        }
    }
    /**
     * @param request
     * @return
     * @throws IOException
     * @throws JsonParseException
     * @throws JsonMappingException
     */
    public static GsmLocationResolvingRequest parseRequest(final String request)
            throws IOException, JsonParseException, JsonMappingException {
        return objectMapper.readValue(
                request, GsmLocationResolvingRequest.class);
    }
    public static String crateGeoLocationRequest(final GsmLocationResolvingRequest r) {
        try {
            return objectMapper.writeValueAsString(r);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.LocationService#getLocation(java.util.List)
     */
    public Location getLocation(final String imei, final String radio, final List<StationSignal> stations)
            throws GeoLocationServiceException {
        if (stations.size() == 0) {
            final GeoLocationServiceException exc = new GeoLocationServiceException(
                    "The number of stations can't be 0");
            exc.setCanRetry(false);
            throw exc;
        }

        final JsonObject req = buildRequest(imei, radio, stations);

        //read response
        String response;
        try {
            //send request.
            final HttpURLConnection con = (HttpURLConnection) new URL(getUrl()).openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setConnectTimeout(10000);
            con.setReadTimeout(15000);

            //write request
            final Writer out = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            try {
                out.write(req.toString());
                out.flush();
            } finally {
                out.close();
            }

            final Reader r = new InputStreamReader(con.getInputStream());
            try {
                response = getContentAsString(r);
            } finally {
                r.close();
            }
        } catch (final Exception e) {
            final GeoLocationServiceException exc = new GeoLocationServiceException(e);
            //parsing error or I/O error should have unlimited retry.
            exc.setNumberOfRetry(Integer.MAX_VALUE);
            throw exc;
        }

        //parse JSON to location object
        Location loc;
        try {
            loc = parseLocation(response);
        } catch (final Exception e) {
            log.error("Failed to parse location from response: " + response, e);
            final GeoLocationServiceException exc = new GeoLocationServiceException(e);
            //James:
            //Thinking over...
            //we need to focus on temperature.
            //If the unwired lookup fails, we should just use null for lat long
            //not wait hrs
            exc.setCanRetry(false);
            throw exc;
        }

        return loc;
    }

    /**
     * @param response
     * @return
     * @throws Exception
     */
    protected Location parseLocation(final String response) throws Exception {
        final JsonObject json = getJson(response);

        //check error
        //{"status":"error","message":"No valid cell IDs or LACs provided","balance":50,"balance_slots":865}
        if (json.has("status") && "error".equals(json.get("status").getAsString())) {
            throw new Exception("UnwiredLabs error: " + json.get("message").getAsString());
        }

        final Location loc = new Location();
        loc.setLatitude(json.get("lat").getAsDouble());
        loc.setLongitude(json.get("lon").getAsDouble());
        return loc;
    }

    /**
     * @param r input stream.
     * @return stream content as string
     * @throws IOException
     */
    private String getContentAsString(final Reader r) throws IOException {
        final StringWriter wr = new StringWriter();
        int len;
        final char[] buff = new char[128];
        while ((len = r.read(buff)) > -1) {
            wr.write(buff, 0, len);
        }

        return wr.toString();
    }

    /**
     * @param text
     * @return
     */
    private JsonObject getJson(final String text) {
        final Reader in = new StringReader(text);
        return (JsonObject) new JsonParser().parse(in);
    }

    /**
     * @param imei TODO
     * @param stations
     * @return
     */
    protected JsonObject buildRequest(final String imei, final String radio, final List<StationSignal> stations) {
        final JsonObject req= new JsonObject();
        //{
        //    "token": "939828b28b7f32",
        //    "radio": "gsm",
        //    "mcc": 310,
        //    "mnc": 410,
        //    "cells": [{
        //        "lac": 7033,
        //        "cid": 17811
        //    }],
        //    "address": 1
        //}
        req.addProperty("id", generateId(imei));
        req.addProperty("token", getToken());
        req.addProperty("radio", radio == null ? "gsm" : radio);
        req.addProperty("mcc", stations.get(0).getMcc());
        req.addProperty("mnc", stations.get(0).getMnc());

        //add cells
        final JsonArray cells = new JsonArray();
        req.add("cells", cells);

        for (final StationSignal s : stations) {
            final JsonObject station = new JsonObject();
            //radio - Radio type of the device (Optional). Type: string; Value: gsm
            //lac - the Location Area Code of your operator's network. Type: integer; Range: 1 to 65533
            station.addProperty("lac", s.getLac());
            //cid - the Cell ID. Type: integer; Range: 0 to 65535
            station.addProperty("cid", s.getCi());
            //mcc - Mobile Country Code of your operator's network (Optional). Type: integer; Range: 0 to 999
            station.addProperty("mcc", s.getMcc());
            //mnc - Mobile Network Code of your operator's network represented by an integer (Optional). Type: integer; Range: 0 to 999.
            station.addProperty("mnc", s.getMnc());
            //signal - the Signal strength (RSSI) of the radio, measured in dBm (Optional). Type: integer; Range: -51 to -113
            station.addProperty("signal", convertToDmb(s.getLevel()));

            cells.add(station);
        }

        req.addProperty("address", 0);

        return req;
    }



    /**
     * @param imei IMEI code for given device.
     * @return uique ID for given device.
     */
    private static String generateId(final String imei) {
        final StringBuilder sb = new StringBuilder();
        final int imeiLength = imei.length();
        for (int i = 1; i < imeiLength - 1; i++) {
            final char ch = imei.charAt(i);
            if (ch >= '0' && ch <='9') {
                final int cipher = ch - '0';
                sb.append((char) ('0' + (9 - cipher)));
            } else {
                sb.append(ch);
            }
        }

        //remove leading '0'
        while (sb.length() > 1 && sb.charAt(0) == '0') {
            sb.delete(0, 1);
        }

        return sb.toString();
    }

    /**
     * @param level signal level.
     * @return signal id decibels
     */
    private int convertToDmb(final int level) {
        return 2 * level - 113;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    /**
     * @param url the url to set
     */
    public void setUrl(final String url) {
        this.url = url;
    }
    /**
     * @param t the token.
     */
    public void setToken(final String t) {
        this.token = t;
    }
    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }
    /**
     * @param jdbc JDBC template.
     * @return UnwiredLabs helper.
     */
    public static UnwiredLabsHelper createHelper(final NamedParameterJdbcTemplate jdbc) {
        return new UnwiredLabsHelper(jdbc);
    }
}
