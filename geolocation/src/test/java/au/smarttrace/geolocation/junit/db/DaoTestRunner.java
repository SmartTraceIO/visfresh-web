/**
 *
 */
package au.smarttrace.geolocation.junit.db;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DaoTestRunner extends BlockJUnit4ClassRunner {

    private static AbstractApplicationContext context = createContext();

    /**
     * @param klass testing class.
     */
    public DaoTestRunner(final Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * @return
     */
    private static AbstractApplicationContext createContext() {
        final AnnotationConfigApplicationContext ctxt = new AnnotationConfigApplicationContext();
        ctxt.setId(DaoTestRunnerConfig.class.getName());
        ctxt.scan(DaoTestRunnerConfig.class.getPackage().getName());
        ctxt.refresh();
        return ctxt;
    }

    /* (non-Javadoc)
     * @see org.junit.runners.BlockJUnit4ClassRunner#createTest()
     */
    @Override
    protected Object createTest() throws Exception {
        final Object test = super.createTest();
        final AutowireCapableBeanFactory f = context.getAutowireCapableBeanFactory();
        f.autowireBean(test);
        return test;
    }
}
