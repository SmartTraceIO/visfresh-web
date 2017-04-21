/**
 *
 */
package com.visfresh.mock;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.User;
import com.visfresh.mpl.services.NotificationServiceImpl;
import com.visfresh.services.RetryableException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockNotificationService extends NotificationServiceImpl {
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.NotificationServiceImpl#sendShipmentReport(com.visfresh.entities.Shipment, java.util.List)
     */
    @Override
    public void sendShipmentReport(final Shipment shipment, final List<User> users) {
        final JsonObject json = createSendShipmentReportMessage(shipment, users);

        //create and handle system message synchronously
        final SystemMessage msg = new SystemMessage();
        msg.setMessageInfo(json.toString());
        msg.setRetryOn(new Date());
        msg.setTime(new Date());

        try {
            handle(msg);
        } catch (final RetryableException e) {
            e.printStackTrace();
        }
    }
}
