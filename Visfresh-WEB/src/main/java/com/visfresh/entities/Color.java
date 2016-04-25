/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum Color {
    Aqua("#00FFFF"),
    Black("#000000"),
    Blue("#0000FF"),
    BlueViolet("#8A2BE2"),
    Brown("#A52A2A"),
    Crimson("#DC143C"),
    Cyan("#00FFFF"),
    DarkBlue("#00008B"),
    DarkGreen("#006400"),
    DarkOrange("#FF8C00"),
    Fuchsia("#FF00FF"),
    Gold("#FFD700"),
    GoldenRod("#DAA520"),
    Gray("#808080"),
    Green("#008000"),
    HotPink("#FF69B4"),
    IndianRed ("#CD5C5C"),
    Indigo ("#4B0082"),
    Lime("#00FF00"),
    Magenta("#FF00FF"),
    Maroon("#800000"),
    Navy("#000080"),
    Olive("#808000"),
    Orange("#FFA500"),
    OrangeRed("#FF4500"),
    PaleVioletRed("#DB7093"),
    Purple("#800080"),
    Red("#FF0000"),
    RoyalBlue("#4169E1"),
    SaddleBrown("#8B4513"),
    Salmon("#FA8072"),
    SandyBrown("#F4A460"),
    SeaGreen("#2E8B57"),
    SlateBlue("#6A5ACD"),
    Tan("#D2B48C"),
    Teal("#008080"),
    Tomato("#FF6347"),
    Turquoise("#40E0D0"),
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
}
