/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.io.DefaultShipmentDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultShipmentSerializerTest {
    private DefaultShipmentSerializer serializer;

    /**
     * Default constructor.
     */
    public DefaultShipmentSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new DefaultShipmentSerializer(TimeZone.getDefault());
    }

    @Test
    public void testSerialize() {
        DefaultShipmentDto dto = new DefaultShipmentDto();
        final Long id = 7l;
        final Long template = 8l;
        final Long loc1 = 1l;
        final Long loc2 = 2l;
        final Long loc3 = 3l;
        final Long loc4 = 4l;

        dto.setId(id);
        dto.setTemplate(template);
        dto.getStartLocations().add(loc1);
        dto.getStartLocations().add(loc2);
        dto.getEndLocations().add(loc3);
        dto.getEndLocations().add(loc4);

        dto = serializer.parseDefaultShipmentDto(serializer.toJson(dto));

        assertEquals(id, dto.getId());
        assertEquals(template, dto.getTemplate());
        assertEquals(loc1, dto.getStartLocations().get(0));
        assertEquals(loc2, dto.getStartLocations().get(1));
        assertEquals(loc3, dto.getEndLocations().get(0));
        assertEquals(loc4, dto.getEndLocations().get(1));
    }
}
