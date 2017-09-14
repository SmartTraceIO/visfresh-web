/**
 *
 */
package com.visfresh.dao;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.visfresh.junit.db.JUnitDbConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DbTestRunner extends BlockJUnit4ClassRunner {
    private static AbstractApplicationContext context = createContext();
    private BaseDbTest test;

    /**
     * @param klass
     * @throws InitializationError
     */
    public DbTestRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * @return
     */
    private static AbstractApplicationContext createContext() {
        final AnnotationConfigApplicationContext ctxt = new AnnotationConfigApplicationContext();
        ctxt.setId(DbTestRunner.class.getName());
        ctxt.scan(JUnitDbConfig.class.getPackage().getName());
        ctxt.refresh();
        return ctxt;
    }

    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        final Object test = super.createTest();
        if (test instanceof BaseDbTest) {
            this.test = ((BaseDbTest) test);
            this.test.initialize(context);
        }
        return test;
    }
    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#runChild(org.junit.runners.model.FrameworkMethod, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        try {
            super.runChild(method, notifier);
        } finally {
            if (this.test != null) {
                clearDb(context);
            }
        }
    }

    /**
     * @param context
     *
     */
    public static void clearDb(final AbstractApplicationContext context) {
        context.getBean(DeviceLockDao.class).deleteAll();
        context.getBean(SystemMessageDao.class).deleteAll();
        context.getBean(TrackerEventDao.class).deleteAll();
        context.getBean(DeviceCommandDao.class).deleteAll();
        context.getBean(NotificationDao.class).deleteAll();
        context.getBean(AlertDao.class).deleteAll();
        context.getBean(ArrivalDao.class).deleteAll();
        context.getBean(ShipmentDao.class).deleteAll();
        context.getBean(ShipmentTemplateDao.class).deleteAll();
        context.getBean(AlertProfileDao.class).deleteAll();
        context.getBean(CorrectiveActionListDao.class).deleteAll();
        context.getBean(LocationProfileDao.class).deleteAll();
        context.getBean(NotificationScheduleDao.class).deleteAll();
        context.getBean(DeviceGroupDao.class).deleteAll();
        context.getBean(DeviceDao.class).deleteAll();
        context.getBean(UserDao.class).deleteAll();
        context.getBean(CompanyDao.class).deleteAll();
    }
}
