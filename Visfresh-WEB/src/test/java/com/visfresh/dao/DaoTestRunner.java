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
public class DaoTestRunner extends BlockJUnit4ClassRunner {
    private AbstractApplicationContext context;
    private BaseCrudTest<?, ?, ?> test;

    /**
     * @param klass
     * @throws InitializationError
     */
    public DaoTestRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        final Object test = super.createTest();
        if (test instanceof BaseCrudTest) {
            this.test = ((BaseCrudTest<?, ?, ?>) test);
            this.test.initialize(context);
        }
        return test;
    }
    /* (non-Javadoc)
     * @see org.junit.runners.ParentRunner#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    public void run(final RunNotifier notifier) {
        final AnnotationConfigApplicationContext ctxt = new AnnotationConfigApplicationContext();
        ctxt.scan(JUnitDbConfig.class.getPackage().getName());
        ctxt.refresh();
        context = ctxt;

        try {
            super.run(notifier);
        } finally {
            context.destroy();
        }
    }
    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#runChild(org.junit.runners.model.FrameworkMethod, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        try {
            super.runChild(method, notifier);
        } finally {
            if (context != null) {
                if (this.test != null) {
                    this.test.handleFinished();
                }
            }
        }
    }
}
