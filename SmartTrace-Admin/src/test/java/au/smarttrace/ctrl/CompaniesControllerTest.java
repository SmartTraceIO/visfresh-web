/**
 *
 */
package au.smarttrace.ctrl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.Company;
import au.smarttrace.Language;
import au.smarttrace.PaymentMethod;
import au.smarttrace.Roles;
import au.smarttrace.User;
import au.smarttrace.company.CompaniesDao;
import au.smarttrace.company.CompaniesService;
import au.smarttrace.company.GetCompaniesRequest;
import au.smarttrace.ctrl.client.AuthClient;
import au.smarttrace.ctrl.client.CompaniesClient;
import au.smarttrace.ctrl.client.ServiceException;
import au.smarttrace.ctrl.req.Order;
import au.smarttrace.ctrl.runner.ControllerTestRunner;
import au.smarttrace.ctrl.runner.ServiceUrlHolder;
import au.smarttrace.junit.AssertUtils;
import au.smarttrace.junit.categories.ControllerTest;
import au.smarttrace.user.UsersService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(ControllerTest.class)
@RunWith(ControllerTestRunner.class)
public class CompaniesControllerTest {
    private AuthClient authClient = new AuthClient();
    private CompaniesClient client = new CompaniesClient();

    @Autowired
    private ApplicationContext context;
    @Autowired
    private CompaniesService companiesService;

    /**
     * Default constructor.
     */
    public CompaniesControllerTest() {
        super();
    }

    @Before
    public void setUp() throws ServiceException, IOException {
        final String serviceUrl = context.getBean(ServiceUrlHolder.class).getServiceUrl();
        authClient.setServiceUrl(serviceUrl);
        client.setServiceUrl(serviceUrl);

        final Company company = new Company();
        company.setName("JUnit Company");
        context.getBean(CompaniesDao.class).createCompany(company);

        //create user
        final User u = new User();
        u.setEmail("junit@developer.com");
        u.setFirstName("Java");
        u.setLastName("Developer");
        u.setCompany(company.getId());
        u.getRoles().add(Roles.SmartTraceAdmin);
        final String password = "password";
        context.getBean(UsersService.class).createUser(u, password);

        //login
        final String token = authClient.login(u.getEmail(), password).getToken().getToken();
        authClient.setAccessToken(token);
        client.setAccessToken(token);
    }

    @Test
    public void testCreateCompany() throws ServiceException, IOException {
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

        final Long id = client.createCompany(c);
        c = companiesService.getCompany(id);

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
    public void testGetCompany() throws ServiceException, IOException {
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

        companiesService.createCompany(c);
        c = client.getCompany(c.getId());

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
    public void testDeleteCompany() throws IOException, ServiceException {
        final Company c = createCompany("C1", "JUnit company");
        client.deleteCompany(c.getId());

        assertNull(companiesService.getCompany(c.getId()));
    }
    @Test
    public void testUpdateCompany() throws ServiceException, IOException {
        Company c = createCompany("C1", "JUnit company");

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

        client.updateCompany(c);
        c = companiesService.getCompany(c.getId());

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
    public void testSorting() throws IOException, ServiceException {
        final Company c1 = createCompany("Name-A", "Description-B");
        final Company c2 = createCompany("Name-B", "Description-A");

        final GetCompaniesRequest req = new GetCompaniesRequest();
        req.setNameFilter("Name-"); //set filter for exclude existing comany of logged in user

        //by name
        req.getOrders().add(new Order("name", true));
        assertEquals(c1.getId(), client.getCompanies(req).getItems().get(0).getId());

        req.getOrders().clear();
        req.getOrders().add(new Order("name", false));
        assertEquals(c2.getId(), client.getCompanies(req).getItems().get(0).getId());

        //by description
        req.getOrders().clear();
        req.getOrders().add(new Order("description", false));
        assertEquals(c1.getId(), client.getCompanies(req).getItems().get(0).getId());

        req.getOrders().clear();
        req.getOrders().add(new Order("description", true));
        assertEquals(c2.getId(), client.getCompanies(req).getItems().get(0).getId());
    }
    @Test
    public void testMultiFieldSorting() throws ServiceException, IOException {
        final Company c1  = createCompany("Name-A", "Description-B");
        final Company c2  = createCompany("Name-B", "Description-B");

        final Company c3  = createCompany("Name-A", "Description-A");
        final Company c4  = createCompany("Name-B", "Description-A");

        final GetCompaniesRequest req = new GetCompaniesRequest();
        req.setNameFilter("Name-"); //set filter for exclude existing comany of logged in user
        req.getOrders().add(new Order("name", false));
        req.getOrders().add(new Order("description", true));

        final List<Company> users = client.getCompanies(req).getItems();

        assertEquals(c4.getId(), users.get(0).getId());
        assertEquals(c2.getId(), users.get(1).getId());
        assertEquals(c3.getId(), users.get(2).getId());
        assertEquals(c1.getId(), users.get(3).getId());
    }
    @Test
    public void testLimits() throws IOException, ServiceException {
        createCompany("Name-A", "Description-B");
        createCompany("Name-B", "Description-A");

        final GetCompaniesRequest req = new GetCompaniesRequest();
        req.setNameFilter("Name-"); //set filter for exclude existing comany of logged in user
        req.setPageSize(1);

        assertEquals(1, client.getCompanies(req).getItems().size());
        req.setPage(1);
        assertEquals(1, client.getCompanies(req).getItems().size());
        req.setPage(2);
        assertEquals(0, client.getCompanies(req).getItems().size());

        assertEquals(2, client.getCompanies(req).getTotalCount());
    }
    @Test
    public void testFilterByName() throws IOException, ServiceException {
        createCompany("First and Second Company", "LastName");

        final GetCompaniesRequest req = new GetCompaniesRequest();
        req.setNameFilter("Second First");
        assertEquals(1, client.getCompanies(req).getItems().size());

        req.setNameFilter("Second Third Last");
        assertEquals(1, client.getCompanies(req).getItems().size());

        req.setNameFilter("Third ED");
        assertEquals(0, client.getCompanies(req).getItems().size());
    }
    @Test
    public void testFilterByDescription() throws IOException, ServiceException {
        createCompany("Name", "First and Second Company description");

        final GetCompaniesRequest req = new GetCompaniesRequest();
        req.setDescriptionFilter("Second First");
        assertEquals(1, client.getCompanies(req).getItems().size());

        req.setDescriptionFilter("Second Third Last");
        assertEquals(1, client.getCompanies(req).getItems().size());

        req.setDescriptionFilter("Third ED");
        assertEquals(0, client.getCompanies(req).getItems().size());
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
        companiesService.createCompany(company);
        return company;
    }
    @After
    public void tearDown() {
        final NamedParameterJdbcTemplate jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        jdbc.update("delete from users", new HashMap<>());
        jdbc.update("delete from companies", new HashMap<>());
    }
}
