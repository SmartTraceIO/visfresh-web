/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.NotificationScheduleRestClient;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.User;
import com.visfresh.io.UserResolver;
import com.visfresh.services.RestServiceException;
import com.visfresh.services.lists.ListNotificationScheduleItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationScheduleControllerTest extends AbstractRestServiceTest {
    private NotificationScheduleRestClient client = new NotificationScheduleRestClient(UTC);
    private User suvorov;
    private User kutuzov;

    /**
     * Default constructor.
     */
    public NotificationScheduleControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        client.setUserResolver(context.getBean(UserResolver.class));
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
        suvorov = createUser1();
        kutuzov = createUser2();
    }

    //@RequestMapping(value = "/saveNotificationSchedule/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveNotificationSchedule(@PathVariable final String authToken,
    //        final @RequestBody String schedule) {
    @Test
    public void testSaveNotificationSchedule() throws RestServiceException, IOException {
        final NotificationSchedule s = createNotificationSchedule(suvorov, false);
        final Long id = client.saveNotificationSchedule(s);

        assertNotNull(id);
    }
    //@RequestMapping(value = "/getNotificationSchedules/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getNotificationSchedules(@PathVariable final String authToken) {
    @Test
    public void testGetNotificationSchedules() throws RestServiceException, IOException {
        createNotificationSchedule(suvorov, true);
        createNotificationSchedule(kutuzov, true);

        assertEquals(2, client.getNotificationSchedules(1, 10000).size());
        assertEquals(1, client.getNotificationSchedules(1, 1).size());
        assertEquals(1, client.getNotificationSchedules(2, 1).size());
        assertEquals(0, client.getNotificationSchedules(3, 10000).size());
    }
    @Test
    public void testGetNotificationSchedule() throws IOException, RestServiceException {
        final NotificationSchedule s = createNotificationSchedule(suvorov, true);
        assertNotNull(client.getNotificationSchedule(s.getId()));
    }
    @Test
    public void testDeleteNotificationSchedule() throws IOException, RestServiceException {
        final NotificationSchedule s = createNotificationSchedule(suvorov, true);
        client.deleteNotificationSchedule(s.getId());
        assertNull(client.getNotificationSchedule(s.getId()));
    }
    @Test
    public void testGetSortedNotificationSchedules() throws RestServiceException, IOException {
        final NotificationSchedule p1 = createNotificationSchedule(suvorov, false);
        p1.setName("b");
        p1.setDescription("c");
        saveNotificationScheduleDirectly(p1);

        final NotificationSchedule p2 = createNotificationSchedule(kutuzov, false);
        p2.setName("a");
        p2.setDescription("b");
        saveNotificationScheduleDirectly(p2);

        final NotificationSchedule p3 = createNotificationSchedule(kutuzov, false);
        p3.setName("c");
        p3.setDescription("a");
        saveNotificationScheduleDirectly(p3);

        //test sort by ID
        ListNotificationScheduleItem first = client.getNotificationSchedules(1, 10000, "notificationScheduleId", "asc").get(0);
        assertEquals(p1.getId(), first.getId());

        first = client.getNotificationSchedules(1, 10000, "notificationScheduleId", "desc").get(0);
        assertEquals(p3.getId(), first.getId());

        //test sort by name
        first = client.getNotificationSchedules(1, 10000, "notificationScheduleName", "asc").get(0);
        assertEquals(p2.getId(), first.getId());

        first = client.getNotificationSchedules(1, 10000, "notificationScheduleName", "desc").get(0);
        assertEquals(p3.getId(), first.getId());

        //test sort by description
        first = client.getNotificationSchedules(1, 10000, "notificationScheduleDescription", "asc").get(0);
        assertEquals(p3.getId(), first.getId());

        first = client.getNotificationSchedules(1, 10000, "notificationScheduleDescription", "desc").get(0);
        assertEquals(p1.getId(), first.getId());
    }
    /**
     * Tests deletePersonSchedule method.
     * @throws IOException
     * @throws RestServiceException
     */
    @Test
    public void testDeletePersonSchedule() throws IOException, RestServiceException {
        final NotificationSchedule p = createNotificationSchedule(suvorov, true);

        client.deletePersonSchedule(p.getId(), p.getSchedules().get(1).getId());

        final NotificationSchedule p1 = client.getNotificationSchedule(p.getId());

        assertEquals(1, p1.getSchedules().size());
        assertEquals(p.getSchedules().get(0).getId(), p1.getSchedules().get(0).getId());
    }
}
