/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.visfresh.entities.Company;
import com.visfresh.entities.Language;
import com.visfresh.entities.PaymentMethod;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanySerializerTest extends AbstractSerializerTest {
    private CompanySerializer serializer;
    /**
     * Default constructor.
     */
    public CompanySerializerTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        serializer = new CompanySerializer(UTC);
    }
    @Test
    public void testCompany() {
        final String description = "Company Description";
        final Long id = 77l;
        final String name = "CompanyName";

        final String address = "Company address";
        final String contactPerson = "Contact person";
        final String email = "dev@smarttrace.com.au";
        final TimeZone timeZone = TimeZone.getDefault();
        final Date startDate = new Date(System.currentTimeMillis() - 10000000l);
        final String trackersEmail = "trackers@smarttrace.com.au";
        final PaymentMethod paymentMethod = PaymentMethod.PayPal;
        final String billingPerson = "Billing person";
        final Language language = Language.English;

        Company c = new Company();
        c.setDescription(description);
        c.setId(id);
        c.setName(name);
        c.setAddress(address);
        c.setContactPerson(contactPerson);
        c.setEmail(email);
        c.setTimeZone(timeZone);
        c.setStartDate(startDate);
        c.setTrackersEmail(trackersEmail);
        c.setPaymentMethod(paymentMethod);
        c.setBillingPerson(billingPerson);
        c.setLanguage(language);

        final JsonElement json = serializer.toJson(c);
        c = serializer.parseCompany(json);

        assertEquals(description, c.getDescription());
        assertEquals(id, c.getId());
        assertEquals(name, c.getName());
        assertEquals(address, c.getAddress());
        assertEquals(contactPerson, c.getContactPerson());
        assertEquals(email, c.getEmail());
        assertEquals(timeZone, c.getTimeZone());
        assertEquals(formatDate(startDate), formatDate(c.getStartDate()));
        assertEquals(trackersEmail, c.getTrackersEmail());
        assertEquals(paymentMethod, c.getPaymentMethod());
        assertEquals(billingPerson, c.getBillingPerson());
        assertEquals(language, c.getLanguage());
    }

    /**
     * @param date date to format.
     * @return
     */
    private String formatDate(final Date date) {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm").format(date);
    }
}
