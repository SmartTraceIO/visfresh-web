/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.io.AutoStartShipmentDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentSerializerTest {
    private AutoStartShipmentSerializer serializer;

    /**
     * Default constructor.
     */
    public AutoStartShipmentSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new AutoStartShipmentSerializer(TimeZone.getDefault());
    }

    @Test
    public void testSerialize() {
        AutoStartShipmentDto dto = new AutoStartShipmentDto();
        final int priority = 5;
        final Long id = 7l;
        final Long template = 8l;
        final Long loc1 = 1l;
        final Long loc2 = 2l;
        final Long loc3 = 3l;
        final Long loc4 = 4l;

        dto.setId(id);
        dto.setTemplate(template);
        dto.setPriority(priority);
        dto.getStartLocations().add(loc1);
        dto.getStartLocations().add(loc2);
        dto.getEndLocations().add(loc3);
        dto.getEndLocations().add(loc4);

        dto = serializer.parseAutoStartShipmentDto(serializer.toJson(dto));

        assertEquals(id, dto.getId());
        assertEquals(priority, dto.getPriority());
        assertEquals(template, dto.getTemplate());
        assertEquals(loc1, dto.getStartLocations().get(0));
        assertEquals(loc2, dto.getStartLocations().get(1));
        assertEquals(loc3, dto.getEndLocations().get(0));
        assertEquals(loc4, dto.getEndLocations().get(1));
    }
}
