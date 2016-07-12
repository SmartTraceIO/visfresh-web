/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.mpl.services.ArrivalServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockArrivalService extends ArrivalServiceImpl {
    /**
     * Default constructor.
     */
    public MockArrivalService() {
        super();
    }
}
