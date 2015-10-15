/**
 *
 */
package com.visfresh.junit.jpa;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.init.JpaConfigBase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {DaoImplBase.class})
public class JUnitJpaConfig extends JpaConfigBase {
    /**
     * Default constructor.
     */
    public JUnitJpaConfig() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.init.JpaConfigBase#getPropertyFilePrefix()
     */
    @Override
    protected String getPropertyFilePrefix() {
        return "junit";
    }

    /* (non-Javadoc)
     * @see com.visfresh.init.JpaConfigBase#annotationDrivenTransactionManager()
     */
    @Override
    @Bean
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return super.annotationDrivenTransactionManager();
    }
    /* (non-Javadoc)
     * @see com.visfresh.init.JpaConfigBase#configureDataSource()
     */
    @Override
    @Bean
    public DataSource configureDataSource() {
        return super.configureDataSource();
    }
    /* (non-Javadoc)
     * @see com.visfresh.init.JpaConfigBase#configureEntityManagerFactory()
     */
    @Override
    @Bean
    public LocalContainerEntityManagerFactoryBean configureEntityManagerFactory() {
        return super.configureEntityManagerFactory();
    }
}
