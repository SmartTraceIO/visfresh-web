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

import com.visfresh.constants.CriticalActionListConstants;
import com.visfresh.controllers.restclient.CriticalActionListRestClient;
import com.visfresh.dao.CriticalActionListDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.CriticalActionList;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CriticalActionListControllerTest extends AbstractRestServiceTest {
    private CriticalActionListDao dao;
    private CriticalActionListRestClient client;
    /**
     * Default constructor.
     */
    public CriticalActionListControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        dao = context.getBean(CriticalActionListDao.class);

        final User user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
        String token;
        try {
            token = context.getBean(AuthService.class).login(user.getEmail(),"").getToken();
        } catch (final AuthenticationException e) {
            throw new RuntimeException(e);
        }

        client = new CriticalActionListRestClient();
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(token);
    }

    //@RequestMapping(value = "/saveCriticalActionList/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveCriticalActionList(@PathVariable final String authToken,
    //        final @RequestBody String alert) {
    @Test
    public void testSaveCriticalActionList() throws RestServiceException, IOException {
        final CriticalActionList list = createCriticalActionList(false);
        list.getActions().add("a");
        list.getActions().add("b");

        final Long id = client.saveCriticalActionList(list);
        assertNotNull(id);

        final CriticalActionList actual = dao.findOne(id);
        assertNotNull(actual);

        assertEquals(list.getName(), actual.getName());
        assertEquals("a", list.getActions().get(0));
        assertEquals("b", list.getActions().get(1));
    }
    @Test
    public void testGetCriticalActionList() throws IOException, RestServiceException {
        final CriticalActionList list = createCriticalActionList(true);
        list.getActions().add("a");
        list.getActions().add("b");
        dao.save(list);

        final CriticalActionList actual = client.getCriticalActionList(list.getId());
        assertNotNull(actual);

        assertEquals(list.getName(), actual.getName());
        assertEquals("a", list.getActions().get(0));
        assertEquals("b", list.getActions().get(1));
    }
    @Test
    public void testDeleteCriticalActionList() throws RestServiceException, IOException {
        final CriticalActionList p = createCriticalActionList(true);
        client.deleteCriticalActionList(p);
        assertNull(dao.findOne(p.getId()));
    }
    //@RequestMapping(value = "/getCriticalActionLists/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getCriticalActionLists(@PathVariable final String authToken) {
    @Test
    public void testGetCriticalActionLists() throws RestServiceException, IOException {
        createCriticalActionList(true);
        createCriticalActionList(true);

        assertEquals(2, client.getCriticalActionLists(null, null).size());
        assertEquals(1, client.getCriticalActionLists(1, 1).size());
        assertEquals(1, client.getCriticalActionLists(2, 1).size());
        assertEquals(0, client.getCriticalActionLists(3, 1).size());
    }
    @Test
    public void testGetSortedCriticalActionLists() throws RestServiceException, IOException {
        final CriticalActionList p1 = createCriticalActionList(false);
        p1.setName("b");
        dao.save(p1);

        final CriticalActionList p2 = createCriticalActionList(false);
        p2.setName("a");
        dao.save(p2);

        final CriticalActionList p3 = createCriticalActionList(false);
        p3.setName("c");
        dao.save(p3);

        final int maxIndex = 2;

        //test sort by ID
        CriticalActionList first = client.getCriticalActionLists(1, 10000,
                CriticalActionListConstants.LIST_ID, "asc").get(0);
        assertEquals(p1.getId(), first.getId());

        first = client.getCriticalActionLists(1, 10000,
                CriticalActionListConstants.LIST_ID, "desc").get(maxIndex);
        assertEquals(p1.getId(), first.getId());

        //test sort by name
        first = client.getCriticalActionLists(1, 10000,
                CriticalActionListConstants.LIST_NAME, "asc").get(0);
        assertEquals(p2.getId(), first.getId());

        first = client.getCriticalActionLists(1, 10000,
                CriticalActionListConstants.LIST_NAME, "desc").get(maxIndex);
        assertEquals(p2.getId(), first.getId());
    }

    /**
     * @param save
     * @return
     */
    private CriticalActionList createCriticalActionList(final boolean save) {
        final CriticalActionList list = new CriticalActionList();
        list.setCompany(getCompany());
        list.setName("JUnit action list");

        if (save) {
            dao.save(list);
        }
        return list;
    }
}
