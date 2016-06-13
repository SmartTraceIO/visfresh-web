/**
 *
 */
package com.visfresh.entities;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum Color {
//    Aqua("#00FFFF"),
    Black("#000000"),
    Blue("#0000FF"),
    BlueViolet("#8A2BE2"),
    Brown("#A52A2A"),
//    Crimson("#DC143C"),
//    Cyan("#00FFFF"),
    DarkBlue("#00008B"),
    DarkCyan("#008B8B"),
    DarkGoldenrod("#B8860B"),
    DarkGreen("#006400"),
    DarkHhaki("#BDB76B"),
    DarkMagenta("#8B008B"),
    DarkOlivegreen("#556B2F"),
    DarkOrange("#FF8C00"),
    DarkOrchid("#9932CC"),
    DarkRed("#8B0000"),
    DarkSalmon("#E9967A"),
    DarkTurquoise("#00CED1"),
    DarksLategray("#2F4F4F"),
    DimGray("#696969"),
//    Fuchsia("#FF00FF"),
//    Gold("#FFD700"),
    GoldenRod("#DAA520"),
    Gray("#808080"),
    Green("#008000"),
    HotPink("#FF69B4"),
    IndianRed("#CD5C5C"),
    Indigo("#4B0082"),
//    Lime("#00FF00"),
//    Magenta("#FF00FF"),
    Maroon("#800000"),
    MediumAquamarine("#66CDAA"),
    MediumsLateBlue("#7B68EE"),
    Navy("#000080"),
    Olive("#808000"),
//    Orange("#FFA500"),
//    OrangeRed("#FF4500"),
    PaleVioletRed("#DB7093"),
    Peru("#CD853F"),
    Purple("#800080"),
//    Red("#FF0000"),
    RosyBrown("#BC8F8F"),
    RoyalBlue("#4169E1"),
    SaddleBrown("#8B4513"),
    Salmon("#FA8072"),
    SandyBrown("#F4A460"),
    SeaGreen("#2E8B57"),
    Sienna("#A0522D"),
    SlateBlue("#6A5ACD"),
    SteelBlue("#4682B4"),
    Tan("#D2B48C"),
    Teal("#008080"),
    Tomato("#FF6347"),
//    Turquoise("#40E0D0"),
    Violet("#EE82EE"),
    YellowGreen("#9ACD32");

    private String htmlValue;
    /**
     * @param htmlValue HTML value of color.
     */
    private Color(final String htmlValue) {
        this.htmlValue = htmlValue;
    }

    /**
     * @return HTML value of color
     */
    public String getHtmlValue() {
        return htmlValue;
    }

    /**
     * Main method for calculate right ordering of enumeration names.
     * @param args
     */
    public static void main(final String[] args) {
        final List<String> list = new LinkedList<>();
        for (final Color c : values()) {
            list.add("    " + c.name() + "(\"" + c.getHtmlValue() + "\"),");
        }

        Collections.sort(list);
        for (final String str : list) {
            System.out.println(str);
        }
    }
}
