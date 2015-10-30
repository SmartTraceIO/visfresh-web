/**
 *
 */
package com.visfresh.drools;

import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.services.RuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(RuleEngineTestRunner.class)
public class BaseRuleTest {
    protected RuleEngine engine;
    protected AnnotationConfigApplicationContext context;
    protected Company company;

    public void setRuleEngine(final RuleEngine engine) {
        this.engine = engine;
    }
    public void setSpringContext(final AnnotationConfigApplicationContext context) {
        this.context = context;
        //create shared company
        final Company c = new Company();
        c.setName("JUnit company");
        c.setDescription("Any Description");
        context.getBean(CompanyDao.class).save(c);
        this.company = c;
    }

    /**
     * @return the context
     */
    public AnnotationConfigApplicationContext getContext() {
        return context;
    }
    /**
     * @return the engine
     */
    public RuleEngine getEngine() {
        return engine;
    }

    /**
     * @param imei
     * @return
     */
    protected Device createDevice(final String imei) {
        final Device d = new Device();
        d.setName("Test Device");
        d.setImei(imei);
        d.setId(d.getImei());
        d.setSn("456");
        d.setCompany(company);
        d.setDescription("Test device");
        return context.getBean(DeviceDao.class).save(d);
    }
    /**
     * @param name shipment name.
     * @param status shipment status
     * @return
     */
    protected Shipment createDefaultShipment(final String name, final ShipmentStatus status, final Device device) {
        final Shipment s = new Shipment();
        s.setDevice(device);
        s.setCompany(company);
        s.setName(name);
        s.setStatus(status);
        return context.getBean(ShipmentDao.class).save(s);
    }
}
