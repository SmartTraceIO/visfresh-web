package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.visfresh.entities.Company;
import com.visfresh.entities.User;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.SaveUserRequest;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.io.json.UserSerializer;
import com.visfresh.services.RestServiceException;
import com.visfresh.services.lists.ListUserItem;

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
     * @param id
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    public User getUser(final Long id) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("userId", id.toString());
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
    public Long saveUser(final User u, final Company c, final String password) throws IOException, RestServiceException {
        final SaveUserRequest req = new SaveUserRequest();
        req.setCompany(c);
        req.setUser(u);
        req.setPassword(password);

        return parseId(sendPostRequest(getPathWithToken("saveUser"),
                serializer.toJson(req)).getAsJsonObject());
    }
    /**
     * @param req update user details request.
     * @throws RestServiceException
     * @throws IOException
     */
    public void updateUserDetails(final UpdateUserDetailsRequest req) throws IOException, RestServiceException {
        sendPostRequest(getPathWithToken("updateUserDetails"), serializer.toJson(req));
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
    public List<User> getUsers(final Integer pageIndex, final Integer pageSize,
            final String sortColumn,
            final String sortOrder) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        if (sortColumn != null) {
            params.put("sc", sortColumn);
        }
        if (sortOrder != null) {
            params.put("so", sortOrder);
        }

        final JsonArray response = sendGetRequest(getPathWithToken("getUsers"),
                params).getAsJsonArray();

        final List<User> users = new ArrayList<User>(response.size());
        for (int i = 0; i < response.size(); i++) {
            users.add(serializer.parseUser(response.get(i).getAsJsonObject()));
        }
        return users;
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
    public List<ListUserItem> listUsers(final Integer pageIndex, final Integer pageSize,
            final String sortColumn,
            final String sortOrder) throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        if (pageIndex != null) {
            params.put("pageIndex", Integer.toString(pageIndex));
            params.put("pageSize", Integer.toString(pageSize == null ? Integer.MAX_VALUE : pageSize));
        }
        if (sortColumn != null) {
            params.put("sc", sortColumn);
        }
        if (sortOrder != null) {
            params.put("so", sortOrder);
        }

        final JsonArray response = sendGetRequest(getPathWithToken("listUsers"),
                params).getAsJsonArray();

        final List<ListUserItem> users = new ArrayList<ListUserItem>(response.size());
        for (int i = 0; i < response.size(); i++) {
            users.add(serializer.parseListUserItem(response.get(i).getAsJsonObject()));
        }
        return users;
    }
    /**
     * @param u user to delete.
     * @throws RestServiceException
     * @throws IOException
     */
    public void deleteUser(final User u) throws IOException, RestServiceException {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userId", u.getId().toString());
        sendGetRequest(getPathWithToken("deleteUser"), params);
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
