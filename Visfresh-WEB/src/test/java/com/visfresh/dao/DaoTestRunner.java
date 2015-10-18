/**
 *
 */
package com.visfresh.dao;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.visfresh.junit.jpa.JUnitJpaConfig;

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
     * @see org.junit.runners.ParentRunner#withBeforeClasses(org.junit.runners.model.Statement)
     */
    @Override
    protected Statement withBeforeClasses(final Statement statement) {
        final AnnotationConfigWebApplicationContext ctxt = new AnnotationConfigWebApplicationContext();
        ctxt.scan(JUnitJpaConfig.class.getPackage().getName());
        ctxt.refresh();
        context = ctxt;
        final Statement st = super.withBeforeClasses(statement);
        return st;
    }
    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        context.refresh();
        final Object test = super.createTest();
        if (test instanceof BaseCrudTest) {
            this.test = ((BaseCrudTest<?, ?, ?>) test);
            this.test.initialize(context);
        }
        return test;
    }
    /* (non-Javadoc)
     * @see org.junit.runners.ParentRunner#withAfterClasses(org.junit.runners.model.Statement)
     */
    @Override
    protected Statement withAfterClasses(final Statement statement) {
        final Statement st = super.withAfterClasses(statement);
        if (context != null) {
            if (this.test != null) {
                this.test.destroy();
            }
            context.destroy();
        }
        return st;
    }
}
