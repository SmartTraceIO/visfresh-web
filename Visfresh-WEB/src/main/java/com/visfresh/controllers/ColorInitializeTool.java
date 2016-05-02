/**
 *
 */
package com.visfresh.controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.visfresh.entities.Color;
import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ColorInitializeTool {
    /**
     * Default constructor.
     */
    public ColorInitializeTool() {
        super();
    }

    /**
     * @param devices
     */
    public void initColors(final List<Device> devices) {
        List<Color> colors = new LinkedList<>();
        final Random random = new Random();

        for (final Device d : devices) {
            if (colors.isEmpty()) {
                colors = getFullColorList();
            }

            final Color c = colors.remove(random.nextInt(colors.size()));
            d.setColor(c);
        }
    }

    /**
     * @return list of colors.
     */
    private List<Color> getFullColorList() {
        final Color[] colors = Color.values();
        final List<Color> list = new ArrayList<>(colors.length);
        for (final Color color : colors) {
            list.add(color);
        }
        return list;
    }
}
