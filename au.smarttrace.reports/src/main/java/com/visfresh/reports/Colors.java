/**
 *
 */
package com.visfresh.reports;

import java.awt.Color;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class Colors {
    public static final Color CELL_BORDER = new Color(191, 204, 204);
    public static final Color CELL_BG = new Color(236, 247, 247);
    public static final Color DEFAULT_GREEN = new Color(109, 192, 55);

    /**
     * @param color origin color.
     * @param percent shadowing percents.
     * @return shadow color.
     */
    public static Color shadeColor(final Color color, final double percent) {
        final int t= percent < 0 ? 0 : 255;
        final double p= Math.abs(percent);

        final int R= color.getRed();
        final int G= color.getGreen();
        final int B= color.getBlue();

        final int rgb = 0x1000000
            + ((int) Math.round((t - R) * p) + R) * 0x10000
            + ((int) Math.round((t - G) * p) + G) * 0x100
            + ((int) Math.round((t - B) * p) + B);

        return new Color(rgb);
    }
}
