/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.mpl.services.SiblingDetectorServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSiblingDetectorService extends SiblingDetectorServiceImpl {
    /**
     * Default constructor.
     */
    public MockSiblingDetectorService() {
        super();
    }
}
