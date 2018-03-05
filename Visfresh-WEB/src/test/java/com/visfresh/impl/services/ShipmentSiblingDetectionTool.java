/**
 *
 */
package com.visfresh.impl.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.init.jdbc.JdbcConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@Import({JdbcConfig.class})
@ComponentScan(basePackageClasses = {
        DaoImplBase.class})
public class ShipmentSiblingDetectionTool extends SiblingDetectDispatcher {
    /**
     * @param env
     */
    public ShipmentSiblingDetectionTool(final Environment env) {
        super(env);
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SiblingDetectDispatcher#start()
     */
    @Override
    public void start() {
        // TODO Auto-generated method stub
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SiblingDetectDispatcher#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }

    public static void main(final String[] args) throws IOException {
        final StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addLast(new PropertiesPropertySource(
                "cfg", loadAndReplaceProperties()));

        final AnnotationConfigApplicationContext ctxt = new AnnotationConfigApplicationContext();
        ctxt.setId(ShipmentSiblingDetectionTool.class.getName());
        ctxt.setEnvironment(env);
        ctxt.register(
                ShipmentSiblingDetectionTool.class,
                SyncGroupLockService.class,
                DummyRuleEngine.class);
        ctxt.refresh();

        try {
            final ShipmentSiblingDetectionTool detector = ctxt.getBean(ShipmentSiblingDetectionTool.class);
            detector.updateSiblings(9107l, 40l);
        } finally {
            ctxt.close();
        }
    }

    /**
     * @return
     */
    private static Properties loadAndReplaceProperties() throws IOException {
        final Properties props = new Properties();
        final InputStream in = JdbcConfig.class.getClassLoader().getResourceAsStream("app.properties");
        try {
            props.load(in);
        } finally {
            in.close();
        }

        final String key = "dataSource.url";
        props.put(key, props.getProperty(key).replace("3306", "3307"));
        return props;
    }
}
