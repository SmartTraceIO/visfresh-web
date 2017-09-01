/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.impl.services.DefaultShipmentAuditService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class JunitShipmentAuditService extends DefaultShipmentAuditService {
    /**
     * Default constructor.
     */
    public JunitShipmentAuditService() {
        super();
    }
}
