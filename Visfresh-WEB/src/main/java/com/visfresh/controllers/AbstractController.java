/**
 *
 */
package com.visfresh.controllers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.EntityWithCompany;
import com.visfresh.entities.RestSession;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
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
        final Long companyId = s == null ? null : s.getCompanyId();
        checkCompanyAccess(user, companyId);
    }
    /**
     * @param user
     * @param companyId
     * @throws RestServiceException
     */
    protected void checkCompanyAccess(final User user, final Long companyId) throws RestServiceException {
        if (companyId != null && !(
                Role.SmartTraceAdmin.hasRole(user)
                || companyId.equals(user.getCompanyId()))) {
            throw new RestServiceException(ErrorCodes.SECURITY_ERROR, "Illegal company access");
        }
    }
    protected void checkCompany(final EntityWithCompany s, final Long company)
            throws RestServiceException {
        if (s != null && s.getCompanyId() != null
                && !s.getCompanyId().equals(company)) {
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
     * @return user if logged in.
     * @throws AuthenticationException
     */
    protected User getLoggedInUser() throws RestServiceException {
        final RestSession session = getSession();
        if (session == null) {
            throw new RestServiceException(ErrorCodes.AUTHENTICATION_ERROR, "Not logged in");
        }
        return session.getUser();
    }
    /**
     * @return supplied authentication information.
     */
    public static RestSession getSession() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() != null) {
            return (RestSession) auth.getCredentials();
        }
        return null;
    }
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
