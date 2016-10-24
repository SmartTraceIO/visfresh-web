/**
 *
 */
package com.visfresh.controllers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.EntityWithCompany;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {AuthService.class})
public abstract class AbstractController {
    /**
     * Authentication service.
     */
    @Autowired
    protected AuthService authService;
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
        if (s != null && s.getCompany() != null && !(
                Role.SmartTraceAdmin.hasRole(user)
                || s.getCompany().getId().equals(user.getCompany().getId()))) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR, "Illegal company access");
        }
    }
    protected <T extends EntityWithCompany> void checkCompanyAccess(final User user, final Collection<T> c)
            throws RestServiceException {
        for (final T t : c) {
            checkCompanyAccess(user, t);
        }
    }
    /**
     * @param user user.
     * @param role role.
     * @throws RestServiceException
     */
    protected void checkAccess(final User user, final Role role) throws RestServiceException {
        if (!role.hasRole(user)) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR,
                    user.getEmail() + " is not permitted for role " + role);
        }
    }
    /**
     * @param id the entity ID.
     * @param idFieldName ID field name.
     * @return JSON response.
     */
    protected JsonObject createIdResponse(final String idFieldName, final Long id) {
        return createSuccessResponse(SerializerUtils.idToJson(idFieldName, id));
    }
    /**
     * @param id the entity ID.
     * @param idFieldName ID field name.
     * @return JSON response.
     */
    protected JsonObject createIdResponse(final String idFieldName, final String id) {
        return createSuccessResponse(SerializerUtils.idToJson(idFieldName, id));
    }
    /**
     * @param response
     * @return
     */
    protected JsonObject createSuccessResponse(final JsonElement response) {
        return createSuccessResponse("Success", response);
    }
    /**
     * @param message
     * @param response
     * @return
     */
    protected JsonObject createSuccessResponse(final String message,
            final JsonElement response) {
        final JsonObject obj = new JsonObject();
        //add status
        obj.add("status", createStatus(0, message));
        //add response
        obj.add("response", response == null ? JsonNull.INSTANCE : response);
        return obj;
    }
    /**
     * @param array
     * @param total
     * @return
     */
    protected JsonObject createListSuccessResponse(final JsonArray array,
            final int total) {
        final JsonObject obj = createSuccessResponse(array);
        obj.add("totalCount", new JsonPrimitive(total));
        return obj;
    }
    /**
     * @param e
     * @return
     */
    protected JsonObject createErrorResponse(final Throwable e) {
        int code = -1;
        if (e instanceof AuthenticationException) {
            code = ErrorCodes.AUTHENTICATION_ERROR;
        } else if (e instanceof RestServiceException) {
            code = ((RestServiceException) e).getErrorCode();
        }
        final String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        return createErrorResponse(code, msg);
    }
    /**
     * @param code error code.
     * @param msg error message.
     * @return error response object.
     */
    protected JsonObject createErrorResponse(final int code, final String msg) {
        final JsonObject obj = new JsonObject();
        //add status
        final JsonObject status =  SerializerUtils.createErrorStatus(code, msg);
        obj.add("status", status);
        return obj;
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
//    /**
//     * @param text the resource name.
//     * @throws RestServiceException
//     * @throws IOException
//     */
//    protected JsonObject getJSonObject(final String text) throws RestServiceException {
//        return getJSon(text).getAsJsonObject();
//    }
//    /**
//     * @param text the resource name.
//     * @throws RestServiceException
//     * @throws IOException
//     */
//    protected JsonElement getJSon(final String text) throws RestServiceException {
//        try {
//            return SerializerUtils.parseJson(text);
//        } catch (final Exception e) {
//            throw new RestServiceException(ErrorCodes.INVALID_JSON, "Invalid JSON format");
//        }
//    }
    /**
     * @param sc sort column
     * @param so sorting order.
     * @param defaultSortOrder
     * @param maxNumOfSortColumns max number of soring columns.
     * @return
     */
    protected Sorting createSorting(final String sc, final String so,
            final String[] defaultSortOrder, final int maxNumOfSortColumns) {
        final boolean ascent = !"desc".equalsIgnoreCase(so);
        if (sc == null) {
            return new Sorting(ascent, defaultSortOrder);
        }

        //create ordered list of sorting columns.
        List<String> props = new LinkedList<String>();
        for (final String prop : defaultSortOrder) {
            if (prop.equalsIgnoreCase(sc)) {
                props.add(0, prop);
            } else {
                props.add(prop);
            }
        }
        if (props.isEmpty()) {
            return null;
        }

        props = props.subList(0, Math.min(props.size(), maxNumOfSortColumns));
        return new Sorting(ascent, props.toArray(new String[props.size()]));
    }
}
