/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestClient  {
    private static final String REST_SERVICE = "/rest";

    private Gson gson;
    private URL serviceUrl;
    private String authToken;

    private final List<RestIoListener> listeners = new LinkedList<RestIoListener>();

    /**
     * Default constructor.
     */
    public RestClient() {
        super();
        final GsonBuilder b = new GsonBuilder();
        b.setPrettyPrinting();
        b.disableHtmlEscaping();
        this.gson = b.create();

        //add logging
        addRestIoListener(new RestIoLogger());
    }
    /**
     * @param login
     *            login.
     * @param password
     *            password.
     * @return authentication token.
     * @throws IOException
     * @throws AuthenticationException
     */
    public final String login(final String login, final String password) throws RestServiceException, IOException {
        final Map<String, String> req = new HashMap<String, String>();
        req.put("login", login);
        req.put("password", password);

        final JsonObject response = sendGetRequest(REST_SERVICE + "/login", req).getAsJsonObject();
        return parseAuthToken(response);
    }

    /**
     * @param authToken
     * @throws IOException
     */
    public final void logout(final String authToken) throws RestServiceException, IOException {
        final Map<String, String> params = new HashMap<String, String>();
        sendGetRequest(getPathWithToken("logout"), params);
    }
    /**
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public final String getToken() throws IOException, RestServiceException {
        final JsonObject response = sendGetRequest(REST_SERVICE + "/getToken",
                new HashMap<String, String>()).getAsJsonObject();
        return parseAuthToken(response);
    }
    /**
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public final String refreshToken() throws IOException, RestServiceException {
        final JsonObject response = sendGetRequest(getPathWithToken("refreshToken"),
                new HashMap<String, String>()).getAsJsonObject();
        return parseAuthToken(response);
    }
    /**
     * @param path URL path.
     * @param params request parameters.
     * @return JSON response.
     * @throws IOException
     * @throws RestServiceException
     */
    public final JsonElement sendGetRequest(final String path, final Map<String, String> params)
            throws IOException, RestServiceException {
        final StringBuilder sb = new StringBuilder(
                getServiceUrl().toExternalForm() + path);

        if (params.size() > 0) {
            sb.append('?');

            boolean first = true;
            for (final Map.Entry<String, String> e : params.entrySet()) {
                if (!first) {
                    sb.append('&');
                } else {
                    first = false;
                }

                sb.append(urlEncode(e.getKey()));
                sb.append('=');
                sb.append(urlEncode(e.getValue()));
            }
        }

        final String url = sb.toString();
        fireRequestSending(url, "GET", null);

        final URLConnection con = new URL(url).openConnection();
        con.setDoOutput(false);
        con.setDoInput(true);

        return parseResponse(con);
    }

    /**
     * @param path path.
     * @param json JSON request body.
     * @return JSON response.
     * @throws RestServiceException
     */
    public final JsonElement sendPostRequest(final String path, final JsonElement json) throws IOException, RestServiceException {
        final StringBuilder sb = new StringBuilder(
                getServiceUrl().toExternalForm() + path);

        final String url = sb.toString();
        final URLConnection con = new URL(url).openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.setDoInput(true);

        final Writer wr = new OutputStreamWriter(con.getOutputStream());
        try {
            final String requestBody = gson.toJson(json);
            //notify listeners
            fireRequestSending(url, "POST", requestBody);
            wr.write(requestBody);
            wr.flush();
        } finally {
            wr.close();
        }

        return parseResponse(con);
    }
    /**
     * @return
     */
    private URL getServiceUrl() {
        return serviceUrl;
    }
    /**
     * @param serviceUrl the serviceUrl to set
     */
    public final void setServiceUrl(final URL serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
    /**
     * @param con
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    public final JsonElement parseResponse(final URLConnection con)
            throws IOException, RestServiceException {
        final String response = getContent(con.getInputStream());
        //notify listeners
        fireResponseReceived(response);

        final JsonObject e = new JsonParser().parse(response).getAsJsonObject();
        checkError(e);
        return e.get("response");
    }
    /**
     * @param response JSON response.
     * @throws RestServiceException
     */
    private void checkError(final JsonObject response) throws RestServiceException {
        final JsonObject status = response.get("status").getAsJsonObject();
        final int code = status.get("code").getAsInt();
        final String message = status.get("message").getAsString();

        if (code != 0) {
            throw new RestServiceException(code, message);
        }
    }

    /**
     * @param in input stream.
     * @return content of stream as string.
     */
    private String getContent(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        final byte[] buff = new byte[256];
        while ((len = in.read(buff)) > -1) {
            out.write(buff, 0, len);
        }

        return new String(out.toByteArray(), "UTF-8");
    }

    /**
     * @param str string to encode.
     * @return URL encoded string.
     */
    private String urlEncode(final String str) throws IOException {
        return URLEncoder.encode(str, "UTF-8");
    }
    /**
     * @param methodPath
     * @return
     * @throws RestServiceException
     */
    protected String getPathWithToken(final String methodPath) throws RestServiceException {
        return REST_SERVICE + "/" + methodPath + "/" + getAuthToken();
    }

    /**
     * @return
     */
    public final String getAuthToken() {
        return authToken;
    }
    /**
     * @param authToken the authToken to set
     */
    public final void setAuthToken(final String authToken) {
        this.authToken = authToken;
    }
    /**
     * @param response
     * @return
     */
    private String parseAuthToken(final JsonObject response) {
        return response.get("token").getAsString();
    }
    /**
     * @param e JSON object.
     * @return ID attribute.
     */
    protected Long parseId(final JsonObject e) {
        //according meeting can have name not only 'id'
        final Set<Entry<String, JsonElement>> set = e.entrySet();
        if (set.size() != 1) {
            throw new RuntimeException("Unexpected ID format: " + e);
        }
        return set.iterator().next().getValue().getAsLong();
    }
    public void addRestIoListener(final RestIoListener l) {
        listeners.add(l);
    }
    public void removeRestIoListener(final RestIoListener l) {
        listeners.remove(l);
    }
    /**
     * @param response
     */
    protected void fireResponseReceived(final String response) {
        for (final RestIoListener l : listeners) {
            l.receivedResponse(response);
        }
    }
    /**
     * @param url URL string.
     * @param method method name.
     * @param requestBody request body in case of POST request.
     */
    protected void fireRequestSending(final String url, final String method, final String requestBody) {
        for (final RestIoListener l : listeners) {
            l.sendingRequest(url, requestBody, method);
        }
    }
}
