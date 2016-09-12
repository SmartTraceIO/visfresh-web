/**
 *
 */
package com.visfresh.services;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.reports.ShortTrackerEventsImporter;
import com.visfresh.reports.geomap.AbstractGeoMapBuiler;
import com.visfresh.reports.geomap.GoogleGeoMapBuiler;
import com.visfresh.reports.geomap.OpenStreetMapBuilder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@SuppressWarnings("serial")
public class EventsOptimizerDebugger extends JFrame {
    private final List<ShortTrackerEvent> originReadings;

    private AbstractGeoMapBuiler builder = new GoogleGeoMapBuiler();
    private final int zoom;
    private Point mapLocation;
    private BufferedImage map;
    private int position;

    private final JPanel viewPanel;
    /**
     * Default constructor.
     */
    public EventsOptimizerDebugger(final List<ShortTrackerEvent> readings) {
        super("Event Optimizer debugger");
        new EventsNullCoordinatesCorrector().correct(readings);
        this.originReadings = readings;

        new EventsNullCoordinatesCorrector().correct(readings);

        final Dimension viewArea = new Dimension(250, 250);

        final List<Point2D> coords = getCoordinates(readings);
        zoom = builder.calculateZoom(coords, viewArea, 10);

        final Rectangle r = builder.getMapBounds(coords, zoom);
        this.mapLocation = new Point(
                (int) (r.getX() - (viewArea.getWidth() - r.getWidth()) / 2.),
                (int) (r.getY() - (viewArea.getHeight() - r.getHeight()) / 2.));

        final int width = (int) Math.floor(viewArea.getWidth());
        final int height = (int) Math.floor(viewArea.getHeight());

        //create picture.
        this.map = createMap(width, height);

        //create UI.
        final JPanel cp = new JPanel(new BorderLayout(5, 5));
        setContentPane(cp);

        //create tool bar.
        final JToolBar tb = new JToolBar();
        tb.add(createResetAction());
        tb.add(createRunAction());
        tb.add(createDebugAction());

        cp.add(tb, BorderLayout.NORTH);

        //view panel
        viewPanel = new JPanel() {
            /* (non-Javadoc)
             * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
             */
            @Override
            protected void paintComponent(final Graphics g) {
                paintMap((Graphics2D) g);
            }
        };
        viewPanel.setBackground(Color.WHITE);
        cp.setPreferredSize(new Dimension(map.getWidth() * 2, map.getHeight()));
        cp.add(viewPanel, BorderLayout.CENTER);
    }

    /**
     * @return
     */
    private Action createDebugAction() {
        return new AbstractAction("Debug") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                doDebug();
            }
        };
    }
    /**
     * @return
     */
    private Action createRunAction() {
        return new AbstractAction("Run") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        while (position < originReadings.size()) {
                            try {
                                Thread.sleep(50);
                            } catch (final InterruptedException e) {
                                e.printStackTrace();
                            }
                            SwingUtilities.invokeLater(new Runnable() {
                                /* (non-Javadoc)
                                 * @see java.lang.Runnable#run()
                                 */
                                @Override
                                public void run() {
                                    doDebug();
                                }
                            });
                        }
                    }
                }.start();
            }
        };
    }

    /**
     *
     */
    protected void doDebug() {
        if (position < originReadings.size()) {
            position++;
            viewPanel.repaint();
        }
    }

    /**
     * @return
     */
    private Action createResetAction() {
        return new AbstractAction("Reset") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                position = 0;
                viewPanel.repaint();
            }
        };
    }

    /**
     * @param g
     */
    protected void paintMap(final Graphics2D g) {
        final AffineTransform oldTransform = g.getTransform();
        final AffineTransform at = new AffineTransform(oldTransform);

        paintMapWithPath(g, originReadings, position);

        at.translate(map.getWidth(), 0);
        g.setTransform(at);

        final Graphics2D g1 = (Graphics2D) g.create();
        try {
            paintMapWithPath(g1, new EventsOptimizer().optimize(originReadings), position);
        } finally {
            g1.dispose();
        }

        g.setTransform(oldTransform);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3f));

        g.drawLine(map.getWidth() - 1, 0, map.getWidth() - 1, map.getWidth());
    }

    /**
     * @param g
     * @param readings
     * @param pos
     */
    private void paintMapWithPath(final Graphics2D g,
            final List<ShortTrackerEvent> readings, final int pos) {
        g.drawImage(map, 0, 0, null);
        drawReadings(g, readings, pos);
    }

    /**
     * @return
     */
    private BufferedImage createMap(final int width, final int height) {
        //use image buffer for avoid of problems with alpha chanel.
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();

        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            //set transparency before draw map
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.8f));

            builder.paint(g, mapLocation, zoom, width, height);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            g.dispose();
        }
        return image;
    }

    private void drawReadings(final Graphics2D g, final List<ShortTrackerEvent> readings, final int sizeToPaint) {
        int i= 0;

        //create path shape
        final GeneralPath path = new GeneralPath();
        for (final ShortTrackerEvent e : readings) {
            if (i >= sizeToPaint) {
                break;
            }
            final int x = Math.round(OpenStreetMapBuilder.lon2position(
                    e.getLongitude(), zoom) - mapLocation.x);
            final int y = Math.round(OpenStreetMapBuilder.lat2position(
                    e.getLatitude(), zoom) - mapLocation.y);
            final Point2D cp = path.getCurrentPoint();
            if (cp == null) {
                path.moveTo(x, y);
            } else if (Math.round(cp.getX()) != x || Math.round(cp.getY()) != y) {
                path.lineTo(x, y);
            }

            i++;
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2.f));
        g.setColor(Color.RED);
        g.draw(path);
    }

    /**
     * @param readings
     * @return
     */
    private List<Point2D> getCoordinates(final List<ShortTrackerEvent> readings) {
        final List<Point2D> coords = new LinkedList<>();
        for (final ShortTrackerEvent e : readings) {
            coords.add(new Point2D.Double(e.getLongitude(), e.getLatitude()));
        }
        return coords;
    }

    public static void main(final String[] args) throws Exception {
        final JFrame f = new EventsOptimizerDebugger(importReadings(new File(args[0])));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();

        f.setLocationByPlatform(true);
        f.setVisible(true);
    }

    /**
     * @param file
     * @return
     * @throws ParseException
     * @throws IOException
     */
    private static List<ShortTrackerEvent> importReadings(final File file) throws IOException, ParseException {
        final Reader r = new FileReader(file);
        try {
            return new ShortTrackerEventsImporter(7l).importEvents(r);
        } finally {
            r.close();
        }
    }
}
