/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.CompanyRestClient;
import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.Company;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanyControllerTest extends AbstractRestServiceTest {
    private CompanyDao dao;
    private CompanyRestClient client;
    /**
     * Default constructor.
     */
    public CompanyControllerTest() {
        super();
    }


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        dao = context.getBean(CompanyDao.class);
        client = new CompanyRestClient(SerializerUtils.UTС);
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
    }

    @Test
    public void testGetCompany() throws IOException, RestServiceException {
        final String description = "JUnit test company";
        final String name = "Test Company";

        Company c = new Company();
        c.setDescription(description);
        c.setName(name);

        dao.save(c);

        c = client.getCompany(c.getId());

        assertEquals(description, c.getDescription());
        assertEquals(name, c.getName());
    }
    @Test
    public void testGetCompanies() throws IOException, RestServiceException {
        //create company
        Company c = new Company();
        c.setDescription("JUnit test company");
        c.setName("JUnit-C-1");
        dao.save(c);

        c = new Company();
        c.setDescription("JUnit test company");
        c.setName("JUnit-C-2");
        dao.save(c);

        //+ one default company existing on server
        assertEquals(3, client.getCompanies(null, null).size());
        assertEquals(1, client.getCompanies(1, 1).size());
        assertEquals(1, client.getCompanies(2, 1).size());
        assertEquals(0, client.getCompanies(3, 10000).size());
    }
}
