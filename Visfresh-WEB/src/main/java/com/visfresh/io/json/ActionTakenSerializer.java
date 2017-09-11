/**
 *
 */
package com.visfresh.io.json;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.visfresh.constants.ActionTakenConstants;
import com.visfresh.entities.ActionTaken;
import com.visfresh.entities.ActionTakenView;
import com.visfresh.entities.Language;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ActionTakenSerializer extends AbstractJsonSerializer  implements ActionTakenConstants {
    private final TemperatureUnits units;
    private RuleBundle ruleBundle;
    private final DateFormat prettyFormat;

    /**
     * @param tz
     */
    public ActionTakenSerializer(final Language lang, final TimeZone tz, final TemperatureUnits units,
            final RuleBundle ruleBundle) {
        super(tz);
        this.units = units;
        this.ruleBundle = ruleBundle;
        prettyFormat = DateTimeUtils.createPrettyFormat(lang, tz);
    }

    /**
     * @param json
     * @return
     */
    public ActionTaken parseActionTaken(final JsonObject json) {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        final ActionTaken at = new ActionTaken();
        at.setId(asLong(json.get(ID)));
        at.setAction(CorrectiveActionListSerializer.parseCorrectiveAction(json.get(ACTION)));
        at.setTime(parseDate(asString(json.get(TIME))));
        at.setCreatedOn(parseDate(asString(json.get(CREATED_ON))));
        at.setVerifiedTime(parseDate(asString(json.get(VERIFIED_TIME))));
        at.setComments(asString(json.get(COMMENTS)));
        at.setVerifiedComments(asString(json.get(VERIFIED_COMMENTS)));
        at.setAlert(asLong(json.get(ALERT)));
        at.setConfirmedBy(asLong(json.get(CONFIRMED_BY)));
        at.setVerifiedBy(asLong(json.get(VERIFIED_BY)));
        return at;
    }

    /**
     * @param at
     * @return
     */
    public JsonObject toJson(final ActionTakenView at) {
        final JsonObject json = createJsonWithBaseParams(at);

        //view constants
        json.addProperty("timePretty", formatDatePretty(at.getTime()));
        json.addProperty("createdOnPretty", formatDatePretty(at.getCreatedOn()));

        json.addProperty(ALERT_TIME, formatDate(at.getAlertTime()));
        json.addProperty(ALERT_DESCRIPTION, ruleBundle.buildDescription(at.getAlertRule(), units));
        json.addProperty(CONFIRMED_BY_EMAIL, at.getConfirmedByEmail());
        json.addProperty(CONFIRMED_BY_NAME, at.getConfirmedByName());
        json.addProperty(VERIFIED_BY_EMAIL, at.getVerifiedByEmail());
        json.addProperty(VERIFIED_BY_NAME, at.getVerifiedByName());
        json.addProperty(SHIPMENT_SN, at.getShipmentSn());
        json.addProperty(SHIPMENT_TRIP_COUNT, at.getShipmentTripCount());

        return json;
    }

    /**
     * @param date
     * @return
     */
    private String formatDatePretty(final Date date) {
        return date == null ? null : prettyFormat.format(date);
    }

    /**
     * @param at action taken.
     * @return JSON representation of action taken.
     */
    public JsonObject createJsonWithBaseParams(final ActionTaken at) {
        final JsonObject json = new JsonObject();
        json.addProperty(ID, at.getId());
        json.add(ACTION, CorrectiveActionListSerializer.toJson(at.getAction()));
        json.addProperty(TIME, formatDate(at.getTime()));
        json.addProperty(CREATED_ON, formatDate(at.getCreatedOn()));
        json.addProperty(VERIFIED_TIME, formatDate(at.getVerifiedTime()));
        json.addProperty(COMMENTS, at.getComments());
        json.addProperty(VERIFIED_COMMENTS, at.getVerifiedComments());
        json.addProperty(ALERT, at.getAlert());
        json.addProperty(CONFIRMED_BY, at.getConfirmedBy());
        json.addProperty(VERIFIED_BY, at.getVerifiedBy());
        return json;
    }
}
