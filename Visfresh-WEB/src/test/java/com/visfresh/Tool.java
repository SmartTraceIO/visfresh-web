/**
 *
 */
package com.visfresh;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.visfresh.controllers.RestServiceFacade;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Tool {
    private static final String COMPANY_NAME = "Demo";
    private String USER_NAME = "developer";
    private RestServiceFacade service;
    private Company company;
    private User user;

    /**
     * @param url REST service URL.
     * @param userName user name.
     * @param password password.
     * @throws IOException
     * @throws RestServiceException
     */
    public Tool(final String url, final String userName, final String password) throws IOException, RestServiceException {
        super();
        final RestServiceFacade f = new RestServiceFacade();
        f.setServiceUrl(new URL(url));

        final String authToken = f.login(userName, password);
        f.setAuthToken(authToken);
        this.service = f;
    }

    public void createDevices(final String... imeis) throws RestServiceException, IOException {
        for (final String imei : imeis) {
            createDevice(imei);
        }
    }

    /**
     * @param imei
     * @throws IOException
     * @throws RestServiceException
     */
    private void createDevice(final String imei) throws RestServiceException, IOException {
        initalize();

        final Device device = new Device();
        device.setImei(imei);
        device.setId(imei);
        device.setCompany(company);
        device.setName("DevTool/" + imei.substring(0, imei.length() - 6));

        service.saveDevice(device);
    }

    /**
     * @throws RestServiceException
     * @throws IOException
     *
     */
    private void initalize() throws IOException, RestServiceException {
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

    /**
     * @param args program arguments.
     */
    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            throw new RuntimeException("Password is required as program argument");
        }
        final String serviceUrl = "http://139.162.3.8:8080/web/vf";

        final Tool tool = new Tool(serviceUrl, "globaladmin", args[0]);
        final String[] devices = {
            "354188046489683",
            "354188048733062",
            "354188048733088",
            "358688000000158"
        };

//        tool.createDevices(devices);
        tool.addUser("dan", "Dananjaya Kulathunga", "password");

        System.out.println("Successfully created");
    }

    /**
     * @param login
     * @param fullName
     * @param password
     * @throws RestServiceException
     * @throws IOException
     */
    private void addUser(final String login, final String fullName, final String password) throws IOException, RestServiceException {
        initalize();

        final User u = new User();
        u.setLogin(login);
        u.setFullName(fullName);
        u.getRoles().add(Role.CompanyAdmin);

        service.createUser(u, company, password);
    }
}
