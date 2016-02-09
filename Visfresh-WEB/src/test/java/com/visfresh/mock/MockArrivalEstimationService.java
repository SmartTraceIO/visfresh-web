/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.mpl.services.DefaultArrivalEstimationService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockArrivalEstimationService extends DefaultArrivalEstimationService {
    /**
     * Default constructor.
     */
    public MockArrivalEstimationService() {
        super();
    }
}
