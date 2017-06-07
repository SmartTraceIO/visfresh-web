/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.visfresh.entities.Company;
import com.visfresh.entities.Language;
import com.visfresh.entities.PaymentMethod;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanyDaoTest extends BaseCrudTest<CompanyDao, Company, Company, Long> {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Default constructor.
     */
    public CompanyDaoTest() {
        super(CompanyDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createSharedCompany()
     */
    @Override
    protected Company createCompany(final String name) {
        //disable creating of shared company.
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Company createTestEntity() {
        final Company c = new Company();
        c.setName("JUnit company");
        c.setDescription("Any Description");

        c.setAddress("Company address");
        c.setContactPerson("contact person");
        c.setEmail("a@b.c");
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setStartDate(parseDate("2016-03-08"));
        c.setTrackersEmail("Trackeres email");
        c.setPaymentMethod(PaymentMethod.PayPal);
        c.setBillingPerson("Billing person");
        c.setLanguage(Language.English);

        return c;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCorrectSaved(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Company c) {
        assertEquals("JUnit company", c.getName());
        assertEquals("Any Description", c.getDescription());

        assertEquals("Company address", c.getAddress());
        assertEquals("contact person", c.getContactPerson());
        assertEquals("a@b.c", c.getEmail());
        assertEquals(TimeZone.getTimeZone("UTC"), c.getTimeZone());
        assertEquals("2016-03-08", dateFormat.format(c.getStartDate()));
        assertEquals("Trackeres email", c.getTrackersEmail());
        assertEquals(PaymentMethod.PayPal, c.getPaymentMethod());
        assertEquals("Billing person", c.getBillingPerson());
        assertEquals(Language.English, c.getLanguage());
    }
    /**
     * @param str
     * @return
     */
    private Date parseDate(final String str) {
        try {
            return dateFormat.parse(str);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
