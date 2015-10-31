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
import com.visfresh.entities.PersonSchedule;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationScheduleTest extends AbstractRestServiceTest {
    /**
     * Default constructor.
     */
    public NotificationScheduleTest() {
        super();
    }
    //@RequestMapping(value = "/saveNotificationSchedule/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveNotificationSchedule(@PathVariable final String authToken,
    //        final @RequestBody String schedule) {
    @Test
    public void testSaveNotificationSchedule() throws RestServiceException, IOException {
        final NotificationSchedule s = createNotificationSchedule(false);
        s.getSchedules().clear();
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
    public void testSavePersonSchedule() throws IOException, RestServiceException {
        final NotificationSchedule s = createNotificationSchedule(true);
        s.getSchedules().clear();

        final PersonSchedule ps = createPersonSchedule();
        assertNotNull(facade.savePersonSchedule(s.getId(), ps));
    }
    @Test
    public void testGetPersonSchedule() throws IOException, RestServiceException {
        final NotificationSchedule s = createNotificationSchedule(true);
        assertNotNull(facade.getPersonSchedule(s.getId(), s.getSchedules().get(0).getId()));
    }
    @Test
    public void testDeleteNotificationSchedule() throws IOException, RestServiceException {
        final NotificationSchedule s = createNotificationSchedule(true);
        facade.deleteNotificationSchedule(s.getId());
        assertNull(facade.getNotificationSchedule(s.getId()));
    }
}
