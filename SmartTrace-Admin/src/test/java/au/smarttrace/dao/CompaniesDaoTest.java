/**
 *
 */
package au.smarttrace.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import au.smarttrace.Company;
import au.smarttrace.Language;
import au.smarttrace.PaymentMethod;
import au.smarttrace.company.CompaniesDao;
import au.smarttrace.company.GetCompaniesRequest;
import au.smarttrace.ctrl.req.Order;
import au.smarttrace.dao.runner.DaoTestRunner;
import au.smarttrace.dao.runner.DbSupport;
import au.smarttrace.junit.AssertUtils;
import au.smarttrace.junit.categories.DaoTest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class CompaniesDaoTest {
    @Autowired
    private CompaniesDao dao;
    @Autowired
    private DbSupport dbSupport;

    /**
     * Default constructor.
     */
    public CompaniesDaoTest() {
        super();
    }

    /**
     * Tests create method.
     */
    @Test
    public void testCreate() {
        final Company c = new Company();
        c.setName("JUnit");
        dao.createCompany(c);

        assertNotNull(dao.getById(c.getId()));
    }
    @Test
    public void testGetById() {
        final String address = "JUnit street";
        final String billingPerson = "Billing Person";
        final String contactPerson = "Contact Person";
        final String description = "JUnit Company";
        final String email = "company@junit.org";
        final Language language = Language.German;
        final String name = "JUnit";
        final PaymentMethod paymentMethod = PaymentMethod.Invoice;
        final Date startDate = new Date();
        final TimeZone timeZone = TimeZone.getTimeZone("PCT");
        final String trackersEmail = "tracker@junit.org";

        Company c = new Company();
        c.setAddress(address);
        c.setBillingPerson(billingPerson);
        c.setContactPerson(contactPerson);
        c.setDescription(description);
        c.setEmail(email);
        c.setLanguage(language);
        c.setName(name);
        c.setPaymentMethod(paymentMethod);
        c.setStartDate(startDate);
        c.setTimeZone(timeZone);
        c.setTrackersEmail(trackersEmail);

        dao.createCompany(c);
        c = dao.getById(c.getId());

        assertNotNull(c.getId());
        assertEquals(address, c.getAddress());
        assertEquals(billingPerson, c.getBillingPerson());
        assertEquals(contactPerson, c.getContactPerson());
        assertEquals(description, c.getDescription());
        assertEquals(email, c.getEmail());
        assertEquals(language, c.getLanguage());
        assertEquals(name, c.getName());
        assertEquals(paymentMethod, c.getPaymentMethod());
        AssertUtils.assertEqualDates(startDate, c.getStartDate(), 1000l);
        assertEquals(timeZone, c.getTimeZone());
        assertEquals(trackersEmail, c.getTrackersEmail());
    }
    @Test
    public void testUpdate() {
        Company c = new Company();
        c.setName("Old Name");
        dao.createCompany(c);

        final String address = "JUnit street";
        final String billingPerson = "Billing Person";
        final String contactPerson = "Contact Person";
        final String description = "JUnit Company";
        final String email = "company@junit.org";
        final Language language = Language.German;
        final String name = "JUnit";
        final PaymentMethod paymentMethod = PaymentMethod.Invoice;
        final Date startDate = new Date();
        final TimeZone timeZone = TimeZone.getTimeZone("PCT");
        final String trackersEmail = "tracker@junit.org";

        c.setAddress(address);
        c.setBillingPerson(billingPerson);
        c.setContactPerson(contactPerson);
        c.setDescription(description);
        c.setEmail(email);
        c.setLanguage(language);
        c.setName(name);
        c.setPaymentMethod(paymentMethod);
        c.setStartDate(startDate);
        c.setTimeZone(timeZone);
        c.setTrackersEmail(trackersEmail);

        dao.updateCompany(c);
        c = dao.getById(c.getId());

        assertNotNull(c.getId());
        assertEquals(address, c.getAddress());
        assertEquals(billingPerson, c.getBillingPerson());
        assertEquals(contactPerson, c.getContactPerson());
        assertEquals(description, c.getDescription());
        assertEquals(email, c.getEmail());
        assertEquals(language, c.getLanguage());
        assertEquals(name, c.getName());
        assertEquals(paymentMethod, c.getPaymentMethod());
        AssertUtils.assertEqualDates(startDate, c.getStartDate(), 1000l);
        assertEquals(timeZone, c.getTimeZone());
        assertEquals(trackersEmail, c.getTrackersEmail());
    }
    @Test
    public void testSorting() {
        final Company c1 = createCompany("Name-A", "Description-B");
        final Company c2 = createCompany("Name-B", "Description-A");

        final GetCompaniesRequest req = new GetCompaniesRequest();

        //by name
        req.getOrders().add(new Order("name", true));
        assertEquals(c1.getId(), dao.getCompanies(req).getItems().get(0).getId());

        req.getOrders().clear();
        req.getOrders().add(new Order("name", false));
        assertEquals(c2.getId(), dao.getCompanies(req).getItems().get(0).getId());

        //by description
        req.getOrders().clear();
        req.getOrders().add(new Order("description", false));
        assertEquals(c1.getId(), dao.getCompanies(req).getItems().get(0).getId());

        req.getOrders().clear();
        req.getOrders().add(new Order("description", true));
        assertEquals(c2.getId(), dao.getCompanies(req).getItems().get(0).getId());
    }
    @Test
    public void testMultiFieldSorting() {
        final Company c1  = createCompany("Name-A", "Description-B");
        final Company c2  = createCompany("Name-B", "Description-B");

        final Company c3  = createCompany("Name-A", "Description-A");
        final Company c4  = createCompany("Name-B", "Description-A");

        final GetCompaniesRequest req = new GetCompaniesRequest();
        req.getOrders().add(new Order("name", false));
        req.getOrders().add(new Order("description", true));

        final List<Company> users = dao.getCompanies(req).getItems();

        assertEquals(c4.getId(), users.get(0).getId());
        assertEquals(c2.getId(), users.get(1).getId());
        assertEquals(c3.getId(), users.get(2).getId());
        assertEquals(c1.getId(), users.get(3).getId());
    }
    @Test
    public void testLimits() {
        createCompany("Name-A", "Description-B");
        createCompany("Name-B", "Description-A");

        final GetCompaniesRequest req = new GetCompaniesRequest();
        req.setPageSize(1);

        assertEquals(1, dao.getCompanies(req).getItems().size());
        req.setPage(1);
        assertEquals(1, dao.getCompanies(req).getItems().size());
        req.setPage(2);
        assertEquals(0, dao.getCompanies(req).getItems().size());

        assertEquals(2, dao.getCompanies(req).getTotalCount());
    }
    @Test
    public void testFilterByName() {
        createCompany("First and Second Company", "LastName");

        final GetCompaniesRequest req = new GetCompaniesRequest();
        req.setNameFilter("Second First");
        assertEquals(1, dao.getCompanies(req).getItems().size());

        req.setNameFilter("Second Third Last");
        assertEquals(1, dao.getCompanies(req).getItems().size());

        req.setNameFilter("Third ED");
        assertEquals(0, dao.getCompanies(req).getItems().size());
    }
    @Test
    public void testFilterByDescription() {
        createCompany("Name", "First and Second Company description");

        final GetCompaniesRequest req = new GetCompaniesRequest();
        req.setDescriptionFilter("Second First");
        assertEquals(1, dao.getCompanies(req).getItems().size());

        req.setDescriptionFilter("Second Third Last");
        assertEquals(1, dao.getCompanies(req).getItems().size());

        req.setDescriptionFilter("Third ED");
        assertEquals(0, dao.getCompanies(req).getItems().size());
    }
    @Test
    public void testDeleteCompany() {
        final Company company = createCompany("JUnit", "JUnit Company");
        dao.deleteCompany(company.getId());

        assertNull(dao.getById(company.getId()));
    }
    /**
     * @param name company name.
     * @param description company description.
     * @return
     */
    private Company createCompany(final String name, final String description) {
        final Company company = new Company();
        company.setName(name);
        company.setDescription(description);
        dao.createCompany(company);
        return company;
    }
    @After
    public void tearDown() {
        dbSupport.deleteCompanies();
    }
}
