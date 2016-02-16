/**
 *
 */
package com.visfresh.entities;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentTest {

    /**
     * Default constructor.
     */
    public AutoStartShipmentTest() {
        super();
    }

    @Test
    public void testCompare() {
        final AutoStartShipment aut1 = create(3l, 5);
        final AutoStartShipment aut2 = create(2l, 5);
        final AutoStartShipment aut3 = create(1l, 10);

        final List<AutoStartShipment> list = new LinkedList<AutoStartShipment>();
        list.add(aut1);
        list.add(aut2);
        list.add(aut3);

        Collections.sort(list);

        assertEquals(1l, list.get(0).getId().longValue());
        assertEquals(2l, list.get(1).getId().longValue());
        assertEquals(3l, list.get(2).getId().longValue());
    }

    /**
     * @param id autostart shipment ID.
     * @param priority priority.
     * @return autostart shipment.
     */
    private AutoStartShipment create(final long id, final int priority) {
        final AutoStartShipment aut = new AutoStartShipment();
        aut.setId(id);
        aut.setPriority(priority);
        return aut;
    }
}
