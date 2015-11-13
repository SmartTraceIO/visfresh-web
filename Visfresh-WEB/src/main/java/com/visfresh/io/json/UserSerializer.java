/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.UserConstants;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.services.lists.ListUserItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserSerializer extends AbstractJsonSerializer {
    private ShipmentResolver shipmentResolver;
    private CompanyResolver companyResolver;

    /**
     * Default constructor.
     */
    public UserSerializer(final TimeZone tz) {
        super(tz);
    }

    /**
     * @param json
     * @return
     */
    public User parseUser(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final User u = new User();
        u.setLogin(asString(json.get(UserConstants.PROPERTY_LOGIN)));
        u.setFullName(asString(json.get(UserConstants.PROPERTY_FULL_NAME)));
        u.setTimeZone(TimeZone.getTimeZone(asString(json.get(UserConstants.PROPERTY_TIME_ZONE))));
        u.setTemperatureUnits(TemperatureUnits.valueOf(asString(json.get(
                UserConstants.PROPERTY_TEMPERATURE_UNITS))));

        final JsonArray array = json.get(UserConstants.PROPERTY_ROLES).getAsJsonArray();
        final int size = array.size();
        for (int i = 0; i < size; i++) {
            u.getRoles().add(Role.valueOf(array.get(i).getAsString()));
        }

        return u;
    }
    /**
     * @param u the user.
     * @return JSON object.
     */
    public JsonObject toJson(final User u) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(UserConstants.PROPERTY_LOGIN, u.getLogin());
        obj.addProperty(UserConstants.PROPERTY_FULL_NAME, u.getFullName());

        final JsonArray roleArray = new JsonArray();
        for (final Role r : u.getRoles()) {
            roleArray.add(new JsonPrimitive(r.name()));
        }
        obj.add(UserConstants.PROPERTY_ROLES, roleArray);

        obj.addProperty(UserConstants.PROPERTY_TIME_ZONE, u.getTimeZone().getID());
        obj.addProperty(UserConstants.PROPERTY_TEMPERATURE_UNITS, u.getTemperatureUnits().toString());

        return obj;
    }
    /**
     * @param profile user profile.
     * @return User pforile as JSON object.
     */
    public JsonElement toJson(final UserProfile profile) {
        if (profile == null) {
            return JsonNull.INSTANCE;
        }
        final JsonObject obj = new JsonObject();

        final JsonArray array = new JsonArray();
        for (final Shipment s : profile.getShipments()) {
            array.add(new JsonPrimitive(s.getId()));
        }

        obj.add("shipments", array);
        return obj;
    }
    public UserProfile parseUserProfile(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final UserProfile p = new UserProfile();
        final JsonArray array = ((JsonObject) e).get("shipments").getAsJsonArray();
        for (final JsonElement id : array) {
            p.getShipments().add(getShipmentResolver().getShipment(id.getAsLong()));
        }
        return p;
    }
    /**
     * @param e JSON element.
     * @return create user request.
     */
    public CreateUserRequest parseCreateUserRequest(final JsonElement e) {
        if (e == null) {
            return null;
        }
        final JsonObject obj = e.getAsJsonObject();
        final CreateUserRequest req = new CreateUserRequest();
        req.setUser(parseUser(obj.get("user").getAsJsonObject()));
        req.setCompany(getCompanyResolver().getCompany(obj.get("company").getAsLong()));
        req.setPassword(obj.get("password").getAsString());
        return req;
    }
    /**
     * @param req create user request.
     * @return JSON object.
     */
    public JsonElement toJson(final CreateUserRequest req) {
        if (req == null) {
            return JsonNull.INSTANCE;
        }
        final JsonObject obj = new JsonObject();
        obj.add("user", toJson(req.getUser()));
        obj.addProperty("password", req.getPassword());
        obj.addProperty("company", req.getCompany().getId());
        return obj;
    }
    /**
     * @param req
     * @return
     */
    public JsonObject toJson(final UpdateUserDetailsRequest req) {
        if (req == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(UserConstants.PROPERTY_FULL_NAME, req.getFullName());
        obj.addProperty("password", req.getPassword());
        obj.addProperty("user", req.getUser());
        obj.addProperty(UserConstants.PROPERTY_TEMPERATURE_UNITS, req.getTemperatureUnits().toString());
        obj.addProperty(UserConstants.PROPERTY_TIME_ZONE, req.getTimeZone().getID());
        return obj;
    }
    public UpdateUserDetailsRequest parseUpdateUserDetailsRequest(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        final JsonObject json = el.getAsJsonObject();

        final UpdateUserDetailsRequest req = new UpdateUserDetailsRequest();
        req.setFullName(asString(json.get(UserConstants.PROPERTY_FULL_NAME)));
        req.setPassword(asString(json.get("password")));
        req.setUser(asString(json.get("user")));
        if (json.has(UserConstants.PROPERTY_TEMPERATURE_UNITS)) {
            req.setTemperatureUnits(TemperatureUnits.valueOf(
                    json.get(UserConstants.PROPERTY_TEMPERATURE_UNITS).getAsString()));
        }
        if (json.has(UserConstants.PROPERTY_TIME_ZONE)) {
            req.setTimeZone(TimeZone.getTimeZone(json.get(UserConstants.PROPERTY_TIME_ZONE).getAsString()));
        }
        return req;
    }

    /**
     * @param s list user item.
     * @return JSON object.
     */
    public JsonObject toJson(final ListUserItem s) {
        final JsonObject json = new JsonObject();
        json.addProperty("login", s.getLogin());
        json.addProperty("fullName", s.getFullName());
        return json;
    }
    /**
     * @param obj JSON object.
     * @return list user item.
     */
    public ListUserItem parseListUserItem(final JsonObject obj) {
        final ListUserItem item = new ListUserItem();
        item.setLogin(asString(obj.get("login")));
        item.setFullName(asString(obj.get("fullName")));
        return item;
    }

    /**
     * @return the referenceResolver
     */
    public ShipmentResolver getShipmentResolver() {
        return shipmentResolver;
    }
    /**
     * @param referenceResolver the referenceResolver to set
     */
    public void setShipmentResolver(final ShipmentResolver referenceResolver) {
        this.shipmentResolver = referenceResolver;
    }
    /**
     * @return the companyResolver
     */
    public CompanyResolver getCompanyResolver() {
        return companyResolver;
    }
    /**
     * @param companyResolver the companyResolver to set
     */
    public void setCompanyResolver(final CompanyResolver companyResolver) {
        this.companyResolver = companyResolver;
    }
}
