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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.io.JSonSerializer;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestServiceFacade  {
    private static final String REST_SERVICE = "/rest";

    private JSonSerializer serializer = new JSonSerializer();
    private Gson gson;
    private URL serviceUrl;
    private String authToken;

    private boolean isPrintEnabled = true;

    /**
     * Default constructor.
     */
    public RestServiceFacade() {
        super();
        final GsonBuilder b = new GsonBuilder();
        b.setPrettyPrinting();
        this.gson = b.create();

        serializer.setReferenceResolver(new ReferenceResolver() {
            @Override
            public Shipment getShipment(final Long id) {
                try {
                    return RestServiceFacade.this.getShipment(id);
                } catch (IOException | RestServiceException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            public NotificationSchedule getNotificationSchedule(final Long id) {
                try {
                    return RestServiceFacade.this.getNotificationSchedule(id);
                } catch (IOException | RestServiceException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            public LocationProfile getLocationProfile(final Long id) {
                try {
                    return RestServiceFacade.this.getLocationProfile(id);
                } catch (IOException | RestServiceException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            public Device getDevice(final String id) {
                try {
                    return RestServiceFacade.this.getDevice(id);
                } catch (IOException | RestServiceException e) {
                    return null;
                }
            }
            @Override
            public AlertProfile getAlertProfile(final Long id) {
                try {
                    return RestServiceFacade.this.getAlertProfile(id);
                } catch (IOException | RestServiceException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            /* (non-Javadoc)
             * @see com.visfresh.io.ReferenceResolver#getCompany(java.lang.Long)
             */
            @Override
            public Company getCompany(final Long id) {
                try {
                    return RestServiceFacade.this.getCompany(id);
                } catch (final Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
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
    public void logout(final String authToken) throws RestServiceException, IOException {
        final Map<String, String> params = new HashMap<String, String>();
        sendGetRequest(getPathWithToken(REST_SERVICE, "logout"), params);
    }

    public Long saveAlertProfile(final AlertProfile alert)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveAlertProfile"),
                serializer.toJson(alert)).getAsJsonObject();
        return parseId(e);
    }

    public List<AlertProfile> getAlertProfiles(final int pageIndex, final int pageSize) throws RestServiceException, IOException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getAlertProfiles"),
                params).getAsJsonArray();

        final List<AlertProfile> profiles = new ArrayList<AlertProfile>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseAlertProfile(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }

    public Long saveLocationProfile(final LocationProfile profile)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveLocationProfile"),
                serializer.toJson(profile)).getAsJsonObject();
        return parseId(e);
    }

    /**
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return list of location profiles.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<LocationProfile> getLocationProfiles(final int pageIndex, final int pageSize)
            throws RestServiceException, IOException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getLocationProfiles"),
                params).getAsJsonArray();

        final List<LocationProfile> profiles = new ArrayList<LocationProfile>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseLocationProfile(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }

    public Long saveNotificationSchedule(final NotificationSchedule schedule)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveNotificationSchedule"),
                serializer.toJson(schedule)).getAsJsonObject();
        return parseId(e);
    }

    public List<NotificationSchedule> getNotificationSchedules(final int pageIndex, final int pageSize)
            throws RestServiceException, IOException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getNotificationSchedules"),
                params).getAsJsonArray();

        final List<NotificationSchedule> profiles = new ArrayList<NotificationSchedule>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseNotificationSchedule(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }

    public Long saveShipmentTemplate(final ShipmentTemplate tpl)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveShipmentTemplate"),
                serializer.toJson(tpl)).getAsJsonObject();
        return parseId(e);
    }

    public List<ShipmentTemplate> getShipmentTemplates(final int pageIndex, final int pageSize) throws RestServiceException, IOException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getShipmentTemplates"),
                params).getAsJsonArray();

        final List<ShipmentTemplate> profiles = new ArrayList<ShipmentTemplate>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseShipmentTemplate(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }

    /**
     * @param tr Device.
     */
    public void saveDevice(final Device tr) throws RestServiceException, IOException {
        sendPostRequest(getPathWithToken(REST_SERVICE, "saveDevice"),
                serializer.toJson(tr));
    }
    /**
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return
     */
    public List<Device> getDevices(final int pageIndex, final int pageSize) throws RestServiceException, IOException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));

        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getDevices"),
                params).getAsJsonArray();

        final List<Device> devices = new ArrayList<Device>(response.size());
        for (int i = 0; i < response.size(); i++) {
            devices.add(serializer.parseDevice(response.get(i).getAsJsonObject()));
        }
        return devices;
    }
    /**
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return
     */
    public List<Shipment> getShipments(final int pageIndex, final int pageSize) throws RestServiceException, IOException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getShipments"),
                params).getAsJsonArray();

        final List<Shipment> shipments = new ArrayList<Shipment>(response.size());
        for (int i = 0; i < response.size(); i++) {
            shipments.add(serializer.parseShipment(response.get(i).getAsJsonObject()));
        }
        return shipments;
    }

    /**
     * @param pageIndex page index.
     * @param pageSize page size.
     * @return notifications for given shipment.
     * @throws RestServiceException
     * @throws IOException
     */
    public List<Notification> getNotifications(final int pageIndex, final int pageSize) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));

        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getNotifications"),
                params).getAsJsonArray();
        final List<Notification> notifications = new ArrayList<Notification>(response.size());
        for (int i = 0; i < response.size(); i++) {
            notifications.add(serializer.parseNotification(response.get(i).getAsJsonObject()));
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
        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE, "getUser"),
                params);
        return serializer.parseUser(response);
    }

    /**
     * @param from
     * @param to
     * @param shipment
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public ShipmentData getShipmentData(final Date from, final Date to, final Shipment shipment)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("fromDate", JSonSerializer.formatDate(from));
        params.put("toDate", JSonSerializer.formatDate(to));
        params.put("shipment", shipment.getId().toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE, "getShipmentData"), params);
        return serializer.parseShipmentData(response);
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
                serializer.toJson(req)).getAsJsonObject();
        final SaveShipmentResponse resp = serializer.parseSaveShipmentResponse(e);
        return resp.getShipmentId();
    }
    /**
     * @param u user.
     * @param c company.
     * @param password user password.
     * @throws RestServiceException
     * @throws IOException
     */
    public void createUser(final User u, final Company c, final String password) throws IOException, RestServiceException {
        final CreateUserRequest req = new CreateUserRequest();
        req.setCompany(c);
        req.setUser(u);
        req.setPassword(password);

        sendPostRequest(getPathWithToken(REST_SERVICE, "createUser"),
                serializer.toJson(req));
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
                serializer.toJson(cmd));
    }

    /**
     * @param id company ID.
     * @return Company
     * @throws RestServiceException
     * @throws IOException
     */
    public Company getCompany(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE, "getCompany"),
                params);
        return serializer.parseCompany(response);
    }

    /**
     * @param f JSON factory.
     */
    public void setJsonFactory(final JSonSerializer f) {
        this.serializer = f;
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
    public LocationProfile getLocationProfile(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE, "getLocationProfile"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseLocationProfile(
                response.getAsJsonObject());
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getAlertProfile(java.lang.Long)
     */
    public AlertProfile getAlertProfile(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE, "getAlertProfile"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseAlertProfile(
                response.getAsJsonObject());
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getShipment(java.lang.Long)
     */
    public Shipment getShipment(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE, "getShipment"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseShipment(
                response.getAsJsonObject());
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getNotificationSchedule(java.lang.Long)
     */
    public NotificationSchedule getNotificationSchedule(final Long id)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE,
                "getNotificationSchedule"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseNotificationSchedule(
                response.getAsJsonObject());
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getDevice(java.lang.String)
     */
    public Device getDevice(final String id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE,
                "getDevice"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseDevice(
                response.getAsJsonObject());
    }
    /**
     * @param id
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public ShipmentTemplate getShipmentTemplate(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("id", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE,
                "getShipmentTemplate"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseShipmentTemplate(
                response.getAsJsonObject());
    }
    /**
     * @return user profile.
     * @throws RestServiceException
     * @throws IOException
     */
    public UserProfile getProfile() throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE,
                "getProfile"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseUserProfile(
                response.getAsJsonObject());
    }
    /**
     * @param pageIndex page index.
     * @param pageSize page size
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<Company> getCompanies(final int pageIndex, final int pageSize) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));
        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE,
                "getCompanies"), params).getAsJsonArray();

        final List<Company> result = new LinkedList<Company>();
        for (final JsonElement e : response) {
            result.add(serializer.parseCompany(e));
        }
        return result;
    }
    /**
     * @param p user profile.
     * @throws RestServiceException
     * @throws IOException
     */
    public void saveProfile(final UserProfile p) throws IOException, RestServiceException {
        sendPostRequest(getPathWithToken(REST_SERVICE, "saveProfile"),
                serializer.toJson(p));
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
}
