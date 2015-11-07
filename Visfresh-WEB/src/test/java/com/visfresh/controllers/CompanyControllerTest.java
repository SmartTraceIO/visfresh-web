/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.Company;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanyControllerTest extends AbstractRestServiceTest {
    private CompanyDao dao;
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
    }

    @Test
    public void testGetCompany() throws IOException, RestServiceException {
        final String description = "JUnit test company";
        final Long id = 77777l;
        final String name = "Test Company";

        Company c = new Company();
        c.setDescription(description);
        c.setId(id);
        c.setName(name);

        dao.save(c);

        c = facade.getCompany(c.getId());

        assertEquals(description, c.getDescription());
        assertEquals(id, c.getId());
        assertEquals(name, c.getName());
    }
    @Test
    public void testGetCompanies() throws IOException, RestServiceException {
        //create company
        Company c = new Company();
        c.setDescription("JUnit test company");
        c.setId(7777l);
        c.setName("JUnit-C-1");
        dao.save(c);

        c = new Company();
        c.setDescription("JUnit test company");
        c.setId(7778l);
        c.setName("JUnit-C-2");
        dao.save(c);

        //+ one default company existing on server
        assertEquals(3, facade.getCompanies(null, null).size());
        assertEquals(1, facade.getCompanies(1, 1).size());
        assertEquals(1, facade.getCompanies(2, 1).size());
        assertEquals(0, facade.getCompanies(3, 10000).size());
    }
}
