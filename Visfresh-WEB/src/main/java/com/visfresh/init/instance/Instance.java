/**
 *
 */
package com.visfresh.init.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class Instance {
    private String id;

    /**
     * Default constructor.
     */
    @Autowired
    public Instance(final Environment env) {
        super();
        this.id = env.getProperty("instance.id");
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }
}
