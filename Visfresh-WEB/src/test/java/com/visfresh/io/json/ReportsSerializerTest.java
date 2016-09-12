/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.io.EmailShipmentReportRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReportsSerializerTest extends ReportsSerializer {
    private ReportsSerializer serializer;

    /**
     * Default constructor.
     */
    public ReportsSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        this.serializer = new ReportsSerializer();
    }

    @Test
    public void testSerializeEmailShipmentReportRequest() {
        final String messageBody = "Email Message body";
        final String sn = "3290487";
        final String subject = "Email subject";
        final int trip = 12345;

        final String chapaevEmail = "chapaev@smarttrace.com.au";
        final String kotovskyEmail = "kotovsky@smarttrace.com.au";

        final Long u1 = 7l;
        final Long u2 = 8l;
        final Long u3 = 9l;

        EmailShipmentReportRequest req = new EmailShipmentReportRequest();
        req.setMessageBody(messageBody);
        req.setSn(sn);
        req.setSubject(subject);
        req.setTrip(trip);

        req.getEmails().add(chapaevEmail);
        req.getEmails().add(kotovskyEmail);

        req.getUsers().add(u1);
        req.getUsers().add(u2);
        req.getUsers().add(u3);

        req = serializer.parseEmailShipmentReportRequest(serializer.toJson(req));

        assertEquals(messageBody, req.getMessageBody());
        assertEquals(sn, req.getSn());
        assertEquals(subject, req.getSubject());
        assertEquals(trip, req.getTrip());

        assertEquals(2, req.getEmails().size());
        assertEquals(3, req.getUsers().size());

        assertEquals(u3, req.getUsers().get(2));
        assertEquals(kotovskyEmail, req.getEmails().get(1));
    }
}
