/**
 *
 */
package com.visfresh.impl.services;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.email.EmailServiceHelper;
import com.visfresh.l12n.NotificationBundle;
import com.visfresh.services.RetryableException;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SendNotificationTool extends NotificationServiceImpl {
    /**
     * Default constructor.
     */
    public SendNotificationTool() {
        super();

        //initialize email service
        final EmailServiceHelper h = new EmailServiceHelper();
        h.setUseSsl(true);
        h.setSender("api@smarttrace.com.au");
        h.setSmtpHost("mail.messagingengine.com");
        h.setSmtpPort(465);
        h.setUser("api@smarttrace.com.au");
        h.setPassword("figspace");

        this.emailService = new EmailServiceImpl(h) {
            /* (non-Javadoc)
             * @see com.visfresh.mpl.services.EmailServiceImpl#sendSystemMessage(java.lang.String, com.visfresh.entities.SystemMessageType)
             */
            @Override
            protected void sendSystemMessage(final String payload,
                    final SystemMessageType type) {
                final SystemMessage msg = new SystemMessage();
                msg.setId(1l);
                msg.setMessageInfo(payload);
                msg.setType(type);

                try {
                    handle(msg);
                } catch (final RetryableException e) {
                    e.printStackTrace();
                }
            }
        };

        //create bundle
        bundle = new NotificationBundle();
    }

    public static void main(final String[] args) throws ParseException {
        //create company
        final Company c = new Company(1l);
        c.setId(7l);
        c.setName("Gotzinger");
        c.setDescription("Gotzinger Smallgoods");

        //create device
        final Device d = new Device();
        d.setImei("354430070005732");
        d.setName("Gotzinger 573");
        d.setDescription("Gotzinger trial for 573");
        d.setCompany(c);
        d.setActive(true);

        //create shipment
        final Shipment s = new Shipment();
        s.setId(799l);
        s.setTripCount(5);
        s.setDevice(d);
        s.setCompany(c);

        //create tracker event.
        final TrackerEvent e = new TrackerEvent();
        e.setId(1l);
        e.setLatitude(-33.76691);
        e.setLongitude(150.662226);
        e.setTemperature(3.62);
        e.setShipment(s);
        e.setTime(DateTimeUtils.createIsoFormat(Language.English, TimeZone.getDefault()).parse("2016-04-19 06:11"));

        //create battery low alert
        final Alert a = new Alert();
        a.setType(AlertType.Battery);
        a.setDate(e.getTime());
        a.setDevice(d);
        a.setShipment(s);
        a.setTrackerEventId(e.getId());

        final SendNotificationTool tool = new SendNotificationTool();

        final String[] emails = {"james@smarttrace.com.au", "vyacheslav.soldatov@inbox.ru"};

        final List<User> users = new LinkedList<>();
        for (final String email : emails) {
            final User user = new User();
            user.setEmail(email);
            user.setFirstName(email);
            users.add(user);
        }

        tool.sendEmailNotification(a, users, e);
    }
}
