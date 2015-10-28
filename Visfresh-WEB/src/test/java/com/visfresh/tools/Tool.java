/**
 *
 */
package com.visfresh.tools;

import java.io.IOException;

import com.visfresh.entities.Device;
import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Tool extends AbstractTool {
    /**
     * @param url REST service URL.
     * @param userName user name.
     * @param password password.
     * @throws IOException
     * @throws RestServiceException
     */
    public Tool(final String url, final String userName, final String password) throws IOException, RestServiceException {
        super(url, userName, password);
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
     * @param args program arguments.
     */
    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            throw new RuntimeException("Password is required as program argument");
        }
//        final String serviceUrl = "http://139.162.3.8:8080/web/vf";
        final String serviceUrl = "http://localhost:8080/web/vf";

        final Tool tool = new Tool(serviceUrl, "globaladmin", args[0]);
        final String[] devices = {
            "354188046489683",
            "354188048733062",
            "354188048733088",
            "358688000000158"
        };

        tool.addUser("dan", "Dananjaya Kulathunga", "password");
        tool.createDevices(devices);

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
