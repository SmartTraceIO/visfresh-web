/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import com.visfresh.entities.NotificationSchedule;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationScheduleControllerTest extends AbstractRestServiceTest {
    /**
     * Default constructor.
     */
    public NotificationScheduleControllerTest() {
        super();
    }
    //@RequestMapping(value = "/saveNotificationSchedule/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveNotificationSchedule(@PathVariable final String authToken,
    //        final @RequestBody String schedule) {
    @Test
    public void testSaveNotificationSchedule() throws RestServiceException, IOException {
        final NotificationSchedule s = createNotificationSchedule(false);
        final Long id = facade.saveNotificationSchedule(s);

        assertNotNull(id);
    }
    //@RequestMapping(value = "/getNotificationSchedules/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getNotificationSchedules(@PathVariable final String authToken) {
    @Test
    public void testGetNotificationSchedules() throws RestServiceException, IOException {
        createNotificationSchedule(true);
        createNotificationSchedule(true);

        assertEquals(2, facade.getNotificationSchedules(1, 10000).size());
        assertEquals(1, facade.getNotificationSchedules(1, 1).size());
        assertEquals(1, facade.getNotificationSchedules(2, 1).size());
        assertEquals(0, facade.getNotificationSchedules(3, 10000).size());
    }
    @Test
    public void testGetNotificationSchedule() throws IOException, RestServiceException {
        final NotificationSchedule s = createNotificationSchedule(true);
        assertNotNull(facade.getNotificationSchedule(s.getId()));
    }
    @Test
    public void testDeleteNotificationSchedule() throws IOException, RestServiceException {
        final NotificationSchedule s = createNotificationSchedule(true);
        facade.deleteNotificationSchedule(s.getId());
        assertNull(facade.getNotificationSchedule(s.getId()));
    }
    @Test
    public void testGetSortedNotificationSchedules() throws RestServiceException, IOException {
        final NotificationSchedule p1 = createNotificationSchedule(false);
        p1.setName("b");
        p1.setDescription("c");
        getRestService().saveNotificationSchedule(getCompany(), p1);

        final NotificationSchedule p2 = createNotificationSchedule(false);
        p2.setName("a");
        p2.setDescription("b");
        getRestService().saveNotificationSchedule(getCompany(), p2);

        final NotificationSchedule p3 = createNotificationSchedule(false);
        p3.setName("c");
        p3.setDescription("a");
        getRestService().saveNotificationSchedule(getCompany(), p3);

        //test sort by ID
        NotificationSchedule first = facade.getNotificationSchedules(1, 10000, "notificationScheduleId", "asc").get(0);
        assertEquals(p1.getId(), first.getId());

        first = facade.getNotificationSchedules(1, 10000, "notificationScheduleId", "desc").get(0);
        assertEquals(p3.getId(), first.getId());

        //test sort by name
        first = facade.getNotificationSchedules(1, 10000, "notificationScheduleName", "asc").get(0);
        assertEquals(p2.getId(), first.getId());

        first = facade.getNotificationSchedules(1, 10000, "notificationScheduleName", "desc").get(0);
        assertEquals(p3.getId(), first.getId());

        //test sort by description
        first = facade.getNotificationSchedules(1, 10000, "notificationScheduleDescription", "asc").get(0);
        assertEquals(p3.getId(), first.getId());

        first = facade.getNotificationSchedules(1, 10000, "notificationScheduleDescription", "desc").get(0);
        assertEquals(p1.getId(), first.getId());
    }
}
