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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

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
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.io.EntityJSonSerializer;
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

    private EntityJSonSerializer serializer = new EntityJSonSerializer(TimeZone.getDefault());
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
    }
    /**
     * @param referenceResolver
     */
    public void setReferenceResolver(final ReferenceResolver referenceResolver) {
        serializer.setReferenceResolver(referenceResolver);
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
    /**
     * @param p
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteAlertProfile(final AlertProfile p) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("alertProfileId", p.getId().toString());
        sendGetRequest(getPathWithToken(REST_SERVICE, "deleteAlertProfile"), params);
    }

    public List<AlertProfile> getAlertProfiles(final int pageIndex, final int pageSize) throws RestServiceException, IOException {
        return getAlertProfiles(pageIndex, pageSize, null, null);
    }
    /**
     * @param pageIndex
     * @param pageSize
     * @param sortColumn
     * @param sortOrder
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<AlertProfile> getAlertProfiles(final int pageIndex, final int pageSize,
            final String sortColumn,
            final String sortOrder) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));
        if (sortColumn != null) {
            params.put("sc", sortColumn);
        }
        if (sortOrder != null) {
            params.put("so", sortOrder);
        }

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
        final JsonObject e = sendPostRequest(getPathWithToken(REST_SERVICE, "saveLocation"),
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
        return getLocationProfiles(pageIndex, pageSize, null, null);
    }
    /**
     * @param pageIndex
     * @param pageSize
     * @param sortColumn
     * @param sortOrder
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<LocationProfile> getLocationProfiles(final int pageIndex, final int pageSize,
            final String sortColumn, final String sortOrder) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));
        if (sortColumn != null) {
            params.put("sc", sortColumn);
        }
        if (sortOrder != null) {
            params.put("so", sortOrder);
        }

        final JsonArray response = sendGetRequest(getPathWithToken(REST_SERVICE, "getLocations"),
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
        return getNotificationSchedules(pageIndex, pageSize, null, null);
    }
    /**
     * @param pageIndex
     * @param pageSize
     * @param sortColumn
     * @param sortOrder
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public List<NotificationSchedule> getNotificationSchedules(final int pageIndex, final int pageSize,
            final String sortColumn, final String sortOrder) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));
        if (sortColumn != null) {
            params.put("sc", sortColumn);
        }
        if (sortOrder != null) {
            params.put("so", sortOrder);
        }
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
    public JsonArray getShipments(final int pageIndex, final int pageSize)
            throws RestServiceException, IOException {
        return getShipments(pageIndex, pageSize, null, null, null, null, null);
    }
    /**
     * @param pageIndex
     * @param pageSize
     * @param shippedFrom
     * @param shippedTo
     * @param goods
     * @param device
     * @param status
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public JsonArray getShipments(final int pageIndex, final int pageSize, final Long shippedFrom,
            final Long shippedTo, final String goods, final String device, final ShipmentStatus status)
                    throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", Integer.toString(pageIndex));
        params.put("pageSize", Integer.toString(pageSize));

        if (shippedFrom != null) {
            params.put("shippedFrom", shippedFrom.toString());
        }
        if (shippedTo != null) {
            params.put("shippedTo", shippedTo.toString());
        }
        if (goods != null) {
            params.put("goods", goods.toString());
        }
        if (device != null) {
            params.put("device", device.toString());
        }
        if (status != null) {
            params.put("status", status.toString());
        }

        return sendGetRequest(getPathWithToken(REST_SERVICE, "getShipments"),
                params).getAsJsonArray();
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
    public JsonElement getSingleShipment(final Shipment shipment, final Date from, final Date to)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("fromDate", serializer.formatDate(from));
        params.put("toDate", serializer.formatDate(to));
        params.put("shipment", shipment.getId().toString());

        return sendGetRequest(getPathWithToken(REST_SERVICE, "getSingleShipment"), params);
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
        params.put("companyId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE, "getCompany"),
                params);
        return serializer.parseCompany(response);
    }

    /**
     * @param f JSON factory.
     */
    public void setJsonFactory(final EntityJSonSerializer f) {
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
    public LocationProfile getLocation(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("locationId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE, "getLocation"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseLocationProfile(
                response.getAsJsonObject());
    }
    /**
     * @param id
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteLocation(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("locationId", id.toString());

        sendGetRequest(getPathWithToken(REST_SERVICE, "deleteLocation"), params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getAlertProfile(java.lang.Long)
     */
    public AlertProfile getAlertProfile(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("alertProfileId", id.toString());

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
        params.put("shipmentId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE, "getShipment"),
                params);
        return response == JsonNull.INSTANCE ? null : serializer.parseShipment(
                response.getAsJsonObject());
    }
    /**
     * @param id
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteShipment(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentId", id.toString());
        sendGetRequest(getPathWithToken(REST_SERVICE, "deleteShipment"), params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getNotificationSchedule(java.lang.Long)
     */
    public NotificationSchedule getNotificationSchedule(final Long id)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("notificationScheduleId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE,
                "getNotificationSchedule"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseNotificationSchedule(
                response.getAsJsonObject());
    }
    /**
     * @param id
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteNotificationSchedule(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("notificationScheduleId", id.toString());

        sendGetRequest(getPathWithToken(REST_SERVICE, "deleteNotificationSchedule"), params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getDevice(java.lang.String)
     */
    public Device getDevice(final String id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("imei", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE,
                "getDevice"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseDevice(
                response.getAsJsonObject());
    }
    /**
     * @param p device to delete.
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteDevice(final Device p) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("imei", p.getId());
        sendGetRequest(getPathWithToken(REST_SERVICE, "deleteDevice"), params);
    }
    /**
     * @param id
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public ShipmentTemplate getShipmentTemplate(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentTemplateId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken(REST_SERVICE,
                "getShipmentTemplate"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseShipmentTemplate(
                response.getAsJsonObject());
    }
    /**
     * @param id
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteShipmentTemplate(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("shipmentTemplateId", id.toString());
        sendGetRequest(getPathWithToken(REST_SERVICE, "deleteShipmentTemplate"), params);
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
        //according meeting can have name not only 'id'
        final Set<Entry<String, JsonElement>> set = e.entrySet();
        if (set.size() != 1) {
            throw new RuntimeException("Unexpected ID format: " + e);
        }
        return set.iterator().next().getValue().getAsLong();
    }
}
