/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.io.SavePersonScheduleRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationScheduleSerializerTest extends AbstractSerializerTest {
    private NotificationScheduleSerializer serializer = new NotificationScheduleSerializer(UTC);
    /**
     * Default constructor.
     */
    public NotificationScheduleSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer.setUserResolver(resolver);
    }

    @Test
    public void testSchedulePersonHowWhen() {
        PersonSchedule s = new PersonSchedule();

        final int forMinute = 17;
        final int fromMinute = 1;
        final Long id = 77l;
        final boolean pushToMobileApp = true;
        final boolean sendEmail = true;
        final boolean sendSms = true;

        s.setUser(createUser("asuvorov"));
        s.setToTime(forMinute);
        s.setFromTime(fromMinute);
        s.setId(id);
        s.setSendApp(pushToMobileApp);
        s.setSendEmail(sendEmail);
        s.setSendSms(sendSms);
        s.getWeekDays()[0] = true;
        s.getWeekDays()[3] = true;

        final JsonObject obj = serializer.toJson(s);
        s = serializer.parsePersonSchedule(obj);

        assertEquals(forMinute, s.getToTime());
        assertEquals(fromMinute, s.getFromTime());
        assertEquals(id, s.getId());
        assertEquals(pushToMobileApp, s.isSendApp());
        assertEquals(sendEmail, s.isSendEmail());
        assertEquals(sendSms, s.isSendSms());
        assertNotNull(s.getUser());
        assertTrue(s.getWeekDays()[0]);
        assertFalse(s.getWeekDays()[1]);
        assertFalse(s.getWeekDays()[2]);
        assertTrue(s.getWeekDays()[3]);
        assertFalse(s.getWeekDays()[4]);
        assertFalse(s.getWeekDays()[5]);
        assertFalse(s.getWeekDays()[6]);
    }
    @Test
    public void testNotificationSchedule() {
        final String description = "JUnit schedule";
        final Long id = 77l;
        final String name = "Sched";

        NotificationSchedule s = new NotificationSchedule();

        s.setDescription(description);
        s.setId(id);
        s.setName(name);
        s.getSchedules().add(createPersonSchedule());
        s.getSchedules().add(createPersonSchedule());

        final JsonObject obj = serializer.toJson(s).getAsJsonObject();
        s = serializer.parseNotificationSchedule(obj);

        assertEquals(description, s.getDescription());
        assertEquals(id, s.getId());
        assertEquals(name, s.getName());
        assertEquals(2, s.getSchedules().size());
    }
    @Test
    public void testSavePersonScheduleRequest() {
        SavePersonScheduleRequest req = new SavePersonScheduleRequest();
        final Long notificationScheduleId = 77L;
        req.setNotificationScheduleId(notificationScheduleId);
        req.setSchedule(createPersonSchedule());

        final JsonObject obj = serializer.toJson(req);
        req = serializer.parseSavePersonScheduleRequest(obj);

        assertEquals(notificationScheduleId, req.getNotificationScheduleId());
        assertNotNull(req.getSchedule());
    }
}
