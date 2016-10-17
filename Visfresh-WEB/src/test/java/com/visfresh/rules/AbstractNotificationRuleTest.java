/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractNotificationRuleTest {
    private long lastId = 1;
    private SimpleDateFormat dateFormat;
    private User user;

    /**
     * Default constructor.
     */
    public AbstractNotificationRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd mm:ss");

        final User u = new User();
        u.setId(lastId++);
        u.setEmail("a@b.c");
        this.user = u;
    }

    /**
     * Tests getPersonalSchedules method.
     */
    @Test
    public void testGetPersonalSchedules() {
        final List<NotificationSchedule> scheds = new LinkedList<>();
        final NotificationSchedule ns = new NotificationSchedule();
        ns.setId(lastId++);
        scheds.add(ns);

        ns.getSchedules().add(createPersonSchedule());
        assertEquals(0, AbstractNotificationRule.getPersonalSchedules(scheds, new Date()).size());

        final PersonSchedule s = createPersonSchedule();
        s.setAllWeek();
        s.setFromTime(0);
        s.setToTime(60 * 24 - 1);

        ns.getSchedules().add(s);
        assertEquals(1, AbstractNotificationRule.getPersonalSchedules(scheds, new Date()).size());
    }
    public void testFilterByDay() throws ParseException {
        final List<NotificationSchedule> scheds = new LinkedList<>();
        final NotificationSchedule ns = new NotificationSchedule();
        ns.setId(lastId++);
        scheds.add(ns);

        final PersonSchedule s = createPersonSchedule();
        s.setAllWeek();
        s.setFromTime(0);
        s.setToTime(60 * 24 - 1);

        ns.getSchedules().add(s);

        final Date date = dateFormat.parse("2016-10-17 18:28");
        assertEquals(1, AbstractNotificationRule.getPersonalSchedules(scheds, date).size());

        //disable given day
        s.getWeekDays()[0] = false;
        assertEquals(0, AbstractNotificationRule.getPersonalSchedules(scheds, date).size());
    }
    public void testFilterTimeRanges() throws ParseException {
        final List<NotificationSchedule> scheds = new LinkedList<>();
        final NotificationSchedule ns = new NotificationSchedule();
        ns.setId(lastId++);
        scheds.add(ns);

        final PersonSchedule s = createPersonSchedule();
        s.setAllWeek();
        s.setFromTime(0);
        s.setToTime(60 * 24 - 1);

        ns.getSchedules().add(s);

        final Date date = dateFormat.parse("2016-10-17 18:28");
        assertEquals(1, AbstractNotificationRule.getPersonalSchedules(scheds, date).size());

        //disable given day
        s.setToTime(1);
        assertEquals(0, AbstractNotificationRule.getPersonalSchedules(scheds, date).size());
    }

    /**
     * @return
     */
    private PersonSchedule createPersonSchedule() {
        final PersonSchedule s = new PersonSchedule();
        s.setId(lastId++);
        s.setUser(user);
        return s ;
    }
}
