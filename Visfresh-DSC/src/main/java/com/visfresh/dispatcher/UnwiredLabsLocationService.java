/**
 *
 */
package com.visfresh.dispatcher;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.Location;
import com.visfresh.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UnwiredLabsLocationService implements LocationService {
    private String url;
    private String token;
    /**
     * Default constructor.
     */
    public UnwiredLabsLocationService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.LocationService#getLocation(java.util.List)
     */
    @Override
    public Location getLocation(final List<StationSignal> stations) throws RetryableException {
        if (stations.size() == 0) {
            final RetryableException exc = new RetryableException("The number of stations can't be 0");
            exc.setCanRetry(false);
            throw exc;
        }

        final JsonObject req = buildRequest(stations);

        //read response
        String response;
        try {
            //send request.
            final URLConnection con = new URL(getUrl()).openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);

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
            throw new RetryableException(e);
        }

        //parse JSON to location object
        final JsonObject json = getJson(response);

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
     * @param stations
     * @return
     */
    protected JsonObject buildRequest(final List<StationSignal> stations) {
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
        req.addProperty("token", getToken());
        req.addProperty("radio", "gsm");
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
    @Value("${unwiredlabs.url}")
    public void setUrl(final String url) {
        this.url = url;
    }
    /**
     * @param t the token.
     */
    @Value("${unwiredlabs.token}")
    public void setToken(final String t) {
        this.token = t;
    }
    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }
}
