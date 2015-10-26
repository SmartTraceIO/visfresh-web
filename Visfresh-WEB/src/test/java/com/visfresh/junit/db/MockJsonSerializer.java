/**
 *
 */
package com.visfresh.junit.db;

import org.springframework.stereotype.Component;

import com.visfresh.io.JSonSerializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockJsonSerializer extends JSonSerializer {
    /**
     * Default constructor.
     */
    public MockJsonSerializer() {
        super();
    }
}
