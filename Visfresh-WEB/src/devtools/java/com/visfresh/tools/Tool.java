/**
 *
 */
package com.visfresh.tools;

import java.util.List;

import org.opengts.db.DBConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.User;
import com.visfresh.init.prod.ProductionConfig;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Tool {
    /**
     *
     */
    private static final String COMPANY_NAME = "Development";
    /**
     * Context.
     */
    private AnnotationConfigApplicationContext context;

    /**
     *
     */
    public Tool() {
        super();
        final AnnotationConfigApplicationContext ctxt = new AnnotationConfigApplicationContext();
        ctxt.scan(ProductionConfig.class.getPackage().getName());
        ctxt.refresh();
        this.context = ctxt;
    }

    public void createCompany(final Company c) {
        final CompanyDao dao = context.getBean(CompanyDao.class);
        dao.save(c);
    }
    public void createUser(final User user, final String password) {
        final AuthService service = context.getBean(AuthService.class);
        service.createUser(user, password);
    }

    /**
     * @param args program arguments. Should contain config location.
     * -conf=${resource_loc:/Visfresh-WEB/src/main/webapp/WEB-INF/common.conf}
     */
    public static void main(final String[] args) {
        final String cfgArg = args[0];
        //init GTS(e)
        DBConfig.cmdLineInit(new String[]{cfgArg}, true);

        final Tool tool = new Tool();

        try {
            createUserAndCompany(tool);
            createToolDevice(tool);
        } finally {
            tool.context.destroy();
        }

        System.out.println("Finished");
    }

    /**
     * @param tool
     */
    private static void createToolDevice(final Tool tool) {
        final Device device = new Device();
        device.setImei("358688000000158");
        device.setId(device.getId() + ".0");
        device.setCompany(getToolCompany(tool));
        device.setName("DevTool Device");

        tool.createDevice(device);
    }

    /**
     * @param device device.
     */
    private void createDevice(final Device device) {
        final RestService rest = context.getBean(RestService.class);
        rest.saveDevice(device.getCompany(), device);
    }

    /**
     * @return
     */
    private static Company getToolCompany(final Tool tool) {
        final CompanyDao dao = tool.context.getBean(CompanyDao.class);
        final List<Company> all = dao.findAll();
        for (final Company company : all) {
            if (company.getName().equals(COMPANY_NAME)) {
                return company;
            }
        }
        return null;
    }

    /**
     * @param tool
     */
    protected static void createUserAndCompany(final Tool tool) {
        //create company
        Company c = getToolCompany(tool);
        if (c == null) {
            c = new Company();
            c.setDescription("Test company for development tools");
            c.setName(COMPANY_NAME);

            tool.createCompany(c);
        }

        //create user
        final User u = new User();
        u.setCompany(c);
        u.setLogin("vsoldatov");

        tool.createUser(u, "password");
    }
}
