/**
 *
 */
package com.visfresh.controllers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.EntityWithCompany;
import com.visfresh.entities.User;
import com.visfresh.io.EntityJSonSerializer;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {AuthService.class})
public abstract class AbstractController {
    protected static final TimeZone UTС = TimeZone.getTimeZone("UTC");
    /**
     * Authentication service.
     */
    @Autowired
    protected AuthService authService;
    /**
     * Access controller.
     */
    @Autowired
    protected AccessController security;
    @Autowired
    private ReferenceResolver resolver;
    /**
     *
     */
    public AbstractController() {
        super();
    }


    /**
     * @param user
     * @param s
     * @throws RestServiceException
     */
    protected void checkCompanyAccess(final User user,
            final EntityWithCompany s) throws RestServiceException {
        if (s != null && s.getCompany() != null && !s.getCompany().getId().equals(user.getCompany().getId())) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR, "Illegal company access");
        }
    }
    /**
     * @param id the entity ID.
     * @param idFieldName ID field name.
     * @return JSON response.
     */
    protected String createIdResponse(final String idFieldName, final Long id) {
        return createSuccessResponse(EntityJSonSerializer.idToJson(idFieldName, id));
    }
    /**
     * @param response.
     * @return
     */
    protected String createSuccessResponse(final JsonElement response) {
        final JsonObject obj = createSuccessResponseObject(response);
        return obj.toString();
    }

    /**
     * @param response
     * @return
     */
    protected JsonObject createSuccessResponseObject(final JsonElement response) {
        final JsonObject obj = new JsonObject();
        //add status
        obj.add("status", createStatus(0, "Success"));
        //add response
        obj.add("response", response == null ? JsonNull.INSTANCE : response);
        return obj;
    }
    protected String createListSuccessResponse(final JsonArray array, final int total) {
        final JsonObject obj = createSuccessResponseObject(array);
        obj.add("totalCount", new JsonPrimitive(total));
        return obj.toString();
    }
    /**
     * @param e
     * @return
     */
    protected String createErrorResponse(final Exception e) {
        final JsonObject obj = new JsonObject();
        //add status
        obj.add("status", createErrorStatus(e));
        //add response
        return obj.toString();
    }
    /**
     * @param errorCode error code.
     * @param e error.
     * @return encoded to JSON object error.
     */
    private JsonObject createErrorStatus(final Throwable e) {
        int code = -1;
        if (e instanceof AuthenticationException) {
            code = ErrorCodes.AUTHENTICATION_ERROR;
        } else if (e instanceof RestServiceException) {
            code = ((RestServiceException) e).getErrorCode();
        }
        return EntityJSonSerializer.createErrorStatus(code, e);
    }
    /**
     * @param code status code.
     * @param message status description.
     * @return
     */
    private JsonObject createStatus(final int code, final String message) {
        final JsonObject status = new JsonObject();
        status.addProperty("code", code);
        status.addProperty("message", message);
        return status;
    }
    /**
     * @param authToken authentication token.
     * @return user if logged in.
     * @throws AuthenticationException
     */
    protected User getLoggedInUser(final String authToken)
            throws AuthenticationException {
        final User user = authService.getUserForToken(authToken);
        if (user == null) {
            throw new AuthenticationException("Not logged in");
        }
        return user;
    }
    /**
     * @param text the resource name.
     * @throws RestServiceException
     * @throws IOException
     */
    protected JsonObject getJSonObject(final String text) throws RestServiceException {
        return getJSon(text).getAsJsonObject();
    }
    /**
     * @param text the resource name.
     * @throws RestServiceException
     * @throws IOException
     */
    protected JsonElement getJSon(final String text) throws RestServiceException {
        try {
            return EntityJSonSerializer.parseJson(text);
        } catch (final Exception e) {
            throw new RestServiceException(ErrorCodes.INVALID_JSON, "Invalid JSON format");
        }
    }
    /**
     * @param user user.
     * @return serializer.
     */
    protected EntityJSonSerializer getSerializer(final User user) {
        final EntityJSonSerializer ser = new EntityJSonSerializer(
                user == null ? UTС : user.getTimeZone());
        ser.setReferenceResolver(resolver);
        return ser;
    }

    /**
     * @param sc
     * @param so
     * @param defaultSortOrder
     * @return
     */
    protected Sorting createSorting(final String sc, final String so,
            final String[] defaultSortOrder) {
        final boolean ascent = !"desc".equalsIgnoreCase(so);
        if (sc == null) {
            return new Sorting(ascent, defaultSortOrder);
        }

        final List<String> props = new LinkedList<String>();
        for (final String prop : defaultSortOrder) {
            if (prop.equals(sc)) {
                props.add(0, prop);
            } else {
                props.add(prop);
            }
        }

        return new Sorting(ascent, props.toArray(new String[props.size()]));
    }
}
