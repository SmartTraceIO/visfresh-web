/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.visfresh.entities.AlertType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertPaintingSupport {
    private static final String LAST_READING = "LastReading";
    private static final String ARRIVAL = "Arrival";

    private Map<Date, List<BufferedImage>> alertsFired = new HashMap<>();
    private Map<String, BufferedImage> imageCache = new HashMap<>();
    public static final int ICON_SIZE = 17;

    /**
     * Default constructor.
     */
    public AlertPaintingSupport() {
        super();
    }

    public void addFiredAlerts(final Date date, final AlertType... alerts) {
        final String[] alertNames = new String[alerts.length];
        for (int i = 0; i < alertNames.length; i++) {
            alertNames[i] = alerts[i].name();
        }
        addFiredAlerts(date, alertNames);
    }
    /**
     * @param date
     * @param alertNames
     */
    protected void addFiredAlerts(final Date date, final String... alertNames) {
        List<BufferedImage> list = getAlertImages(date);
        if (list == null) {
            list = new LinkedList<>();
            alertsFired.put(date, list);
        }
        for (final String name: alertNames) {
            list.add(possibleLoadImage(name));
        }
    }
    /**
     * @param type
     */
    private BufferedImage possibleLoadImage(final String name) {
        BufferedImage image = imageCache.get(name);
        if (image == null) {
            image = loadAlertImage(name);
        }
        imageCache.put(name, image);
        return image;
    }
    public static BufferedImage loadLastReaing() {
        return loadAlertImage(LAST_READING);
    }
    /**
     * @param name
     * @return
     */
    protected static BufferedImage loadAlertImage(final String name) {
        final String resourcePath = "reports/images/shipment/alert";
        final URL url = AlertPaintingSupport.class.getClassLoader().getResource(
                resourcePath + name + ".png");
        if (url == null) {
            throw new RuntimeException("Image not found for alert " + name);
        }

        try {
            BufferedImage image = ImageIO.read(url);
            if (image != null && image.getWidth() != 17 && image.getHeight() != 17) {
                final BufferedImage im = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB_PRE);

                final Graphics2D g = im.createGraphics();
                try {
                    final double scale = (double) ICON_SIZE/ image.getWidth();
                    g.setTransform(AffineTransform.getScaleInstance(scale, scale));
                    g.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    g.drawImage(image, 0, 0, null);
                } finally {
                    g.dispose();
                }

                image = im;
            }
            return image;
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load image", e);
        }
    }

    /**
     * @param date
     * @return
     */
    public List<BufferedImage> getAlertImages(final Date date) {
        return alertsFired.get(date);
    }
    /**
     * @param date
     */
    public void addLastReading(final Date date) {
        addFiredAlerts(date, LAST_READING);
    }
    /**
     * @param date
     */
    public void addArrival(final Date date) {
        addFiredAlerts(date, ARRIVAL);
    }
    /**
     * @param type
     * @return
     */
    public static BufferedImage loadAlertImage(final AlertType type) {
        return loadAlertImage(type.name());
    }
}
