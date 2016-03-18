/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.CompanyRestClient;
import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Language;
import com.visfresh.entities.PaymentMethod;
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
        final Company c = client.getCompany(getCompany().getId());

        assertEquals(getCompany().getDescription(), c.getDescription());
        assertEquals(getCompany().getName(), c.getName());
    }
    @Test
    public void testGetCompanies() throws IOException, RestServiceException {
        //create company
        Company c = new Company();
        c.setDescription("JUnit test company");
        c.setName("JUnit-C-1");

        c.setAddress("RU, Odessa, Deribasovskaya st. 1");
        c.setBillingPerson("Adam Smit");
        c.setContactPerson("James Bond");
        c.setEmail("junt@smarttrace.com.au");
        c.setLanguage(Language.English);
        c.setPaymentMethod(PaymentMethod.PayPal);
        c.setStartDate(new Date(System.currentTimeMillis() - 100000L));
        c.setTimeZone(TimeZone.getDefault());
        c.setTrackersEmail("junit.trackers@smarttrace.com.au");
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
