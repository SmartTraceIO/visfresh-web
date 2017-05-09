/**
 *
 */
package com.visfresh.rules;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.dao.DaoTestRunner;
import com.visfresh.junit.db.JUnitDbConfig;
import com.visfresh.mock.MockEmailService;
import com.visfresh.mock.MockNotificationService;
import com.visfresh.mock.MockRestSessionManager;
import com.visfresh.mock.MockShipmentAuditService;
import com.visfresh.services.RuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RuleEngineTestRunner extends BlockJUnit4ClassRunner {
    private static AnnotationConfigApplicationContext context = createContext();

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
    private static  AnnotationConfigApplicationContext createContext() {
        final AnnotationConfigApplicationContext ctxt = new AnnotationConfigApplicationContext() {

            /* (non-Javadoc)
             * @see java.lang.Object#finalize()
             */
            @Override
            protected void finalize() throws Throwable {
                try {
                    destroy();
                    System.out.println("Application context for rule engine has destroyed");
                } catch (final Throwable t) {
                    t.printStackTrace();
                }
                super.finalize();
            }
        };
        ctxt.scan(JUnitDbConfig.class.getPackage().getName(),
            MockNotificationService.class.getPackage().getName());
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
        DaoTestRunner.clearDb(context);
        try {
            context.getBean(MockEmailService.class).clear();
            context.getBean(MockShipmentAuditService.class).clear();
            context.getBean(MockRestSessionManager.class).clear();
        } catch (final Exception e) {}
    }
    /**
     * @return the context
     */
    public static AnnotationConfigApplicationContext getContext() {
        return context;
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
}
