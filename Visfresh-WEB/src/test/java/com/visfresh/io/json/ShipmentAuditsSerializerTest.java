/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.entities.ShipmentAuditItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentAuditsSerializerTest {
    private ShipmentAuditsSerializer serializer;

    /**
     * Default constructor.
     */
    public ShipmentAuditsSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new ShipmentAuditsSerializer(TimeZone.getTimeZone("GMT+3"));
    }

    @Test
    public void testSerialize() {
        ShipmentAuditItem item = new ShipmentAuditItem();

        final ShipmentAuditAction action = ShipmentAuditAction.DeletedNote;
        final Long id = 7l;
        final long shipmentId = 17l;
        final Date time = new Date(System.currentTimeMillis() - 1000000000000l);
        final Long userId = 99l;
        final String value1 = "value1";
        final String key1 = "key1";
        final String value2 = "value2";
        final String key2 = "key2";

        item.setAction(action);
        item.setId(id);
        item.setShipmentId(shipmentId);
        item.setTime(time);
        item.setUserId(userId);
        item.getAdditionalInfo().put(key1, value1);
        item.getAdditionalInfo().put(key2, value2);

        item = serializer.parseShipmentAuditItem(serializer.toJson(item));

        assertEquals(action, item.getAction());
        assertEquals(id, item.getId());
        assertEquals(shipmentId, item.getShipmentId());
        assertTrue(Math.abs(time.getTime() - item.getTime().getTime()) < 60100l);
        assertEquals(userId, item.getUserId());
        assertEquals(value1, item.getAdditionalInfo().get(key1));
        assertEquals(value2, item.getAdditionalInfo().get(key2));
    }
    @Test
    public void testSerializeWithNullUser() {
        ShipmentAuditItem item = new ShipmentAuditItem();

        final ShipmentAuditAction action = ShipmentAuditAction.DeletedNote;
        final Long id = 7l;
        final long shipmentId = 17l;
        final Date time = new Date(System.currentTimeMillis() - 1000000000000l);
        final String value1 = "value1";
        final String key1 = "key1";
        final String value2 = "value2";
        final String key2 = "key2";

        item.setAction(action);
        item.setId(id);
        item.setShipmentId(shipmentId);
        item.setTime(time);
        item.getAdditionalInfo().put(key1, value1);
        item.getAdditionalInfo().put(key2, value2);

        item = serializer.parseShipmentAuditItem(serializer.toJson(item));

        assertEquals(action, item.getAction());
        assertEquals(id, item.getId());
        assertEquals(shipmentId, item.getShipmentId());
        assertTrue(Math.abs(time.getTime() - item.getTime().getTime()) < 60100l);
        assertNull(item.getUserId());
        assertEquals(value1, item.getAdditionalInfo().get(key1));
        assertEquals(value2, item.getAdditionalInfo().get(key2));
    }
}
