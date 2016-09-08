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
public class ImagePaintingSupport {
    private Map<Date, BufferedImage> renderedImages = new HashMap<>();
    private static final String ARRIVAL = "Arrival";

    private Map<Date, List<BufferedImage>> alertsFired = new HashMap<>();
    private Map<String, BufferedImage> alertImages = new HashMap<>();

    /**
     * Default constructor.
     */
    public ImagePaintingSupport() {
        super();
    }

    public void addFiredAlerts(final Date date, final AlertType... alerts) {
        final String[] imageNames = new String[alerts.length];
        for (int i = 0; i < imageNames.length; i++) {
            final String name = alerts[i].name();
            imageNames[i] = createAlertPictureName(name);
        }
        addFiredAlerts(date, imageNames);
    }
    /**
     * @param name
     * @param useTiny
     * @return
     */
    private static String createAlertPictureName(final String name) {
        return "alert" + name;
    }
    /**
     * @param date
     * @param alertNames
     */
    protected void addFiredAlerts(final Date date, final String... alertNames) {
        for (final String name: alertNames) {
            addImageMarkerForDate(date, possibleLoadAlertImage(name));
        }
    }
    /**
     * @param date
     * @param image
     */
    public void addImageMarkerForDate(final Date date, final BufferedImage image) {
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
    private BufferedImage possibleLoadAlertImage(final String name) {
        BufferedImage image = alertImages.get(name);
        if (image == null) {
            image = loadReportPngImage(name);
        }
        alertImages.put(name, image);
        return image;
    }
    /**
     * @param name image name.
     * @return
     */
    public static BufferedImage loadReportPngImage(final String name) {
        return loadImageResource("reports/images/shipment/" + name + ".png");
    }
    /**
     * @param name
     * @return
     */
    public static BufferedImage loadImageResource(final String name) {
        final URL url = ImagePaintingSupport.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new RuntimeException("Image not found: " + name);
        }

        try {
            final BufferedImage origin = ImageIO.read(url);
            //for now the image has loaded with byte data buffer need convert it to int buffer
            //for avoid the painting problems
            final BufferedImage image = new BufferedImage(origin.getWidth(), origin.getHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = image.createGraphics();
            try {
                g.drawImage(origin, 0, 0, null);
            } finally {
                g.dispose();
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
        } finally {
            g.dispose();
        }

        addImageMarkerForDate(date, image);
    }
    /**
     * @param date
     */
    public void addArrival(final Date date) {
        addFiredAlerts(date, createAlertPictureName(ARRIVAL));
    }
    /**
     * @param type alert type.
     * @return buffered image.
     */
    public static BufferedImage loadAlertImage(final AlertType type) {
        return loadReportPngImage(createAlertPictureName(type.name()));
    }
    /**
     * @param date the date which images are bound to.
     * @param size target image size
     * @return compound buffered image with including all images bound to given date.
     */
    public BufferedImage getRenderedImage(final Date date, final int size) {
        final BufferedImage bim = this.renderedImages.get(date);
        if (bim != null) {
            return bim;
        }

        final List<BufferedImage> images = getAlertImages(date);
        if (images != null) {
            final BufferedImage result = createCompoundImage(images, size);
            renderedImages.put(date, result);
            return result;
        }
        return null;
    }
    /**
     * @param images
     * @param singleImageSize
     * @return
     */
    public static BufferedImage createCompoundImage(final List<BufferedImage> images,
            final int singleImageSize) {
        //calculate image width
        final int gap = 2;
        final int w = singleImageSize + (images.size() - 1) * gap;
        final int h = w;

        //render complex image
        final BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = result.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            int i = 0;
            for (final BufferedImage a : images) {
                if (a.getWidth() != singleImageSize || a.getHeight() != singleImageSize) {
                    final Graphics2D g1 = (Graphics2D) g.create(i, i, singleImageSize, singleImageSize);
                    try {
                        g1.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);
                        g1.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g1.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                        final double scale = (double) singleImageSize / a.getWidth();
                        g1.drawImage(a, AffineTransform.getScaleInstance(scale, scale), null);
                    } finally {
                        g1.dispose();
                    }
                } else {
                    g.drawImage(a, i * gap, i * gap, null);
                }

                i++;
            }
        } finally {
            g.dispose();
        }
        return result;
    }
    /**
     * @param image
     * @param iconSize
     * @return
     */
    public static BufferedImage scaleImage(final BufferedImage image, final int iconSize) {
        final List<BufferedImage> images = new LinkedList<>();
        images.add(image);
        return createCompoundImage(images, iconSize);
    }

    /**
     * @param createShippedToImage
     * @return
     */
    public static void flip(final BufferedImage a) {
        final int w = a.getWidth();
        final int h = a.getHeight();

        final int w2 = w / 2;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w2; x++) {
                final int tmp = a.getRGB(x, y);
                a.setRGB(x, y, a.getRGB(w - 1 - x, y));
                a.setRGB(w - 1 - x, y, tmp);
            }
        }
    }
}
