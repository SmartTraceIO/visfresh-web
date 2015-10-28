/**
 *
 */
package com.visfresh.junit.db;

import org.springframework.stereotype.Component;

import com.visfresh.io.EntityJSonSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockJsonSerializer extends EntityJSonSerializer {
    /**
     * Default constructor.
     */
    public MockJsonSerializer() {
        super();
    }
}
