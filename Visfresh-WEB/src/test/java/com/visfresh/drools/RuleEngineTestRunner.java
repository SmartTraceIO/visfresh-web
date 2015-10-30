/**
 *
 */
package com.visfresh.drools;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.mock.MockDaoConfig;
import com.visfresh.mock.MockSmsService;
import com.visfresh.services.RuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RuleEngineTestRunner extends BlockJUnit4ClassRunner {
    private AnnotationConfigApplicationContext context = createContext();

    /**
     * @param klass
     * @throws InitializationError
     */
    public RuleEngineTestRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * @return spring context.
     */
    private AnnotationConfigApplicationContext createContext() {
        final AnnotationConfigApplicationContext ctxt = new AnnotationConfigApplicationContext();
        ctxt.scan(MockDaoConfig.class.getPackage().getName(),
            DroolsRuleEngine.class.getPackage().getName(),
            MockSmsService.class.getPackage().getName());
        ctxt.refresh();
        return ctxt;
    }
    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#runChild(org.junit.runners.model.FrameworkMethod, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        try {
            super.runChild(method, notifier);
        } finally {
            cleanUp();
        }
    }

    /**
     *
     */
    private void cleanUp() {
        context.getBean(NotificationDao.class).deleteAll();
        context.getBean(TrackerEventDao.class).deleteAll();
        context.getBean(ArrivalDao.class).deleteAll();
        context.getBean(AlertDao.class).deleteAll();
        context.getBean(ShipmentDao.class).deleteAll();
        context.getBean(LocationProfileDao.class).deleteAll();
        context.getBean(AlertProfileDao.class).deleteAll();
        context.getBean(NotificationScheduleDao.class).deleteAll();
        context.getBean(DeviceDao.class).deleteAll();
        context.getBean(CompanyDao.class).deleteAll();
    }

    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        final Object test = super.createTest();
        if (test instanceof BaseRuleTest) {
            final BaseRuleTest rt = (BaseRuleTest) test;
            rt.setSpringContext(context);
            rt.setRuleEngine(context.getBean(RuleEngine.class));
        }
        return test;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            context.destroy();
            System.out.println("Application context for rule engine has destroyed");
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        super.finalize();
    }
}
