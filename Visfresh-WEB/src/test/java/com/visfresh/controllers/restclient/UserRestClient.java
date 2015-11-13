package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.visfresh.entities.Company;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.io.json.UserSerializer;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserRestClient extends RestClient {
    private final UserSerializer serializer;
    /**
     * Default constructor.
     */
    public UserRestClient(final TimeZone tz) {
        super();
        serializer = new UserSerializer(tz);
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
        final JsonElement response = sendGetRequest(getPathWithToken( "getUser"),
                params);
        return serializer.parseUser(response);
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

        sendPostRequest(getPathWithToken("createUser"),
                serializer.toJson(req));
    }
    /**
     * @return user profile.
     * @throws RestServiceException
     * @throws IOException
     */
    public UserProfile getProfile() throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        final JsonElement response = sendGetRequest(getPathWithToken("getProfile"), params);
        return response == JsonNull.INSTANCE ? null : serializer.parseUserProfile(
                response.getAsJsonObject());
    }
    /**
     * @param req update user details request.
     * @throws RestServiceException
     * @throws IOException
     */
    public void updateUserDetails(final UpdateUserDetailsRequest req) throws IOException, RestServiceException {
        sendPostRequest(getPathWithToken("updateUserDetails"),
                serializer.toJson(req));
    }
    /**
     * @param p user profile.
     * @throws RestServiceException
     * @throws IOException
     */
    public void saveProfile(final UserProfile p) throws IOException, RestServiceException {
        sendPostRequest(getPathWithToken("saveProfile"),
                serializer.toJson(p));
    }
    /**
     * @param r company resolver.
     */
    public void setCompanyResolver(final CompanyResolver r) {
        serializer.setCompanyResolver(r);
    }
    /**
     * @param r shipment resolver.
     */
    public void setShipmentResolver(final ShipmentResolver r) {
        serializer.setShipmentResolver(r);
    }
}
