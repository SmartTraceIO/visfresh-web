/**
 *
 */
package com.visfresh.tools;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import com.visfresh.controllers.restclient.CompanyRestClient;
import com.visfresh.controllers.restclient.DeviceRestClient;
import com.visfresh.controllers.restclient.UserRestClient;
import com.visfresh.entities.Company;
import com.visfresh.entities.Language;
import com.visfresh.entities.Role;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.lists.ExpandedListUserItem;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractTool {

    /**
     *
     */
    private static final TimeZone UTС = SerializerUtils.UTС;
    public static final String COMPANY_NAME = "Demo";
    protected String EMAIL = "developer@visfresh.com";
    protected UserRestClient userService;
    protected CompanyRestClient companyService;
    protected DeviceRestClient deviceService;
    protected Company company;
    protected User user;

    /**
     *
     */
    public AbstractTool(final String url)
            throws IOException, RestServiceException {
        super();

        final URL u = new URL(url);
        //create user service.
        userService = new UserRestClient(UTС);
        userService.setServiceUrl(u);

        //company service
        companyService = new CompanyRestClient(UTС);
        companyService.setServiceUrl(u);

        //device client
        deviceService = new DeviceRestClient(UTС, Language.English, TemperatureUnits.Celsius);
        deviceService.setServiceUrl(u);
    }

    /**
     * @param authToken
     */
    protected void setAuthToken(final String authToken) {
        companyService.setAuthToken(authToken);
        userService.setAuthToken(authToken);
        deviceService.setAuthToken(authToken);
    }

    /**
     * @throws RestServiceException
     * @throws IOException
     *
     */
    protected void initalize(final String userName, final String password)
            throws IOException, RestServiceException {
        final String authToken = userService.login(userName, password);
        setAuthToken(authToken);

        if (user != null) {
            //initialized
            return;
        }

        Company c = null;
        final List<Company> companies = companyService.getCompanies(1, 10000);
        for (final Company company : companies) {
            if (company.getName().equals(COMPANY_NAME)) {
                c = company;
                break;
            }
        }

        if (c == null) {
            throw new RuntimeException("Can't find required company " + COMPANY_NAME + " on server");
        }
        company = c;

        //create user if need
        final String newPassword = "password";
        String token;
        try {
            token = userService.login(EMAIL, newPassword);
        } catch (final Exception e) {
            final User u = new User();
            u.setEmail(EMAIL);
            u.setFirstName("Java (JS)");
            u.setLastName("Developer");
            u.setRoles(new HashSet<Role>());
            u.getRoles().add(Role.Admin);
            u.setCompany(c.getCompanyId());

            userService.saveUser(u, company, newPassword, false);
            token = userService.login(u.getEmail(), newPassword);
        }

        setAuthToken(token);
        user = getUserByEmail(EMAIL);
    }

    /**
     * @param email
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    private User getUserByEmail(final String email) throws IOException, RestServiceException {
        for (final ExpandedListUserItem item : userService.getUsers(null, null, null, null)) {
            if (email.equals(item.getEmail())) {
                return userService.getUser(item.getId());
            }
        }
        return null;
    }
}
