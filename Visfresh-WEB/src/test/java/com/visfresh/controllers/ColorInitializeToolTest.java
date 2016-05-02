/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.visfresh.entities.Color;
import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ColorInitializeToolTest {
    private ColorInitializeTool tool = new ColorInitializeTool();

    /**
     * Default constructor.
     */
    public ColorInitializeToolTest() {
        super();
    }

    @Test
    public void testUniqueColors() {
        final int colorsCount = Color.values().length;
        final List<Device> devices = createDevices(colorsCount);

        tool.initColors(devices);
        //check unique
        final Map<Color, Integer> map = new HashMap<Color, Integer>();
        for (final Device d : devices) {
            Integer num = map.get(d.getColor());
            if (num == null) {
                num = 1;
            } else {
                num = num + 1;
            }

            map.put(d.getColor(), num);
        }

        assertEquals(colorsCount, map.size());

        for (final Map.Entry<Color, Integer> e : map.entrySet()) {
            assertEquals(new Integer(1), e.getValue());
        }
    }
    @Test
    public void testDuplicateColors() {
        final int colorsCount = Color.values().length;
        final List<Device> devices = createDevices(colorsCount * 2);

        tool.initColors(devices);
        //check unique
        final Map<Color, Integer> map = new HashMap<Color, Integer>();
        for (final Device d : devices) {
            Integer num = map.get(d.getColor());
            if (num == null) {
                num = 1;
            } else {
                num = num + 1;
            }

            map.put(d.getColor(), num);
        }

        assertEquals(colorsCount, map.size());

        for (final Map.Entry<Color, Integer> e : map.entrySet()) {
            assertEquals(new Integer(2), e.getValue());
        }
    }
    /**
     * @param count
     * @return
     */
    private List<Device> createDevices(final int count) {
        final long base = 100000000000l;
        final List<Device> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            final Device d = new Device();
            d.setImei("2384097" + (base + 1));
            list.add(d);
        }
        return list;
    }
}
