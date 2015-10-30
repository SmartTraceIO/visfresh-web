/**
 *
 */
package com.visfresh.controllers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.entities.EntityWithId;
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
     * @param list
     * @param pageIndex
     * @param pageSize
     * @return
     */
    protected <E extends EntityWithId<ID>, ID extends Serializable & Comparable<ID>> List<E> getPage(
            final List<E> list, final int pageIndex, final int pageSize) {
        final int fromIndex = (pageIndex - 1) * pageSize;
        if (fromIndex >= list.size()) {
            return new LinkedList<E>();
        }

        //sort first of all
        Collections.sort(list, new Comparator<E>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final E o1, final E o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        final int toIndex = Math.min(fromIndex + pageSize, list.size());
        return list.subList(fromIndex, toIndex);
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
        final JsonObject obj = new JsonObject();
        //add status
        obj.add("status", createStatus(0, "Success"));
        //add response
        obj.add("response", response == null ? JsonNull.INSTANCE : response);
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
     * @param dateStr date string.
     * @return date.
     */
    protected Date parseDate(final String dateStr) {
        return dateStr == null || dateStr.length() == 0 ? null : EntityJSonSerializer.parseDate(dateStr);
    }
    /**
     * @return serializer.
     */
    protected EntityJSonSerializer getSerializer() {
        final EntityJSonSerializer ser = new EntityJSonSerializer(TimeZone.getDefault());
        ser.setReferenceResolver(resolver);
        return ser;
    }
}
