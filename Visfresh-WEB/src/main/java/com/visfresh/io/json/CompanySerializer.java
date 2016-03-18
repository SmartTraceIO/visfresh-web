/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.constants.CompanyConstants;
import com.visfresh.entities.Company;
import com.visfresh.entities.Language;
import com.visfresh.entities.PaymentMethod;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanySerializer extends AbstractJsonSerializer {
    /**
     * @param tz time zone.
     */
    public CompanySerializer(final TimeZone tz) {
        super(tz);
    }
    /**
     * @param e
     * @return
     */
    public Company parseCompany(final JsonElement e) {
        if (isNull(e)) {
            return null;
        }
        final JsonObject json = e.getAsJsonObject();
        final Company c = new Company();
        c.setDescription(asString(json.get(CompanyConstants.DESCRIPTION)));
        c.setId(asLong(json.get(CompanyConstants.ID)));
        c.setName(asString(json.get(CompanyConstants.NAME)));

        c.setAddress(asString(json.get(CompanyConstants.ADDRESS)));
        c.setContactPerson(asString(json.get(CompanyConstants.CONTACT_PERSON)));
        c.setEmail(asString(json.get(CompanyConstants.EMAIL)));
        if (!isNull(json.get(CompanyConstants.TIME_ZONE))) {
            c.setTimeZone(TimeZone.getTimeZone(asString(json.get(CompanyConstants.TIME_ZONE))));
        }
        c.setStartDate(parseDate(asString(json.get(CompanyConstants.START_DATE))));
        c.setTrackersEmail(asString(json.get(CompanyConstants.TRACKERS_EMAIL)));
        if (!isNull(json.get(CompanyConstants.PAYMENT_METHOD))) {
            c.setPaymentMethod(PaymentMethod.valueOf(asString(json.get(CompanyConstants.PAYMENT_METHOD))));
        }
        c.setBillingPerson(asString(json.get(CompanyConstants.BILLING_PERSON)));
        if (!isNull(json.get(CompanyConstants.LANGUAGE))) {
            c.setLanguage(Language.valueOf(asString(json.get(CompanyConstants.LANGUAGE))));
        }

        return c;
    }
    /**
     * @param e
     * @return
     */
    protected boolean isNull(final JsonElement e) {
        return e == null || e.isJsonNull();
    }
    public JsonObject toJson(final Company c) {
        if (c == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(CompanyConstants.ID, c.getId());
        obj.addProperty(CompanyConstants.NAME, c.getName());
        obj.addProperty(CompanyConstants.DESCRIPTION, c.getDescription());

        obj.addProperty(CompanyConstants.ADDRESS, c.getAddress());
        obj.addProperty(CompanyConstants.CONTACT_PERSON, c.getContactPerson());
        obj.addProperty(CompanyConstants.EMAIL, c.getEmail());
        obj.addProperty(CompanyConstants.TIME_ZONE, c.getTimeZone() == null ? null : c.getTimeZone().getID());
        obj.addProperty(CompanyConstants.START_DATE, formatDate(c.getStartDate()));
        obj.addProperty(CompanyConstants.TRACKERS_EMAIL, c.getTrackersEmail());
        obj.addProperty(CompanyConstants.PAYMENT_METHOD, c.getPaymentMethod() == null ? null : c.getPaymentMethod().name());
        obj.addProperty(CompanyConstants.BILLING_PERSON, c.getBillingPerson());
        obj.addProperty(CompanyConstants.LANGUAGE, c.getLanguage() == null ? null : c.getLanguage().name());
        return obj;
    }
}
