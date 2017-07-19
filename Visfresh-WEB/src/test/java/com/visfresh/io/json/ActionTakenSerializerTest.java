/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.constants.ActionTakenConstants;
import com.visfresh.entities.ActionTaken;
import com.visfresh.entities.ActionTakenView;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.l12n.RuleBundle;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ActionTakenSerializerTest {
    private ActionTakenSerializer serializer;
    private RuleBundle bundle;

    /**
     * Default constructor.
     */
    public ActionTakenSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        bundle = new RuleBundle();
        serializer = new ActionTakenSerializer(TimeZone.getDefault(), TemperatureUnits.Celsius,
                bundle);
    }

    @Test
    public void testSerialize() {
        final TemperatureRule rule = new TemperatureRule(AlertType.CriticalHot);
        rule.setCumulativeFlag(true);
        rule.setMaxRateMinutes(34);
        rule.setTemperature(19.0);
        rule.setTimeOutMinutes(15);

        final Long alert = 1l;
        final Date alertTime = new Date(System.currentTimeMillis() - 10000000l);
        final String comments = "Any comments";
        final Long confirmedBy = 2l;
        final String confirmedByEmail = confirmedBy + "@junit.ru";
        final String confirmedByName = "Name2";
        final Long id = 3l;
        final String shipmentSn = "777";
        final int shipmentTripCount = 5;
        final Date time = new Date(1000000l);
        final Date verifiedTime = new Date(time.getTime() + 3600000l);
        final Long verifiedBy = 8l;
        final String verifiedByEmail = verifiedBy + "@junit.ru";
        final String verifiedByName= "Name2";
        final CorrectiveAction action = new CorrectiveAction("Any action");

        final ActionTakenView at = new ActionTakenView();
        at.setAction(action);
        at.setAlert(alert);
        at.setAlertRule(rule);
        at.setAlertTime(alertTime);
        at.setComments(comments);
        at.setConfirmedBy(confirmedBy);
        at.setConfirmedByEmail(confirmedByEmail);
        at.setConfirmedByName(confirmedByName);
        at.setId(id);
        at.setShipmentSn(shipmentSn);
        at.setShipmentTripCount(shipmentTripCount);
        at.setTime(time);
        at.setVerifiedTime(verifiedTime);
        at.setVerifiedBy(verifiedBy);
        at.setVerifiedByEmail(verifiedByEmail);
        at.setVerifiedByName(verifiedByName);

        final JsonObject json = serializer.toJson(at);

        final ActionTaken actual = serializer.parseActionTaken(json);

        assertEquals(actual.getAction().getAction(), action.getAction());
        assertEquals(actual.getAction().isRequestVerification(), action.isRequestVerification());
        assertEquals(actual.getAlert(), alert);
        assertEquals(actual.getComments(), comments);
        assertEquals(actual.getConfirmedBy(), confirmedBy);
        assertEquals(actual.getId(), id);
        assertTrue(Math.abs(actual.getTime().getTime() - at.getTime().getTime()) < 61000l);
        assertTrue(Math.abs(actual.getVerifiedTime().getTime() - at.getVerifiedTime().getTime()) < 61000l);
        assertEquals(actual.getVerifiedBy(), verifiedBy);

        assertEquals(json.get(ActionTakenConstants.ALERT_DESCRIPTION).getAsString(),
                bundle.buildDescription(rule, TemperatureUnits.Celsius));
    }
}
