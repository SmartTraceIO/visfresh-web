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

import com.visfresh.constants.CorrectiveActionsConstants;
import com.visfresh.controllers.restclient.CorrectiveActionListRestClient;
import com.visfresh.dao.CorrectiveActionListDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.CorrectiveActionList;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CorrectiveActionControllerTest extends AbstractRestServiceTest {
    private CorrectiveActionListDao dao;
    private CorrectiveActionListRestClient client;
    /**
     * Default constructor.
     */
    public CorrectiveActionControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(CorrectiveActionListDao.class);

        final User user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
        String token;
        try {
            token = context.getBean(AuthService.class).login(user.getEmail(),"").getToken();
        } catch (final AuthenticationException e) {
            throw new RuntimeException(e);
        }

        client = new CorrectiveActionListRestClient();
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(token);
    }

    //@RequestMapping(value = "/saveCorrectiveActionList/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveCorrectiveActionList(@PathVariable final String authToken,
    //        final @RequestBody String alert) {
    @Test
    public void testSaveCorrectiveActionList() throws RestServiceException, IOException {
        final CorrectiveActionList list = createCorrectiveActionList(false);
        list.getActions().add(new CorrectiveAction("a"));
        list.getActions().add(new CorrectiveAction("b"));

        final Long id = client.saveCorrectiveActionList(list);
        assertNotNull(id);

        final CorrectiveActionList actual = dao.findOne(id);
        assertNotNull(actual);

        assertEquals(list.getName(), actual.getName());
        assertEquals("a", list.getActions().get(0).getAction());
        assertEquals("b", list.getActions().get(1).getAction());
    }
    @Test
    public void testGetCorrectiveActionList() throws IOException, RestServiceException {
        final CorrectiveActionList list = createCorrectiveActionList(true);
        list.getActions().add(new CorrectiveAction("a"));
        list.getActions().add(new CorrectiveAction("b"));
        dao.save(list);

        final CorrectiveActionList actual = client.getCorrectiveActionList(list.getId());
        assertNotNull(actual);

        assertEquals(list.getName(), actual.getName());
        assertEquals("a", list.getActions().get(0).getAction());
        assertEquals("b", list.getActions().get(1).getAction());
    }
    @Test
    public void testDeleteCorrectiveActionList() throws RestServiceException, IOException {
        final CorrectiveActionList p = createCorrectiveActionList(true);
        client.deleteCorrectiveActionList(p);
        assertNull(dao.findOne(p.getId()));
    }
    //@RequestMapping(value = "/getCorrectiveActionLists/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getCorrectiveActionLists(@PathVariable final String authToken) {
    @Test
    public void testGetCorrectiveActionLists() throws RestServiceException, IOException {
        createCorrectiveActionList(true);
        createCorrectiveActionList(true);

        assertEquals(2, client.getCorrectiveActionLists(null, null).size());
        assertEquals(1, client.getCorrectiveActionLists(1, 1).size());
        assertEquals(1, client.getCorrectiveActionLists(2, 1).size());
        assertEquals(0, client.getCorrectiveActionLists(3, 1).size());
    }
    @Test
    public void testGetSortedCorrectiveActionLists() throws RestServiceException, IOException {
        final CorrectiveActionList p1 = createCorrectiveActionList(false);
        p1.setName("b");
        dao.save(p1);

        final CorrectiveActionList p2 = createCorrectiveActionList(false);
        p2.setName("a");
        dao.save(p2);

        final CorrectiveActionList p3 = createCorrectiveActionList(false);
        p3.setName("c");
        dao.save(p3);

        final int maxIndex = 2;

        //test sort by ID
        CorrectiveActionList first = client.getCorrectiveActionLists(1, 10000,
                CorrectiveActionsConstants.LIST_ID, "asc").get(0);
        assertEquals(p1.getId(), first.getId());

        first = client.getCorrectiveActionLists(1, 10000,
                CorrectiveActionsConstants.LIST_ID, "desc").get(maxIndex);
        assertEquals(p1.getId(), first.getId());

        //test sort by name
        first = client.getCorrectiveActionLists(1, 10000,
                CorrectiveActionsConstants.LIST_NAME, "asc").get(0);
        assertEquals(p2.getId(), first.getId());

        first = client.getCorrectiveActionLists(1, 10000,
                CorrectiveActionsConstants.LIST_NAME, "desc").get(maxIndex);
        assertEquals(p2.getId(), first.getId());
    }

    /**
     * @param save
     * @return
     */
    private CorrectiveActionList createCorrectiveActionList(final boolean save) {
        final CorrectiveActionList list = new CorrectiveActionList();
        list.setCompany(getCompany());
        list.setName("JUnit action list");

        if (save) {
            dao.save(list);
        }
        return list;
    }
}
