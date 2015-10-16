/**
 *
 */
package com.visfresh.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.io.JSonSerializer;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestServiceFacade implements ReferenceResolver {
    private static final String REST_SERVICE = "/rest";

    private JSonSerializer jsonFactory = new JSonSerializer();
    private Gson gson;
    private URL serviceUrl;
    private String authToken;

    private boolean isPrintEnabled = false;

    /**
     * Default constructor.
     */
    public RestServiceFacade() {
        super();
        final GsonBuilder b = new GsonBuilder();
        b.setPrettyPrinting();
        this.gson = b.create();
        jsonFactory.setReferenceResolver(this);
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
    public String login(final String login, final String password) throws RestServiceException, IOException {
        final JsonObject req = new JsonObject();
        req.addProperty("login", login);
        req.addProperty("password", password);

        final JsonObject response = sendPostRequest(REST_SERVICE + "/login", req).getAsJsonObject();
        return parseAuthToken(response);
    }

    /**
     * @param authToken
     * @throws IOException
     */
    public void logout(final String authToken) throws RestServiceException, IOException {
        final Map<String, String> params = new HashMap<String, String>();
        sendGetRequest(getPathWithToken(REST_SERVICE, "logout"), params);
    }

    public Long saveAlertProfile(final AlertProfile alert)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveAlertProfile"),
                jsonFactory.toJson(alert)).getAsJsonObject();
        return parseId(e);
    }

    public List<AlertProfile> getAlertProfiles() throws RestServiceException, IOException {
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getAlertProfiles"),
                new HashMap<String, String>()).getAsJsonArray();

        final List<AlertProfile> profiles = new ArrayList<AlertProfile>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(jsonFactory.parseAlertProfile(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }

    public Long saveLocationProfile(final LocationProfile profile)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveLocationProfile"),
                jsonFactory.toJson(profile)).getAsJsonObject();
        return parseId(e);
    }

    public List<LocationProfile> getLocationProfiles()
            throws RestServiceException, IOException {
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getLocationProfiles"),
                new HashMap<String, String>()).getAsJsonArray();

        final List<LocationProfile> profiles = new ArrayList<LocationProfile>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(jsonFactory.parseLocationProfile(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }

    public Long saveNotificationSchedule(final NotificationSchedule schedule)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveNotificationSchedule"),
                jsonFactory.toJson(schedule)).getAsJsonObject();
        return parseId(e);
    }

    public List<NotificationSchedule> getNotificationSchedules()
            throws RestServiceException, IOException {
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getNotificationSchedules"),
                new HashMap<String, String>()).getAsJsonArray();

        final List<NotificationSchedule> profiles = new ArrayList<NotificationSchedule>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(jsonFactory.parseNotificationSchedule(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }

    public Long saveShipmentTemplate(final ShipmentTemplate tpl)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveShipmentTemplate"),
                jsonFactory.toJson(tpl)).getAsJsonObject();
        return parseId(e);
    }

    public List<ShipmentTemplate> getShipmentTemplates() throws RestServiceException, IOException {
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getShipmentTemplates"),
                new HashMap<String, String>()).getAsJsonArray();

        final List<ShipmentTemplate> profiles = new ArrayList<ShipmentTemplate>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(jsonFactory.parseShipmentTemplate(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }

    /**
     * @param tr Device.
     */
    public void saveDevice(final Device tr) throws RestServiceException, IOException {
        sendPostRequest(getPathWithToken(REST_SERVICE, "saveDevice"),
                jsonFactory.toJson(tr));
    }
    /**
     * @return
     */
    public List<Device> getDevices() throws RestServiceException, IOException {
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getDevices"),
                new HashMap<String, String>()).getAsJsonArray();

        final List<Device> devices = new ArrayList<Device>(response.size());
        for (int i = 0; i < response.size(); i++) {
            devices.add(jsonFactory.parseDevice(response.get(i).getAsJsonObject()));
        }
        return devices;
    }
    /**
     * @return
     */
    public List<Shipment> getShipments() throws RestServiceException, IOException {
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getShipments"),
                new HashMap<String, String>()).getAsJsonArray();

        final List<Shipment> shipments = new ArrayList<Shipment>(response.size());
        for (int i = 0; i < response.size(); i++) {
            shipments.add(jsonFactory.parseShipment(response.get(i).getAsJsonObject()));
        }
        return shipments;
    }

    /**
     * @return notifications for given shipment.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<Notification> getNotifications() throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getNotifications"),
                params).getAsJsonArray();
        final List<Notification> notifications = new ArrayList<Notification>(response.size());
        for (int i = 0; i < response.size(); i++) {
            notifications.add(jsonFactory.parseNotification(response.get(i).getAsJsonObject()));
        }
        return notifications;
    }

    /**
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public String getToken() throws IOException, RestServiceException {
        final JsonObject response = sendGetRequest(REST_SERVICE + "/getToken",
                new HashMap<String, String>()).getAsJsonObject();
        return parseAuthToken(response);
    }
    /**
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public String refreshToken() throws IOException, RestServiceException {
        final JsonObject response = sendGetRequest(getPathWithToken(REST_SERVICE, "refreshToken"),
                new HashMap<String, String>()).getAsJsonObject();
        return parseAuthToken(response);
    }
    /**
     * @param userName
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public User getUser(final String userName) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", userName);
        final JsonObject response = sendGetRequest(getPathWithToken(REST_SERVICE, "getUser"),
                params).getAsJsonObject();
        return jsonFactory.parseUser(response);
    }

    /**
     * @param from
     * @param to
     * @param onlyWithAlerts
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<ShipmentData> getShipmentData(final Date from, final Date to, final boolean onlyWithAlerts)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("fromDate", JSonSerializer.formatDate(from));
        params.put("toDate", JSonSerializer.formatDate(to));
        params.put("onlyWithAlerts", Boolean.toString(onlyWithAlerts));

        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getShipmentData"),
                params).getAsJsonArray();
        final List<ShipmentData> result = new LinkedList<ShipmentData>();
        for (final JsonElement e : response) {
            result.add(jsonFactory.parseShipmentData(e.getAsJsonObject()));
        }

        return result;
    }

    /**
     * @param shipment
     * @param templateName
     * @param saveTemplate
     */
    public Long saveShipment(final Shipment shipment, final String templateName,
            final boolean saveTemplate) throws RestServiceException, IOException {
        final SaveShipmentRequest req = new SaveShipmentRequest();
        req.setShipment(shipment);
        req.setTemplateName(templateName);
        req.setSaveAsNewTemplate(saveTemplate);

        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveShipment"),
                jsonFactory.toJson(req)).getAsJsonObject();
        final SaveShipmentResponse resp = jsonFactory.parseSaveShipmentResponse(e);
        return resp.getShipmentId();
    }

    /**
     * @param toReaden
     * @throws RestServiceException
     * @throws IOException
     */
    public void markNotificationsAsRead(final List<Notification> toReaden) throws IOException, RestServiceException {
        final JsonArray req = new JsonArray();
        for (final Notification n : toReaden) {
            req.add(new JsonPrimitive(n.getId()));
        }

        sendPostRequest(getPathWithToken(REST_SERVICE, "markNotificationsAsRead"), req);
    }
    /**
     * @param device device.
     * @param command device specific command.
     * @throws IOException
     * @throws RestServiceException
     */
    public void sendCommandToDevice(final Device device, final String command) throws IOException, RestServiceException {
        final DeviceCommand cmd = new DeviceCommand();
        cmd.setDevice(device);
        cmd.setCommand(command);

        sendPostRequest(getPathWithToken(REST_SERVICE, "sendCommandToDevice"),
                jsonFactory.toJson(cmd));
    }

    /**
     * @param f JSON factory.
     */
    public void setJsonFactory(final JSonSerializer f) {
        this.jsonFactory = f;
    }

    /**
     * @param path URL path.
     * @param params request parameters.
     * @return JSON response.
     * @throws IOException
     * @throws RestServiceException
     */
    private JsonElement sendGetRequest(final String path, final Map<String, String> params)
            throws IOException, RestServiceException {
        final StringBuilder urlString = new StringBuilder(
                getServiceUrl().toExternalForm() + path);

        if (params.size() > 0) {
            urlString.append('?');

            boolean first = true;
            for (final Map.Entry<String, String> e : params.entrySet()) {
                if (!first) {
                    urlString.append('&');
                } else {
                    first = false;
                }

                urlString.append(urlEncode(e.getKey()));
                urlString.append('=');
                urlString.append(urlEncode(e.getValue()));
            }
        }

        println("GET " + urlString);

        final URLConnection con = new URL(urlString.toString()).openConnection();
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
    private JsonElement sendPostRequest(final String path, final JsonElement json) throws IOException, RestServiceException {
        final StringBuilder urlString = new StringBuilder(
                getServiceUrl().toExternalForm() + path);

        println("POST " + urlString);
        final URLConnection con = new URL(urlString.toString()).openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.setDoInput(true);

        final Writer wr = new OutputStreamWriter(con.getOutputStream());
        try {
            final String requestBody = json.toString();
            println("Request body:");
            println(gson.toJson(json));
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
    public void setServiceUrl(final URL serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
    /**
     * @param con
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    protected JsonElement parseResponse(final URLConnection con)
            throws IOException, RestServiceException {
        final String response = getContent(con.getInputStream());
        final JsonObject e = new JsonParser().parse(response).getAsJsonObject();

        printJsonPretty(e);

        checkError(e);
        return e.get("response");
    }

    /**
     * @param e GSON element.
     */
    private void printJsonPretty(final JsonElement e) {
        println("Response:");
        println(gson.toJson(e));
    }

    /**
     * @param str
     */
    private void println(final String str) {
        if (isPrintEnabled) {
            System.out.println(str);
        }
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
     * @param servicePath
     * @param methodPath
     * @return
     * @throws RestServiceException
     */
    private String getPathWithToken(final String servicePath, final String methodPath) throws RestServiceException {
        return servicePath + "/" + methodPath + "/" + getAuthToken();
    }

    /**
     * @return
     */
    public String getAuthToken() {
        return authToken;
    }
    /**
     * @param authToken the authToken to set
     */
    public void setAuthToken(final String authToken) {
        this.authToken = authToken;
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getLocationProfile(java.lang.Long)
     */
    @Override
    public LocationProfile getLocationProfile(final Long id) {
        try {
            return getById(getLocationProfiles(), id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getAlertProfile(java.lang.Long)
     */
    @Override
    public AlertProfile getAlertProfile(final Long id) {
        try {
            return getById(getAlertProfiles(), id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getShipment(java.lang.Long)
     */
    @Override
    public Shipment getShipment(final Long id) {
        try {
            return getById(getShipments(), id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getNotificationSchedule(java.lang.Long)
     */
    @Override
    public NotificationSchedule getNotificationSchedule(final Long id) {
        try {
            return getById(getNotificationSchedules(), id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
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
    private Long parseId(final JsonObject e) {
        return e.get("id").getAsLong();
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getDevice(java.lang.String)
     */
    @Override
    public Device getDevice(final String id) {
        if (id == null) {
            return null;
        }

        try {
            final List<Device> Devices = getDevices();
            for (final Device t : Devices) {
                if (id.equals(t.getId())) {
                    return t;
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }
    /**
     * @param list
     * @param id entity ID.
     * @return
     */
    private <M extends EntityWithId> M getById(final List<M> list, final Long id) {
        if (id == null) {
            return null;
        }

        for (final M m : list) {
            if (id.equals(m.getId())) {
                return m;
            }
        }
        return null;
    }
}
