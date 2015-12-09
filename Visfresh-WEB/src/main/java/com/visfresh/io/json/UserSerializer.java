/**
 *
 */
package com.visfresh.io.json;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.UserConstants;
import com.visfresh.entities.Language;
import com.visfresh.entities.MeasurementUnits;
import com.visfresh.entities.Role;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.SaveUserRequest;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.services.lists.ExpandedListUserItem;
import com.visfresh.services.lists.ShortListUserItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserSerializer extends AbstractJsonSerializer {
    private static final String INTERNAL_COMPANY_NAME = "internalCompany";
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
        u.setId(asLong(json.get(UserConstants.PROPERTY_ID)));
        u.setTitle(asString(json.get("title")));
        u.setFirstName(asString(json.get(UserConstants.PROPERTY_FIRST_NAME)));
        u.setLastName(asString(json.get(UserConstants.PROPERTY_LAST_NAME)));
        u.setExternal(asBoolean(json.get(UserConstants.PROPERTY_EXTERNAL)));
        u.setExternalCompany(asString(json.get(UserConstants.PROPERTY_EXTERNAL_COMPANY)));
        u.setPosition(asString(json.get(UserConstants.PROPERTY_POSITION)));
        u.setEmail(asString(json.get(UserConstants.PROPERTY_EMAIL)));
        u.setPhone(asString(json.get(UserConstants.PROPERTY_PHONE)));
        u.setTimeZone(TimeZone.getTimeZone(asString(json.get(UserConstants.PROPERTY_TIME_ZONE))));
        u.setTemperatureUnits(TemperatureUnits.valueOf(asString(json.get(
                UserConstants.PROPERTY_TEMPERATURE_UNITS))));
        u.setDeviceGroup(asString(json.get("deviceGroup")));
        u.setLanguage(Language.valueOf(asString(json.get("language"))));
        u.setMeasurementUnits(MeasurementUnits.valueOf(asString(json.get("measurementUnits"))));
        u.setActive(!Boolean.FALSE.equals(asBoolean(json.get(UserConstants.PROPERTY_ACTIVE))));

        final JsonElement roles = json.get(UserConstants.PROPERTY_ROLES);
        if (roles != null && !roles.isJsonNull()) {
            u.setRoles(new HashSet<Role>());
            u.getRoles().addAll(parseRoles(roles.getAsJsonArray()));
        }
        final JsonElement companyId = json.get(UserConstants.PROPERTY_INTERNAL_COMPANY_ID);
        if (companyId != null && !companyId.isJsonNull()) {
            u.setCompany(getCompanyResolver().getCompany(companyId.getAsLong()));
        }
        return u;
    }
    /**
     * @param u the user.
     * @return JSON object.
     */
    public JsonObject toJson(final User u) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(UserConstants.PROPERTY_ID, u.getId());
        obj.addProperty(UserConstants.PROPERTY_FIRST_NAME, u.getFirstName());
        obj.addProperty(UserConstants.PROPERTY_LAST_NAME, u.getLastName());
        obj.addProperty("title", u.getTitle());

        //company is readonly property, should not be serialized back
        if (u.getCompany() != null) {
            obj.addProperty(INTERNAL_COMPANY_NAME, u.getCompany().getName());
            obj.addProperty(UserConstants.PROPERTY_INTERNAL_COMPANY_ID, u.getCompany().getId());
        }

        obj.addProperty(UserConstants.PROPERTY_EXTERNAL, u.getExternal());
        obj.addProperty(UserConstants.PROPERTY_EXTERNAL_COMPANY, u.getExternalCompany());
        obj.addProperty(UserConstants.PROPERTY_POSITION, u.getPosition());
        obj.addProperty(UserConstants.PROPERTY_EMAIL, u.getEmail());
        obj.addProperty(UserConstants.PROPERTY_PHONE, u.getPhone());

        if (u.getRoles() != null) {
            obj.add(UserConstants.PROPERTY_ROLES, toJson(u.getRoles()));
        }

        obj.addProperty(UserConstants.PROPERTY_TIME_ZONE, u.getTimeZone().getID());
        obj.addProperty(UserConstants.PROPERTY_TEMPERATURE_UNITS, u.getTemperatureUnits().toString());
        obj.addProperty("measurementUnits", u.getMeasurementUnits().toString());
        obj.addProperty("language", u.getLanguage().toString());
        obj.addProperty("deviceGroup", u.getDeviceGroup());
        obj.addProperty(UserConstants.PROPERTY_ACTIVE, u.getActive());

        return obj;
    }
    /**
     * @param e JSON element.
     * @return create user request.
     */
    public SaveUserRequest parseSaveUserRequest(final JsonElement e) {
        if (e == null) {
            return null;
        }
        final JsonObject obj = e.getAsJsonObject();
        final SaveUserRequest req = new SaveUserRequest();
        req.setUser(parseUser(obj.get("user").getAsJsonObject()));
        req.setPassword(asString(obj.get("password")));
        req.setResetOnLogin(asBoolean(obj.get("resetOnLogin")));
        return req;
    }
    /**
     * @param req create user request.
     * @return JSON object.
     */
    public JsonElement toJson(final SaveUserRequest req) {
        if (req == null) {
            return JsonNull.INSTANCE;
        }
        final JsonObject obj = new JsonObject();
        obj.add("user", toJson(req.getUser()));
        obj.addProperty("password", req.getPassword());
        obj.addProperty("resetOnLogin", req.getResetOnLogin());
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
        obj.addProperty("user", req.getUser());
        obj.addProperty("password", req.getPassword());
        obj.addProperty(UserConstants.PROPERTY_FIRST_NAME, req.getFirstName());
        obj.addProperty(UserConstants.PROPERTY_LAST_NAME, req.getLastName());
        obj.addProperty(UserConstants.PROPERTY_POSITION, req.getPosition());
        obj.addProperty(UserConstants.PROPERTY_EMAIL, req.getEmail());
        obj.addProperty(UserConstants.PROPERTY_PHONE, req.getPhone());
        obj.addProperty(UserConstants.PROPERTY_TEMPERATURE_UNITS, req.getTemperatureUnits().toString());
        obj.addProperty(UserConstants.PROPERTY_TIME_ZONE, req.getTimeZone().getID());
        obj.addProperty("measurementUnits", req.getMeasurementUnits().toString());
        obj.addProperty("language", req.getLanguage().toString());
        obj.addProperty("scale", req.getScale());
        obj.addProperty("title", req.getTitle());
        return obj;
    }
    public UpdateUserDetailsRequest parseUpdateUserDetailsRequest(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        final JsonObject json = el.getAsJsonObject();

        final UpdateUserDetailsRequest req = new UpdateUserDetailsRequest();
        req.setFirstName(asString(json.get(UserConstants.PROPERTY_FIRST_NAME)));
        req.setLastName(asString(json.get(UserConstants.PROPERTY_LAST_NAME)));
        req.setPosition(asString(json.get(UserConstants.PROPERTY_POSITION)));
        req.setEmail(asString(json.get(UserConstants.PROPERTY_EMAIL)));
        req.setPhone(asString(json.get(UserConstants.PROPERTY_PHONE)));
        req.setPassword(asString(json.get("password")));
        req.setUser(asLong(json.get("user")));
        if (json.has(UserConstants.PROPERTY_TEMPERATURE_UNITS)) {
            req.setTemperatureUnits(TemperatureUnits.valueOf(
                    json.get(UserConstants.PROPERTY_TEMPERATURE_UNITS).getAsString()));
        }
        if (json.has(UserConstants.PROPERTY_TIME_ZONE)) {
            req.setTimeZone(TimeZone.getTimeZone(json.get(UserConstants.PROPERTY_TIME_ZONE).getAsString()));
        }
        if (json.has("language")) {
            req.setLanguage(Language.valueOf(asString(json.get("language"))));
        }
        if (json.has("measurementUnits")) {
            req.setMeasurementUnits(MeasurementUnits.valueOf(asString(json.get("measurementUnits"))));
        }
        req.setScale(asString(json.get("scale")));
        req.setTitle(asString(json.get("title")));
        return req;
    }

    /**
     * @param s list user item.
     * @return JSON object.
     */
    public JsonObject toJson(final ShortListUserItem s) {
        final JsonObject json = new JsonObject();
        json.addProperty(UserConstants.PROPERTY_ID, s.getId());
        json.addProperty("fullName", s.getFullName());
        json.addProperty("positionCompany", s.getPositionCompany());
        return json;
    }
    /**
     * @param obj JSON object.
     * @return list user item.
     */
    public ShortListUserItem parseListUserItem(final JsonObject obj) {
        final ShortListUserItem item = new ShortListUserItem();
        item.setId(asLong(obj.get(UserConstants.PROPERTY_ID)));
        item.setFullName(asString(obj.get("fullName")));
        item.setPositionCompany(asString(obj.get("positionCompany")));
        return item;
    }

    /**
     * @param item
     * @return
     */
    public JsonObject toJson(final ExpandedListUserItem item) {
        if (item == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(UserConstants.PROPERTY_ID, item.getId());
        json.addProperty(UserConstants.PROPERTY_FIRST_NAME, item.getFirstName());
        json.addProperty(UserConstants.PROPERTY_LAST_NAME, item.getLastName());
        json.addProperty(UserConstants.PROPERTY_EMAIL, item.getEmail());
        json.addProperty("companyName", item.getCompanyName());
        json.addProperty(UserConstants.PROPERTY_POSITION, item.getPosition());
        json.add(UserConstants.PROPERTY_ROLES, toJson(item.getRoles()));
        json.addProperty(UserConstants.PROPERTY_ACTIVE, item.isActive());
        json.addProperty(UserConstants.PROPERTY_EXTERNAL, item.isExternal());

        return json;
    }
    public ExpandedListUserItem parseExpandedListUserItem(final JsonObject json) {
        if (json == null) {
            return null;
        }

        final ExpandedListUserItem item = new ExpandedListUserItem();

        item.setId(asLong(json.get(UserConstants.PROPERTY_ID)));
        item.setFirstName(asString(json.get(UserConstants.PROPERTY_FIRST_NAME)));
        item.setLastName(asString(json.get(UserConstants.PROPERTY_LAST_NAME)));
        item.setEmail(asString(json.get(UserConstants.PROPERTY_EMAIL)));
        item.setCompanyName(asString(json.get("companyName")));
        item.setPosition(asString(json.get(UserConstants.PROPERTY_POSITION)));
        item.getRoles().addAll(parseRoles(json.get(UserConstants.PROPERTY_ROLES).getAsJsonArray()));
        item.setActive(asBoolean(json.get(UserConstants.PROPERTY_ACTIVE)));
        item.setExternal(asBoolean(json.get(UserConstants.PROPERTY_EXTERNAL)));

        return item;
    }

    /**
     * @param array
     * @return
     */
    protected List<Role> parseRoles(final JsonArray array) {
        final List<Role> roles = new LinkedList<Role>();
        final int size = array.size();
        for (int i = 0; i < size; i++) {
            roles.add(Role.valueOf(array.get(i).getAsString()));
        }
        return roles;
    }
    /**
     * @param roles role set.
     * @return roles as JSON array.
     */
    protected JsonArray toJson(final Set<Role> roles) {
        final JsonArray roleArray = new JsonArray();
        for (final Role r : roles) {
            roleArray.add(new JsonPrimitive(r.name()));
        }
        return roleArray;
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
