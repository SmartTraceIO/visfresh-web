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
import com.visfresh.constants.NotificationScheduleConstants;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.io.SavePersonScheduleRequest;
import com.visfresh.services.lists.NotificationScheduleListItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationScheduleSerializer extends AbstractJsonSerializer {

    /**
     * @param tz
     */
    public NotificationScheduleSerializer(final TimeZone tz) {
        super(tz);
    }
    /**
     * @param schedule notification schedule.
     * @return JSON object.
     */
    public JsonElement toJson(final NotificationSchedule schedule) {
        if (schedule == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();

        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION,
                schedule.getDescription());
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID, schedule.getId());
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME, schedule.getName());

        final JsonArray array = new JsonArray();
        obj.add("schedules", array);

        for (final PersonSchedule sphw : schedule.getSchedules()) {
            array.add(toJson(sphw));
        }

        return obj;
    }
    /**
     * @param obj JSON object.
     * @return notification schedule.
     */
    public NotificationSchedule parseNotificationSchedule(final JsonObject obj) {
        final NotificationSchedule sched = new NotificationSchedule();

        sched.setDescription(asString(obj.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION)));
        sched.setName(asString(obj.get(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME)));
        sched.setId(asLong(obj.get(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID)));

        final JsonArray array = obj.get("schedules").getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            sched.getSchedules().add(parsePersonSchedule(array.get(i).getAsJsonObject()));
        }

        return sched;
    }

    /**
     * @param s schedule/person/how/when
     * @return JSON object.
     */
    public JsonObject toJson(final PersonSchedule s) {
        if (s == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();

        obj.addProperty("personScheduleId", s.getId());
        obj.addProperty("firstName", s.getFirstName());
        obj.addProperty("lastName", s.getLastName());
        obj.addProperty("company", s.getCompany());
        obj.addProperty("position", s.getPosition());
        obj.addProperty("emailNotification", s.getEmailNotification());
        obj.addProperty("smsNotification", s.getSmsNotification());
        obj.addProperty("pushToMobileApp", s.isPushToMobileApp());
        obj.addProperty("fromTime", s.getFromTime());
        obj.addProperty("toTime", s.getToTime());

        final JsonArray weekDays = new JsonArray();
        for (final boolean day : s.getWeekDays()) {
            weekDays.add(new JsonPrimitive(day));
        }
        obj.add("weekDays", weekDays);

        return obj;
    }
    /**
     * @param obj JSON object.
     * @return schedule/person/how/when
     */
    public PersonSchedule parsePersonSchedule(
            final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject obj = e.getAsJsonObject();
        final PersonSchedule s = new PersonSchedule();

        s.setCompany(asString(obj.get("company")));
        s.setEmailNotification(asString(obj.get("emailNotification")));
        s.setFirstName(asString(obj.get("firstName")));
        s.setLastName(asString(obj.get("lastName")));
        s.setPosition(asString(obj.get("position")));
        s.setSmsNotification(asString(obj.get("smsNotification")));
        s.setToTime(asInt(obj.get("toTime")));
        s.setFromTime(asInt(obj.get("fromTime")));
        s.setId(asLong(obj.get("personScheduleId")));
        s.setPushToMobileApp(asBoolean(obj.get("pushToMobileApp")));

        final JsonArray weekDays = obj.get("weekDays").getAsJsonArray();
        for (int i = 0; i < weekDays.size(); i++) {
            s.getWeekDays()[i] = weekDays.get(i).getAsBoolean();
        }

        return s;
    }
    /**
     * @param req
     * @return
     */
    public JsonObject toJson(final SavePersonScheduleRequest req) {
        if (req == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID,
                req.getNotificationScheduleId());
        json.add("schedule", toJson(req.getSchedule()));
        return json;
    }
    public SavePersonScheduleRequest parseSavePersonScheduleRequest(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final SavePersonScheduleRequest req = new SavePersonScheduleRequest();
        req.setNotificationScheduleId(asLong(json.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID)));
        req.setSchedule(parsePersonSchedule(json.get("schedule").getAsJsonObject()));
        return req;
    }
    /**
     * @param item
     * @return
     */
    public JsonObject toJson(final NotificationScheduleListItem item) {
        if (item == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID,
                item.getNotificationScheduleId());
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME,
                item.getNotificationScheduleName());
        obj.addProperty(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION,
                item.getNotificationScheduleDescription());
        obj.addProperty("peopleToNotify", item.getPeopleToNotify());
        return obj;
    }
    public NotificationScheduleListItem parseNotificationScheduleListItem(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();
        final NotificationScheduleListItem item = new NotificationScheduleListItem();
        item.setNotificationScheduleDescription(asString(json.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION)));
        item.setNotificationScheduleId(asLong(json.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID)));
        item.setNotificationScheduleName(asString(json.get(
                NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME)));
        item.setPeopleToNotify(asString(json.get("peopleToNotify")));
        return item;
    }
}
