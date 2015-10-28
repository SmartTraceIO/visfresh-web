/**
 *
 */
package com.visfresh.tools;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.visfresh.controllers.RestServiceFacade;
import com.visfresh.entities.Company;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractTool {

    public static final String COMPANY_NAME = "Demo";
    protected String USER_NAME = "developer";
    protected RestServiceFacade service;
    protected Company company;
    protected User user;

    /**
     *
     */
    public AbstractTool(final String url, final String userName, final String password)
            throws IOException, RestServiceException {
        super();
        final RestServiceFacade f = new RestServiceFacade();
        f.setServiceUrl(new URL(url));

        final String authToken = f.login(userName, password);
        f.setAuthToken(authToken);
        this.service = f;
    }

    /**
     * @throws RestServiceException
     * @throws IOException
     *
     */
    protected void initalize() throws IOException, RestServiceException {
        if (user != null) {
            //initialized
            return;
        }

        Company c = null;
        final List<Company> companies = service.getCompanies(1, 10000);
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
        final String password = "password";
        User u = service.getUser(USER_NAME);
        if (u == null) {
            u = new User();
            u.setLogin(USER_NAME);
            u.setFullName("Developer");
            u.getRoles().add(Role.CompanyAdmin);

            service.createUser(u, c, password);
        }
        user = u;

        //relogin
        final String authToken = service.login(user.getLogin(), "password");
        service.setAuthToken(authToken);
    }
}
