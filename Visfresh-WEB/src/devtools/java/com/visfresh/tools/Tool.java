/**
 *
 */
package com.visfresh.tools;

import org.opengts.db.DBConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.User;
import com.visfresh.init.prod.ProductionConfig;
import com.visfresh.services.AuthService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Tool {
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

        //create company
        final Company c = new Company();
        c.setDescription("Test company for development tools");
        c.setName("Development");

        tool.createCompany(c);

        //create user
        final User u = new User();
        u.setCompany(c);
        u.setLogin("vsoldatov");

        tool.createUser(u, "password");

        tool.context.destroy();

        System.out.println("Finished");
    }
}
