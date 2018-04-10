/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.visfresh.constants.PairedPhoneConstants;
import com.visfresh.entities.PairedPhone;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PairedPhoneSerializer extends AbstractJsonSerializer {
    /**
     * @param tz time zone.
     */
    public PairedPhoneSerializer(final TimeZone tz) {
        super(tz);
    }
    /**
     * @param json JSON object.
     * @return BeaconGateway.
     */
    public PairedPhone parsePairedPhone(final JsonObject json) {
        final PairedPhone g = new PairedPhone();
        g.setId(asLong(json.get(PairedPhoneConstants.ID)));
        g.setActive(!Boolean.FALSE.equals(asBoolean(json.get(PairedPhoneConstants.ACTIVE))));
        g.setImei(asString(json.get(PairedPhoneConstants.BEACON_ID)));
        g.setCompany(asLong(json.get(PairedPhoneConstants.COMAPNY)));
        g.setDescription(asString(json.get(PairedPhoneConstants.DESCRIPTION)));
        g.setBeaconId(asString(json.get(PairedPhoneConstants.IMEI)));
        return g;
    }
    /**
     * @param g BeaconGateway.
     * @return BeaconGateway serialized to JSON format.
     */
    public JsonObject toJson(final PairedPhone g) {
        if (g == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(PairedPhoneConstants.ID, g.getId());
        json.addProperty(PairedPhoneConstants.ACTIVE, g.isActive());
        json.addProperty(PairedPhoneConstants.BEACON_ID, g.getImei());
        json.addProperty(PairedPhoneConstants.COMAPNY, g.getCompany());
        json.addProperty(PairedPhoneConstants.DESCRIPTION, g.getDescription());
        json.addProperty(PairedPhoneConstants.IMEI, g.getBeaconId());
        return json;
    }
}
