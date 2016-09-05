/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Color;
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
    private Map<Date, BufferedImage> renderedImages = new HashMap<>();
    private static final String LAST_READING = "LastReading";
    private static final String ARRIVAL = "Arrival";

    private Map<Date, List<BufferedImage>> alertsFired = new HashMap<>();
    private Map<String, BufferedImage> alertImages = new HashMap<>();

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
        for (final String name: alertNames) {
            addFiredAlertImage(date, possibleLoadImage(name));
        }
    }
    /**
     * @param date
     * @param image
     */
    protected void addFiredAlertImage(final Date date, final BufferedImage image) {
        List<BufferedImage> list = getAlertImages(date);
        if (list == null) {
            list = new LinkedList<>();
            alertsFired.put(date, list);
        }
        list.add(image);
    }
    /**
     * @param type
     */
    private BufferedImage possibleLoadImage(final String name) {
        BufferedImage image = alertImages.get(name);
        if (image == null) {
            image = loadAlertImage(name);
        }
        alertImages.put(name, image);
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
            return ImageIO.read(url);
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
    public void addLastReading(final Date date, final Color color) {
        //create last reading image.
        final int size = 16;
        final int margins = 3;
        final int internalSize = size - 2 * margins;

        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB_PRE);

        final Graphics2D g = image.createGraphics();
        try {
            g.setColor(color);
            g.fillRect(margins, margins, internalSize, internalSize);

            //draw border
            g.setColor(Color.WHITE);
            g.drawRect(margins, margins, internalSize, internalSize);
        } finally {
            g.dispose();
        }

        addFiredAlertImage(date, image);
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
    /**
     * @param date
     * @return
     */
    public BufferedImage getRenderedImage(final Date date, final int size) {
        final BufferedImage bim = this.renderedImages.get(date);
        if (bim != null) {
            return bim;
        }

        final List<BufferedImage> images = getAlertImages(date);
        if (images != null) {
            //calculate image width
            final int w = size + images.size() - 1;
            final int h = w;

            //render complex image
            final BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            final Graphics2D g = result.createGraphics();
            try {
                int i = 0;
                for (final BufferedImage a : images) {
                    if (a.getWidth() != size || a.getHeight() != size) {
                        final Graphics2D g1 = (Graphics2D) g.create(i, i, size, size);
                        try {
                            g1.setRenderingHint(RenderingHints.KEY_RENDERING,
                                    RenderingHints.VALUE_RENDER_QUALITY);
                            g1.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g1.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                    RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                            final double scale = (double) size / a.getWidth();
                            g1.drawImage(a, AffineTransform.getScaleInstance(scale, scale), null);
                        } finally {
                            g1.dispose();
                        }
                    } else {
                        g.drawImage(a, i, i, null);
                    }

                    i++;
                }
            } finally {
                g.dispose();
            }

            renderedImages.put(date, result);
            return result;
        }
        return null;
    }
}
