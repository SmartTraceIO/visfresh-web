/**
 *
 */
package com.visfresh.controllers.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.io.json.NotificationScheduleSerializer;
import com.visfresh.services.RestServiceException;
import com.visfresh.services.lists.NotificationScheduleListItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationScheduleRestClient extends RestClient {
    private NotificationScheduleSerializer serializer;
    /**
     *
     */
    public NotificationScheduleRestClient(final TimeZone tz) {
        super();
        serializer = new NotificationScheduleSerializer(tz);
    }

    public Long saveNotificationSchedule(final NotificationSchedule schedule)
            throws RestServiceException, IOException {
        final JsonObject e = sendPostRequest(getPathWithToken("saveNotificationSchedule"),
                serializer.toJson(schedule)).getAsJsonObject();
        return parseId(e);
    }
    public List<NotificationScheduleListItem> getNotificationSchedules(
            final Integer pageIndex, final Integer pageSize)
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
    public List<NotificationScheduleListItem> getNotificationSchedules(
            final Integer pageIndex, final Integer pageSize,
            final String sortColumn, final String sortOrder) throws IOException, RestServiceException {
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
        final JsonArray response = sendGetRequest(getPathWithToken("getNotificationSchedules"),
                params).getAsJsonArray();

        final List<NotificationScheduleListItem> profiles = new ArrayList<NotificationScheduleListItem>(response.size());
        for (int i = 0; i < response.size(); i++) {
            profiles.add(serializer.parseNotificationScheduleListItem(response.get(i).getAsJsonObject()));
        }
        return profiles;
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getNotificationSchedule(java.lang.Long)
     */
    public NotificationSchedule getNotificationSchedule(final Long id)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("notificationScheduleId", id.toString());

        final JsonElement response = sendGetRequest(getPathWithToken("getNotificationSchedule"), params);
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

        sendGetRequest(getPathWithToken("deleteNotificationSchedule"), params);
    }
    /**
     * @param scheduleId
     * @param personScheduleId
     * @throws RestServiceException
     * @throws IOException
     */
    public void deletePersonSchedule(final long scheduleId, final long personScheduleId)
            throws IOException, RestServiceException {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("notificationScheduleId", Long.toString(scheduleId));
        params.put("personScheduleId", Long.toString(personScheduleId));

        sendGetRequest(getPathWithToken("deletePersonSchedule"), params);
    }
}
