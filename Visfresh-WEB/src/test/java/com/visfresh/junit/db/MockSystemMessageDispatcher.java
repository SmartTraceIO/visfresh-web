/**
 *
 */
package com.visfresh.junit.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.mpl.services.SystemMessageDispatcher;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSystemMessageDispatcher extends SystemMessageDispatcher {
    /**
     * @param env
     */
    @Autowired
    public MockSystemMessageDispatcher(final Environment env) {
        super(env);
    }

}
