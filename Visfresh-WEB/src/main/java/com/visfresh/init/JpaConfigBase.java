/**
 *
 */
package com.visfresh.init;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import com.visfresh.entities.Device;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Base JPA configuration for test and production environment.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class JpaConfigBase implements TransactionManagementConfigurer {
    protected final Properties configuration;

    /**
     * Default constructor.
     */
    public JpaConfigBase() {
        super();

        final Properties props = new Properties();
        final InputStream in = new BufferedInputStream(JpaConfigBase.class.getClassLoader().getResourceAsStream(
                getPropertyFilePrefix() + ".jpa.configuration"));
        try {
            try {
                props.load(in);
            } finally {
                in.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        this.configuration = props;

    }

    /**
     * @return JDBC data source.
     */
    public DataSource configureDataSource() {
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName(configuration.getProperty("dataSource.driverClassName"));
        config.setJdbcUrl(configuration.getProperty("dataSource.url"));
        config.setUsername(configuration.getProperty("dataSource.username"));
        config.setPassword(configuration.getProperty("dataSource.password"));

        return new HikariDataSource(config);
    }

    /**
     * @return EntityManager factory.
     */
    public LocalContainerEntityManagerFactoryBean configureEntityManagerFactory() {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(configureDataSource());
        entityManagerFactoryBean.setPackagesToScan(Device.class.getPackage().getName());
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        final Properties jpaProperties = new Properties();
        jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, configuration.getProperty("hibernate.dialect"));
        jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO,
                configuration.getProperty("hibernate.hbm2ddl.auto"));
        entityManagerFactoryBean.setJpaProperties(jpaProperties);

        return entityManagerFactoryBean;
    }

    /* (non-Javadoc)
     * @see org.springframework.transaction.annotation.TransactionManagementConfigurer#annotationDrivenTransactionManager()
     */
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new JpaTransactionManager();
    }
    /**
     * @return returns the prefix of configuration file.
     */
    protected abstract String getPropertyFilePrefix();
}
