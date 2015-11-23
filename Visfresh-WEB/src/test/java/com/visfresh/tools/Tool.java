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
     * @throws IOException
     * @throws RestServiceException
     */
    public Tool(final String url) throws IOException, RestServiceException {
        super(url);
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
        final Device device = new Device();
        device.setImei(imei);
        device.setCompany(company);
        device.setName("DevTool/" + imei.substring(0, imei.length() - 6));

        deviceService.saveDevice(device);
    }

    /**
     * @param args program arguments.
     */
    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            throw new RuntimeException("Password is required as program argument");
        }
        final String serviceUrl = "http://139.162.3.8:8080/web/vf";
//        final String serviceUrl = "http://localhost:8080/web/vf";

        final Tool tool = new Tool(serviceUrl);
        tool.initalize("globaladmin@visfresh.com", args[0]);

        final String[] devices = {
            "354188046489683",
            "354188048733062",
            "354188048733088",
            "358688000000158"
        };

        tool.addUser("dan@visfresh.com", "Dananjaya", "Kulathunga", "password");
        tool.createDevices(devices);

        System.out.println("Successfully created");
    }

    /**
     * @param email
     * @param fullName
     * @param password
     * @throws RestServiceException
     * @throws IOException
     */
    private void addUser(final String email, final String firstName, final String lastName,
            final String password) throws IOException, RestServiceException {
        final User u = new User();
        u.setEmail(email);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.getRoles().add(Role.CompanyAdmin);

        userService.createUser(u, company, password);
    }
}
